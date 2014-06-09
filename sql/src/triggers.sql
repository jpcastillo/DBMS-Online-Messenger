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
	return 'Error: Invalid login.';
elsif num_rows = 1 then
	--retVal := '';
	
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
	--retVal := 'Error: Multiple matches returned.';
	return 'Error: Multiple matches returned.';
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
	select cl.chat_id || '\\n' || c.chat_type as val1 
	from chat_list cl
	join chat c on cl.chat_id = c.chat_id
	where cl.member = v_Login 
	), '|[(^#^)]|' );

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

--
--	proc for adding user B to user A's contact/block list.
--	input: LoginA, LoginB, Control
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function addToContactBlock(v_LoginA char(50), v_LoginB char(50), v_Control integer) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	bl_id integer := -1;
	cl_id integer := -1;
begin

select into bl_id,cl_id block_list,contact_list from usr where lower(login) = lower(v_LoginA);
if bl_id is null or cl_id is null then
	return 'Error: Invalid subject login.';
end if;

select into num_rows count(*) from usr where lower(login) = lower(v_LoginB);
if num_rows = 0 then
	return 'Error: Invalid target login.';
end if;


if v_Control > 0 then
-- contact list
	select into num_rows count(*) from user_list_contains where list_id = cl_id and lower(list_member) = lower(v_LoginB);
	if num_rows = 0 then
	insert into user_list_contains values (cl_id,v_LoginB);
	else
	return 'Error: Target user already exists in that list.';
end if;
else
-- block list
	select into num_rows count(*) from user_list_contains where list_id = bl_id and lower(list_member) = lower(v_LoginB);
	if num_rows = 0 then
	insert into user_list_contains values (bl_id,v_LoginB);
	else
	return 'Error: Target user already exists in that list.';
	end if;
end if;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for removing user B from user A's contact/block list.
--	input: LoginA, LoginB, Control
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function delFromContactBlock(v_LoginA char(50), v_LoginB char(50), v_Control integer) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	bl_id integer := -1;
	cl_id integer := -1;
begin

select into bl_id,cl_id block_list,contact_list from usr where lower(login) = lower(v_LoginA);
if bl_id is null or cl_id is null then
	return 'Error: Invalid subject login (A).';
end if;

select into num_rows count(*) from usr where lower(login) = lower(v_LoginB);
if num_rows = 0 then
	return 'Error: Invalid target login (B).';
end if;

if v_Control > 0 then
	-- contact list
	delete from user_list_contains where list_id = cl_id and lower(list_member) = lower(v_LoginB);
else
	-- block list
	delete from user_list_contains where list_id = bl_id and lower(list_member) = lower(v_LoginB);
