
-- User w/ chat - 2 messages
WITH u AS (
    INSERT INTO users (id, active, password, username, name)
    VALUES (uuidv4(), true, 'temp', 'user-temp', 'temp')
    RETURNING id
),
c AS (
    INSERT INTO chat (id, type)
    VALUES (uuidv4(), 'ONE')
    RETURNING id
)
INSERT INTO message (id, dt_created, text, chat_id, user_id)
SELECT uuidv4(), to_timestamp(concat('20',gs,'5-01-01 10:00:00'), 'YYYY-MM-DD'), 'msg-' || gs, c.id, u.id
FROM u, c, generate_series(1,2) as gs;

-- User w/ chat - 1 message
WITH u AS (
    INSERT INTO users (id, active, password, username, name)
    VALUES (uuidv4(), true, 'temp', 'user-temp-2', 'temp-2')
    RETURNING id
),
c AS (
    INSERT INTO chat (id, type)
    VALUES (uuidv4(), 'ONE')
    RETURNING id
)
INSERT INTO message (id, dt_created, text, chat_id, user_id)
SELECT uuidv4(), now(), 'msg-3', c.id, u.id
FROM u, c;

-- User w/ 2 chats - 2 messages
WITH u AS (
    INSERT INTO users (id, active, password, username, name)
    VALUES (uuidv4(), true, 'temp', 'user-temp-5', 'temp-5')
    RETURNING id
),
c AS (
    INSERT INTO chat (id, type)
    select uuidv4(), 'GROUP'
    RETURNING id
)
INSERT INTO message (id, dt_created, text, chat_id, user_id)
SELECT uuidv4(), now(), 'msg-5-1', c.id, u.id
FROM u, c;

WITH c AS (
    INSERT INTO chat (id, type)
    select uuidv4(), 'GROUP'
    RETURNING id
)
INSERT INTO message (id, dt_created, text, chat_id, user_id)
SELECT uuidv4(), now(), 'msg-5-2', c.id, u.id
FROM users u, c
where u.name = 'temp-5';

-- User w/o chat
INSERT INTO users (id, active, password, username, name)
VALUES (uuidv4(), true, 'temp', 'user-temp-3', 'temp-3');

-- Inactive user
INSERT INTO users (id, active, password, username, name)
VALUES (uuidv4(), false, 'temp', 'user-temp-4', 'temp-4');