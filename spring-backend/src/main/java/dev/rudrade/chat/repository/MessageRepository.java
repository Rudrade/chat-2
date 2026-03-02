package dev.rudrade.chat.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.model.Message;
import dev.rudrade.chat.model.MessageSummary;

@Repository
public interface MessageRepository extends CrudRepository<Message, UUID> {

    @Query(
        value = "with aux as (select distinct on(user_id, chat_id) chat_id, text, dt_created, user_id from message where chat_id in (select chats_id from chat_users where users_id = :userId) order by user_id, chat_id, dt_created desc) "
              + "select u.name as name, aux.chat_id, aux.text, aux.dt_created, u.id as user_id "
              + "from users u "
              + "left join aux on aux.user_id = u.id "
              + "where u.id <> :userId and u.active = true and (:name is null or u.name ilike concat('%',:name,'%'))"
              + "order by u.name",
        countQuery = " with aux as (select distinct on(user_id, chat_id) chat_id, text, dt_created, user_id from message where chat_id in (select chats_id from chat_users where users_id = :userId) order by user_id, chat_id, dt_created desc)  "
                    + " select count(u.id) from users u left join aux on aux.user_id = u.id  where u.id <> :userId and u.active = true and (:name is null or u.name ilike concat('%',:name,'%')) ",
        nativeQuery = true
    )
    Page<MessageSummary> findLatest(UUID userId, String name, Pageable pageable);

    @Query(
        value = "with chat_active as ( "
        + " select cu.chats_id, count(cu.users_id) "
        + " from chat_users cu "
        + " inner join users u on u.id = cu.users_id "
        + " where u.active = true "
        + " group by cu.chats_id "
        + " having count(cu.users_id) > 1 "
        + " ), chat_in as ( "
        + " select chats_id from chat_users where users_id = :userId "
        + " ) "
        + " select distinct on (m.chat_id) u.name, m.chat_id, m.text, m.dt_created, m.user_id "
        + " from message m "
        + " inner join chat_active ca on ca.chats_id = m.chat_id "
        + " inner join chat_in ci on ci.chats_id = m.chat_id "
        + " inner join users u on u.id = m.user_id "
        + " order by m.chat_id, m.dt_created desc ",
        countQuery = "with chat_active as ( "
        + " select cu.chats_id, count(cu.users_id) "
        + " from chat_users cu "
        + " inner join users u on u.id = cu.users_id "
        + " where u.active = true "
        + " group by cu.chats_id "
        + " having count(cu.users_id) > 1 "
        + " ), chat_in as ( "
        + " select chats_id from chat_users where users_id = :userId "
        + " ) "
        + " select count(distinct m.chat_id) "
        + " from message m "
        + " inner join chat_active ca on ca.chats_id = m.chat_id "
        + " inner join chat_in ci on ci.chats_id = m.chat_id "
        + " inner join users u on u.id = m.user_id ",
        nativeQuery = true
    )
    Page<MessageSummary> findLatestWithMessages(UUID userId,  Pageable pageable);

    @Query(
        " select new dev.rudrade.chat.dto.MessageDto(m.id, m.user.id, m.chat.id, m.text, m.dtCreated) "
        + " from Message m "
        + " where m.chat.id = :chatId "
        + " order by m.dtCreated desc "
    )
    Page<MessageDto> findByChat(UUID chatId, Pageable pageable); // TODO: Take this as example and apply to others
}
