--
-- 	Procedure for Login
--	This proc makes use of the usr_lower_login index on table usr
--	This proc also hashes the password at comparison
--
-- 	NOTE: there are three levels of volatility
-- 	volatile, stable, immutable
-- 	volatile is able to make modifications to DB and tells DB optimizer
--		not to optimize away anything. Stable is recommended for select
--		statements (best optimization) with no ups. Default is volatile.
--	http://www.postgresql.org/docs/8.1/static/xfunc-volatility.html
--
create language plpgsql;
create or replace function login(un char(50),pw char(50)) returns text as $$
declare
	retVal text := '';
	num_rows int := 0;
begin

-- save number of records that match credentials into a temp variable
select into num_rows count(*) from usr where lower(login) = lower(un) and password = md5(pw);

-- some conditionals to check validity of credentials
if num_rows = 0 then
	retVal := 'Error: Incorrect login/password.';
elsif num_rows = 1 then
	retVal := '';
	-- let's also update user's the status to 'Online'
	update usr set status = 'Online' where lower(login) = lower(un) and password = md5(pw);
else
	retVal := 'Error: Multiple matches returned.';
end if;

return retVal;
end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for logout. takes user's login name as input.
--	returns empty string on success. else error message.
--	updates user status to offline
--
create language plpgsql;
create or replace function logout(un char(50)) returns text as $$
declare
	retVal text := '';
	num_rows int := 0;
begin

-- save number of records that match user's login into a temp variable
select into num_rows count(*) from usr where lower(login) = lower(un);

-- some conditionals to check validity of credentials
if num_rows = 0 then
	retVal := 'Error: Invalid login.';
elsif num_rows = 1 then
	retVal := '';
	-- let's also update user's the status to 'Offline'
	update usr set status = 'Offline' where lower(login) = lower(un);
else
	retVal := 'Error: Multiple matches returned.';
end if;

return retVal;
end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for deleting account. takes user's login name as input.
--	returns empty string on success. else error message.
--	removes all instances of login from system.
--
create language plpgsql;
create or replace function deleteAccount(un char(50)) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	--num_chat_lists := 0;
	--num_notifications := 0;
	num_chats integer := 0;
	num_attachments integer := 0;
	bl_id integer := -1;
	cl_id integer := -1;
begin

-- save number of records that match user's login into a temp variable
select into num_rows count(*) from usr where lower(login) = lower(un);

-- some conditionals to check validity of credentials
if num_rows = 0 then
	retVal := 'Error: Invalid login.';
elsif num_rows = 1 then
	retVal := '';
	
	-- let's check if there are any chats owned by the user
	select into num_chats count(*) from chat where lower(init_sender) = lower(un);
	
	if num_chats != 0 then
	return 'Error: There are chats/attachments linked to this account.';
	end if;
	
	-- let's check if there are any media attachments owned by user
	select into num_attachments count(*) 
	from media_attachment 
	join message on media_attachment.msg_id = message.msg_id
	where lower(sender_login) = lower(un);
	
	if num_attachments != 0 then
	return 'Error: There are chats/attachments linked to this account.';
	end if;

	-- let's now delete user from everywhere!
	delete from notification where lower(usr_login) = lower(un);
	delete from message where lower(sender_login) = lower(un);
	delete from chat_list where lower(member) = lower(un);
	delete from chat where lower(init_sender) = lower(un);
	--	Note: table USER_LIST_CONTAINS cascades on delete of USR and USER_LIST

	-- below will delete usr, contact list, and block list entries
	select into cl_id,bl_id contact_list,block_list from usr where lower(login) = lower(un);
	delete from usr where lower(login) = lower(un);
	delete from user_list where list_id = cl_id or list_id = bl_id;
else
	retVal := 'Error: Multiple matches returned.';
end if;

return retVal;
end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc and trigger for new account.
--	assigns new block_list id and new contact_list id.
--

create language plpgsql;
create or replace function cl_id() returns trigger as $body$
begin
	new.block_list := nextval('user_list_list_id_seq');
	new.contact_list := nextval('user_list_list_id_seq');
	insert into user_list (list_id,list_type) values (new.block_list,'block');
	insert into user_list (list_id,list_type) values (new.contact_list,'contact');
	return new;
end;
$body$ language plpgsql volatile;

drop trigger cl_id_up on usr;
create trigger cl_id_up before insert on usr
for each row execute procedure cl_id();
---------------------------------------------------------------------

--
--	proc for new account. takes user's login,pw,phone as input.
--	returns empty string on success. else error message.
--
create language plpgsql;
create or replace function newAccount(v_Un char(50), v_Pw char(50), v_Phone char(16), v_Status char(140)) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	cleanPhone char(16) := '';
begin

-- remove white space from phone number
cleanPhone := regexp_replace(v_Phone, '[ \t\n\r]+', '', 'g');

-- save number of records that match user's login into a temp variable
select into num_rows count(*) from usr where lower(login) = lower(v_Un) or phoneNum = cleanPhone;