end if;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for reading notifications for user
--	input: login
--	returns string list of notificatoins on success. else error string.
--	msg_id\nchat_id\nsender_login\nmsg_timestamp [|[(^#^)]| ...]
create language plpgsql;
create or replace function readNotifications(v_Login char(50)) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
begin

select into num_rows count(*) from usr where lower(login) = lower(v_Login);
if num_rows = 0 then
	return 'Error: Invalid login.';
end if;

select into retVal array_to_string (
	array (
	select m.msg_id || '\\n' || m.chat_id || '\\n' || m.sender_login || '\\n' || msg_timestamp
	from notification n 
	join message m on n.msg_id = m.msg_id 
	where lower(n.usr_login) = lower(v_Login)
	),
'|[(^#^)]|');

if retVal is null then
	retVal := '';
end if;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for marking notifications as read
--	input: login, chat_id
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function markReadNotifications(v_Login char(50), v_MsgId text) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	num_chat_id integer := 0;
	tmp integer := 0;
	chat_str text := '';
	cur_str text := '';
begin

if length(v_MsgId) = 0 then
	return 'Error: Invalid message id, empty.';
end if;

select into num_rows count(*) from usr where lower(login) = lower(v_Login);
if num_rows = 0 then
	return 'Error: Invalid login.';
end if;

num_chat_id := length(regexp_replace(v_MsgId,'[^,]','','g'));

chat_str := v_MsgId;
for i in 1..num_chat_id loop
	select into tmp position(',' in chat_str);
	cur_str := substring(chat_str from 0 for tmp);
	chat_str := substring(chat_str from tmp+1 for length(chat_str)-tmp);
	--retVal := retVal || ' ' || cur_str;-- || ':' || chat_str || ':' || to_char(tmp,'FM999MI');
	delete from notification where lower(usr_login) = lower(v_Login) and msg_id = cur_str;
end loop;
	cur_str := chat_str;--substring(chat_str from 0 for tmp);
	--chat_str := substring(chat_str from tmp+1 for length(chat_str)-tmp);
	--retVal := retVal || ' ' || cur_str;-- || ':' || to_char(tmp,'FM999MI');
	delete from notification where lower(usr_login) = lower(v_Login) and msg_id = cur_str;
return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for listing members of contact list
--	input: login, control (0 is block, 1 is contact list)
--	returns login list string on success. else error string.
create language plpgsql;
create or replace function listContactBlock(v_Login char(50), v_Control integer) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	bl_id integer := 0;
	cl_id integer := 0;
	l_id integer := 0;
begin

select into bl_id,cl_id block_list,contact_list from usr where lower(login) = lower(v_Login);

if bl_id is null or cl_id is null then
	return 'Error: Invalid login.';
end if;


if v_Control > 0 then
-- contact list
	l_id := cl_id;
else
-- block list
	l_id := bl_id;
end if;

select into retVal array_to_string( array( select btrim(list_member) from user_list_contains where list_id = l_id ), ',' );

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for managing members of chat
--	input: chat_id, loginA, loginB, control (0 is remove, 1 is add)
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function editChatList(v_ChatID integer, v_LoginA char(50), v_LoginB char(50), v_Control integer) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
	tmp1 text := '';
	tmp2 text := '';
begin

select into num_rows count(*) from usr where lower(login) = lower(v_LoginB);

if num_rows = 0 then
	return 'Error: Invalid login.';
end if;

if v_Control > 0 then
-- add to chat
	select into num_rows count(*) from chat where lower(init_sender) = lower(v_LoginA) and chat_id = v_ChatID;

	if num_rows = 0 then
		return 'Error: User needs to be owner of chat.';
	end if;

	-- first check if LoginB is already in chat or not.
	select into num_rows count(*) from chat_list where lower(member) = lower(v_LoginB) and chat_id = v_ChatID;

	if num_rows > 0 then
		return 'Error: User is already in chat.';
	end if;

	-- LoginB is not in chat, let's add them.
	insert into chat_list values (v_ChatID, v_LoginB);
else
-- remove from chat
	if lower(v_LoginA) = lower(v_LoginB) then -- if1
		-- want to remove self
		select into num_rows count(*) from chat where lower(init_sender) = lower(v_LoginA) and chat_id = v_ChatID;
		
		if num_rows = 0 then -- if2
			-- is not the owner
			delete from notification where lower(usr_login) = lower(v_LoginA) and msg_id in (select msg_id from message where chat_id = v_ChatID);
			delete from chat_list where chat_id = v_ChatID and lower(member) = lower(v_LoginA);
			
		else -- if2
			-- is owner. need to assign new owner.
			select into tmp1,tmp2 cl.member,u.login from chat_list cl left join usr u on cl.member = u.login where cl.chat_id = v_ChatID and lower(cl.member) != lower(v_LoginA) order by u.login asc limit 1;
			
			if tmp1 is null then -- if3
				-- chat is empty
				delete from chat where chat_id = v_ChatID;
			elsif tmp2 is null then -- if3
				-- chat is not empty. chat member does not exist in usr. Chat is broken.
				delete from notification where msg_id in (select msg_id from message where chat_id = v_ChatID);
				delete from chat_list where chat_id = v_ChatID;
				delete from chat where chat_id = v_ChatID;
				return 'Error: Chat is broken. Removed chat and its members.';
			else
				-- chat is not empty. assign new owner.
				update chat set init_sender = tmp1 where chat_id = v_ChatID;
				delete from notification where lower(usr_login) = lower(v_LoginA) and msg_id in (select msg_id from message where chat_id = v_ChatID);
				delete from chat_list where chat_id = v_ChatID and lower(member) = lower(v_LoginA);
			end if; -- if3
		
		end if; -- if2

	else -- if1
		-- want to remove someone else
		select into num_rows count(*) from chat where lower(init_sender) = lower(v_LoginA) and chat_id = v_ChatID;
		if num_rows = 0 then -- if4
			return 'Error: User needs to be owner of chat.';
		end if; -- if4

		delete from notification where lower(usr_login) = lower(v_LoginB) and msg_id in (select msg_id from message where chat_id = v_ChatID);
		delete from chat_list where chat_id = v_ChatID and lower(member) = lower(v_LoginB);

	end if; -- if1
end if;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for deleting message
--	input: login, msg_id
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function delMessage(v_Login char(50), v_MsgID integer) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
begin

select into num_rows count(*) from message where lower(sender_login) = lower(v_Login) and msg_id = v_MsgID;

if num_rows = 0 then
	return 'Error: Invalid owner or message does not exist.';
end if;

delete from notification where msg_id = v_MsgID;
delete from message where msg_id = v_MsgID;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for deleting expired messages
create language plpgsql;
create or replace function selfDestruct() returns text as $$
declare ts timestamp;
begin
	ts := to_timestamp(now(),'YYYY-MM-DD HH:MI:SS');
	-- Messages with a self-destruction timestamp should be deleted from the system, after specified datetime and once it is read
	delete from message where destr_timestamp < ts and msg_id not in (select msg_id from notification);
   return '';
end;
$$ language plpgsql volatile;

-- trigger for calling selfDestruct() proc on insert and update events
--drop trigger selfDestruct on message;
--create trigger selfDestruct after insert or update on message for each row execute procedure selfDestruct();
---------------------------------------------------------------------

--
--	proc for updates a user's status
--	input: login, status
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function updateStatus(v_Login char(50), v_Status char(140)) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
begin

if length(v_Status) > 140 then
	return 'Error: Status string must be less than 141 characters.';
end if;

select into num_rows count(*) from usr where lower(login) = lower(v_Login);

if num_rows = 0 then
	return 'Error: Invalid login.';
end if;

update usr set status = v_Status where lower(login) = lower(v_Login);

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------

--
--	proc for deleting message
--	input: login, msg_id
--	returns empty string on success. else error string.
create language plpgsql;
create or replace function updateMessage(v_Login char(50), v_MsgID integer, v_MsgText char(300)) returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
begin

select into num_rows count(*) from message where lower(sender_login) = lower(v_Login) and msg_id = v_MsgID;

if num_rows = 0 then
	return 'Error: Invalid owner or message does not exist.';
end if;

update message set msg_text = v_MsgText, msg_timestamp = to_timestamp(now(),'YYYY-MM-DD HH:MI:SS') where lower(sender_login) = lower(v_Login) and msg_id = v_MsgID;

return retVal;

end;
$$ language plpgsql volatile;
---------------------------------------------------------------------
