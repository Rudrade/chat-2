insert into users(id, active, password, username, name) values ('29a8d960-46d2-4e55-80ab-7f6477541a28', true, '$2a$16$w.yonojJ0u2cf1LOgWlmXeSaDkiQd0ajL.3tm6Z1ueTnSHxefafse','user-test','test') on conflict do nothing;
insert into users(id, active, password, username, name) values ('29a8d960-46d2-4e55-80ab-7f6477541d28', true, '$2a$16$w.yonojJ0u2cf1LOgWlmXeSaDkiQd0ajL.3tm6Z1ueTnSHxefafse','user-test-2','test') on conflict do nothing;
insert into users(id, active, password, username, name) values ('29a8d960-46d2-4e55-80ac-7f6477541d28', true, '$2a$16$w.yonojJ0u2cf1LOgWlmXeSaDkiQd0ajL.3tm6Z1ueTnSHxefafse','user-test-3','test') on conflict do nothing;
insert into users(id, active, password, username, name) values ('29a8d960-46d2-4e55-80ad-7f6477541d28', true, '$2a$16$w.yonojJ0u2cf1LOgWlmXeSaDkiQd0ajL.3tm6Z1ueTnSHxefafse','user-test-4','test') on conflict do nothing;
insert into users(id, active, password, username, name) values ('29a8c960-46d2-4e55-80ad-7f6477541d28', true, '$2a$16$w.yonojJ0u2cf1LOgWlmXeSaDkiQd0ajL.3tm6Z1ueTnSHxefafse','user-test-5','test') on conflict do nothing;

INSERT INTO USERS (ID, ACTIVE, PASSWORD, USERNAME, NAME) VALUES ('4a33e6ec-215a-411f-ac8c-09d7beaa77dc', TRUE, 'TEMP', 'TEMP-A', 'TEMP-A') on conflict do nothing;
INSERT INTO USERS (ID, ACTIVE, PASSWORD, USERNAME, NAME) VALUES ('ff02df0d-1d76-4262-b00b-8de3bac55a49', TRUE, 'TEMP', 'TEMP-B', 'TEMP-B') on conflict do nothing;
INSERT INTO USERS (ID, ACTIVE, PASSWORD, USERNAME, NAME) VALUES ('371f245c-5ddc-4124-9dc6-3384a521c6db', TRUE, 'TEMP', 'TEMP-C', 'TEMP-C') on conflict do nothing;
INSERT INTO USERS (ID, ACTIVE, PASSWORD, USERNAME, NAME) VALUES ('a62f1573-24b2-49af-81d4-1fb2ee1ff208', TRUE, '$2a$16$w.yonojJ0u2cf1LOgWlmXeSaDkiQd0ajL.3tm6Z1ueTnSHxefafse', 'TEMP-D', 'TEMP-D') on conflict do nothing;
INSERT INTO USERS (ID, ACTIVE, PASSWORD, USERNAME, NAME) VALUES ('1b47ec5e-5e8a-4159-a3e9-397b7b4cff48', TRUE, 'TEMP', 'TEMP-E', 'TEMP-E') on conflict do nothing;
INSERT INTO USERS (ID, ACTIVE, PASSWORD, USERNAME, NAME) VALUES ('c91b04ad-f45b-4b29-994a-625b568bd0ce', FALSE, 'TEMP', 'TEMP-F', 'TEMP-F') on conflict do nothing;

INSERT INTO users (id, active, password, username, name) VALUES ('5d0f7c31-32c5-493b-92bc-4ab29f62af7c', true, 'temp', 'user-temp', 'temp') on conflict do nothing;
INSERT INTO users (id, active, password, username, name) VALUES ('b1e622b0-c34b-4624-b1d2-8122b4d0d616', true, 'temp', 'user-temp-2', 'temp-2') on conflict do nothing;
INSERT INTO users (id, active, password, username, name) VALUES ('aee5f52f-f10e-4b93-abbd-28303c3b9b2e', true, 'temp', 'user-temp-5', 'temp-5') on conflict do nothing;
INSERT INTO users (id, active, password, username, name) VALUES (uuidv4(), false, 'temp', 'user-temp-4', 'temp-4') on conflict do nothing;
INSERT INTO users (id, active, password, username, name) VALUES (uuidv4(), true, 'temp', 'user-temp-3', 'temp-3') on conflict do nothing;