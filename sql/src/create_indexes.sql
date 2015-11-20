create index usr_lower_login on usr (lower(login));
create index notif_lower_usrlogin on notification (lower(usr_login));
create index msg_lower_senderlogin on message (lower(sender_login));
create index chat_lower_initsender on chat (lower(init_sender));
create index chatl_lower_member on chat_list (lower(member));