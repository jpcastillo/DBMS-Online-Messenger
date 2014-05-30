COPY USER_LIST
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/usr_list.csv'
WITH DELIMITER ';';
ALTER SEQUENCE user_list_list_id_seq RESTART 55906;

COPY USR
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/usr.csv'
WITH DELIMITER ';';

COPY USER_LIST_CONTAINS
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/usr_list_contains.csv'
WITH DELIMITER ';';

COPY CHAT
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/chat.csv'
WITH DELIMITER ';';
ALTER SEQUENCE chat_chat_id_seq RESTART 5001;

COPY CHAT_LIST
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/chat_list.csv'
WITH DELIMITER ';';

COPY MESSAGE
	(msg_id, 
	msg_text, 
	msg_timestamp, 
	sender_login,
	chat_id)
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/message.csv'
WITH DELIMITER ';';
ALTER SEQUENCE message_msg_id_seq RESTART 50000;

COPY MEDIA_ATTACHMENT
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/media_attachment.csv'
WITH DELIMITER ';';
ALTER SEQUENCE media_attachment_media_id_seq RESTART 2000;

COPY NOTIFICATION
FROM '/Users/Torcherist/Documents/school/Spring2014/cs166/project/03/code/data/notification.csv'
WITH DELIMITER ';';