-- some conditionals to check validity of credentials
if num_rows = 0 then
	-- All good here. Let's continue.
	insert into usr (login,phonenum,password,status) values (v_Un,v_Phone,md5(v_Pw),v_Status);
	retVal := '';
else
	-- Login or Phone already exists.
	retVal := 'Error: Login/Phone is already in use.';
end if;

return retVal;
end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for retrieving chat members.
--	input: chat_id,
--	returns list of login (comma delimited string) on success.
--	else empty string.
--
create language plpgsql;
create or replace function chatListMembers(v_ChatId integer) returns text as $$
declare
	retVal text := '';
begin

select into retVal array_to_string( array( select btrim(member) from chat_list where chat_id = v_ChatId ), ',');
return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for retrieving list of chats a user is a part of.
--	input: login
--	returns list of chats that login is a member of (comma delimited string) on success.
--	else empty string.
--
create language plpgsql;
create or replace function userChatList(v_Login char(50)) returns text as $$
declare
	retVal text := '';
begin

--	must return the chat_type and chat_id
--	first: subquery, concatenate chat_id and chat_type into csv format
--	second: create an array of the returned results
--	third: change that array into a vbar delimited list
--	fourth: return this string
select into retVal array_to_string( array( 
	select cl.chat_id || ',' || c.chat_type as val1 
	from chat_list cl
	join chat c on cl.chat_id = c.chat_id
	where cl.member = v_Login 
	), '|' );

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

/*
CREATE TABLE MESSAGE(
	msg_id serial, 
	msg_text char(300) NOT NULL, 
	msg_timestamp timestamp NOT NULL,
	destr_timestamp timestamp, 
	sender_login char(50),
	chat_id integer,
	PRIMARY KEY(msg_id), 
	FOREIGN KEY(sender_login) REFERENCES USR(login),
	FOREIGN KEY(chat_id) REFERENCES CHAT(chat_id));

CREATE TABLE NOTIFICATION(
	usr_login char(50), 
	msg_id integer,
	PRIMARY KEY(usr_login,msg_id),
	FOREIGN KEY(usr_login) REFERENCES USR(login),
	FOREIGN KEY(msg_id) REFERENCES MESSAGE(msg_id));
*/

--
--	proc for sending a new message to a chat.
--	this proc must also create a notification for appropriate recipients
--	input: msg_text, destr_timestamp, sender_login, chat_id
--	returns empty string on success. else error string.
--
-- TimeStamp with TimeZone> 1981-03-01 15:55:10 -0800 
-- YYYY-MM-DD HH:MI:SS
-- select newMessage('Hello world!','2014-05-31 02:34:49','Torcherist3',0,'Norma') as ret;
create language plpgsql;
create or replace function newMessage(v_MsgText char(300),v_DestrTimeStr char(19),v_SenderLogin char(50),v_ChatId integer,v_RecipientLogin char(50)) returns text as $$
declare
	retVal text := '';
	cleanDestrStr text := '';
	fDestrTimeStr timestamp;
	num_rows integer := 0;
	newChatId integer := -1;
	newMsgId integer := -1;
begin

cleanDestrStr := regexp_replace(v_DestrTimeStr, '^[0-9]{4}(-)[0-9]{2}(-)[0-9]{2}( )[0-9]{2}(:)[0-9]{2}(:)[0-9]{2}$', '', 'g');
if length(cleanDestrStr) != 0 then
	return 'Error: Invalid self-destruction time';
end if;

fDestrTimeStr := to_timestamp(v_DestrTimeStr, 'YYYY-MM-DD HH:MI:SS');

select into num_rows count(*) from usr where lower(login) = lower(v_SenderLogin);
if num_rows = 0 then
	return 'Error: Invalid sender login.';
end if;

if v_ChatId < 0 then
	-- New Chat
	select into num_rows count(*) from usr where lower(login) = lower(v_RecipientLogin);
	if num_rows = 0 then
	return 'Error: Invalid recipient login.';
	end if;
	-- create new chat
	insert into chat (chat_type,init_sender) values ('private',v_SenderLogin);
	-- save new chat_id
	newChatId := currval('chat_chat_id_seq');
	-- add sender and recipient to new chat
	insert into chat_list (chat_id,member) values (newChatId,v_SenderLogin);
	insert into chat_list (chat_id,member) values (newChatId,v_RecipientLogin);
else
	select into num_rows count(*) from chat_list where chat_id = v_ChatId and member = v_SenderLogin;
	if num_rows = 0 then
	return 'Error: Sender is not a member of the chat.';
	end if;
	-- Existing Chat
	newChatId := v_ChatId;
end if;
-- add new message to system with the new chat_id
insert into message (msg_text,msg_timestamp,destr_timestamp,sender_login,chat_id) values (v_MsgText,now(),fDestrTimeStr,v_SenderLogin,newChatId);
-- save the new message id
newMsgId := currval('message_msg_id_seq');
-- must now add notifications to each user of chat (excluding sender)
insert into notification select cl.member, newMsgId from chat_list cl where cl.chat_id = newChatId and cl.member != v_SenderLogin;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------