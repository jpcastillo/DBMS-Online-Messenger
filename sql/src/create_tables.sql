DROP TABLE NOTIFICATION;
DROP TABLE MEDIA_ATTACHMENT;
DROP TABLE MESSAGE;
DROP TABLE CHAT_LIST;
DROP TABLE CHAT;
DROP TABLE USER_LIST_CONTAINS;
DROP TABLE USR;
DROP TABLE USER_LIST;

CREATE TABLE USER_LIST(
	list_id serial,
	list_type char(10) NOT NULL, 
	PRIMARY KEY(list_id));

CREATE TABLE USR(
	login char(50), 
	phoneNum CHAR(16) UNIQUE NOT NULL, 
	password char(50) NOT NULL,
	status char(140),
	block_list integer,
	contact_list integer,
	Primary Key(login),
	FOREIGN KEY(block_list) REFERENCES USER_LIST(list_id),
	FOREIGN KEY(contact_list) REFERENCES USER_LIST(list_id));

CREATE TABLE USER_LIST_CONTAINS(
	list_id integer,
	list_member char(50),
	PRIMARY KEY(list_id,list_member), 
	FOREIGN KEY(list_id) REFERENCES USER_LIST(list_id) ON DELETE CASCADE,
	FOREIGN KEY(list_member) REFERENCES USR(login) ON DELETE CASCADE);

CREATE TABLE CHAT(
	chat_id serial,
	chat_type char(50) NOT NULL,
	init_sender char(50),
	PRIMARY KEY(chat_id), 
	FOREIGN KEY(init_sender) REFERENCES USR(login));

CREATE TABLE CHAT_LIST(
	chat_id integer,
	member char(50),
	PRIMARY KEY(chat_id,member),
	FOREIGN KEY(member) REFERENCES USR(login),
	FOREIGN KEY(chat_id) REFERENCES CHAT(chat_id));

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

CREATE TABLE MEDIA_ATTACHMENT(
	media_id serial, 
	media_type char(10),
	URL char(256) NOT NULL,
	msg_id integer, 
	PRIMARY KEY(media_id), 
	FOREIGN KEY(msg_id) REFERENCES MESSAGE(msg_id));

CREATE TABLE NOTIFICATION(
	usr_login char(50), 
	msg_id integer,
	PRIMARY KEY(usr_login,msg_id),
	FOREIGN KEY(usr_login) REFERENCES USR(login),
	FOREIGN KEY(msg_id) REFERENCES MESSAGE(msg_id));