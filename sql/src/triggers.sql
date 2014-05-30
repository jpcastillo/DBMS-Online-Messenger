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
--	returns empty string on success. else error message.
--
create language plpgsql;
create or replace function newAccount() returns text as $$
declare
	retVal text := '';
	num_rows integer := 0;
begin

--

return retVal;
end;
$$ language plpgsql volatile;
---------------------------------------------------------------------
