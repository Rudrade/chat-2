INSERT INTO chat (id, type) VALUES ('01415185-b265-4362-b56f-fc7829e61cf0', 'ONE');

INSERT INTO message (id, dt_created,text,chat_id,user_id) 
select uuidv4(), now(),'msg','01415185-b265-4362-b56f-fc7829e61cf0','4a33e6ec-215a-411f-ac8c-09d7beaa77dc'
from generate_series(1,100);