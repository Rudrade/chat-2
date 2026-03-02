
-- User w/ chat - 2 messages
INSERT INTO chat (id, type) VALUES ('f95319a0-b23d-435c-94ee-9129e79261d1', 'ONE');

insert into chat_users (chats_id, users_id) values ('f95319a0-b23d-435c-94ee-9129e79261d1', '5d0f7c31-32c5-493b-92bc-4ab29f62af7c');
insert into chat_users (chats_id, users_id) values ('f95319a0-b23d-435c-94ee-9129e79261d1', '29a8d960-46d2-4e55-80ab-7f6477541a28');

INSERT INTO message (id, dt_created, text, chat_id, user_id) values (uuidv4(), to_timestamp(concat('2015-01-01 10:00:00'), 'YYYY-MM-DD'), 'msg-1', 'f95319a0-b23d-435c-94ee-9129e79261d1', '5d0f7c31-32c5-493b-92bc-4ab29f62af7c');
INSERT INTO message (id, dt_created, text, chat_id, user_id) values (uuidv4(), to_timestamp(concat('2025-01-01 10:00:00'), 'YYYY-MM-DD'), 'msg-2', 'f95319a0-b23d-435c-94ee-9129e79261d1', '5d0f7c31-32c5-493b-92bc-4ab29f62af7c');

-- User w/ chat - 1 message
INSERT INTO chat (id, type) VALUES ('e6eee34f-a0fa-4996-aff1-dd0f97d70d55', 'ONE');

insert into chat_users (chats_id, users_id) values ('e6eee34f-a0fa-4996-aff1-dd0f97d70d55', 'b1e622b0-c34b-4624-b1d2-8122b4d0d616');
insert into chat_users (chats_id, users_id) values ('e6eee34f-a0fa-4996-aff1-dd0f97d70d55', '29a8d960-46d2-4e55-80ab-7f6477541a28');

INSERT INTO message (id, dt_created, text, chat_id, user_id) values (uuidv4(), now(), 'msg-3', 'e6eee34f-a0fa-4996-aff1-dd0f97d70d55', 'b1e622b0-c34b-4624-b1d2-8122b4d0d616');

-- User w/ 2 chats - 2 messages
INSERT INTO chat (id, type) values ('a5351e79-48a3-472d-86bd-314f8e81f238', 'GROUP');

insert into chat_users (chats_id, users_id) values ('a5351e79-48a3-472d-86bd-314f8e81f238', 'aee5f52f-f10e-4b93-abbd-28303c3b9b2e');
insert into chat_users (chats_id, users_id) values ('a5351e79-48a3-472d-86bd-314f8e81f238', '29a8d960-46d2-4e55-80ab-7f6477541a28');

INSERT INTO message (id, dt_created, text, chat_id, user_id) values (uuidv4(), now(), 'msg-5-1', 'a5351e79-48a3-472d-86bd-314f8e81f238', 'aee5f52f-f10e-4b93-abbd-28303c3b9b2e');

INSERT INTO chat (id, type) values ('55a96a2f-f2ab-4155-98a4-f4a7bb50ab36', 'GROUP');

insert into chat_users (chats_id, users_id) values ('55a96a2f-f2ab-4155-98a4-f4a7bb50ab36', 'aee5f52f-f10e-4b93-abbd-28303c3b9b2e');
insert into chat_users (chats_id, users_id) values ('55a96a2f-f2ab-4155-98a4-f4a7bb50ab36', '29a8d960-46d2-4e55-80ab-7f6477541a28');

INSERT INTO message (id, dt_created, text, chat_id, user_id) values (uuidv4(), now(), 'msg-5-2', '55a96a2f-f2ab-4155-98a4-f4a7bb50ab36', 'aee5f52f-f10e-4b93-abbd-28303c3b9b2e');

