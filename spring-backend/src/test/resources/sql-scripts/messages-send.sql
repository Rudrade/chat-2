with c as (
    insert into chat(id, type)
    values ('6fd61b05-4052-4a5d-881d-72ae3f1cbff6', 'GROUP')
    returning id
),
u as (
    select id
    from users
    where username like 'user-test%' and username <> 'user-test-5'
)
insert into chat_users (chats_id, users_id)
select c.id, u.id
from c, u;