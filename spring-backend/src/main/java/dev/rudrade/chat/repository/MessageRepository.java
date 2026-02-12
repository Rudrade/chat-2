package dev.rudrade.chat.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.rudrade.chat.model.Message;
import dev.rudrade.chat.model.MessageSummary;

@Repository
public interface MessageRepository extends CrudRepository<Message, UUID> {

    // with aux as (
    // 	select distinct on(user_id) *
    // 	from message
    // 	order by user_id, dt_created desc
    // )
    // select u.id as user_id, u.name as name, aux.chat_id, aux.text, aux.dt_created
    // from users u
    // left join aux on aux.user_id = u.id
    // where u.id <> ? and u.active = true
    @Query(
        value = "with aux as (select distinct on(user_id) chat_id, text, dt_created, user_id from message order by user_id, dt_created desc) "
              + "select u.name as name, aux.chat_id, aux.text, aux.dt_created, u.id as user_id "
              + "from users u "
              + "left join aux on aux.user_id = u.id "
              + "where u.id <> :userId and u.active = true and (:name is null or u.name ilike concat('%',:name,'%'))"
              + "order by u.name",
        countQuery = "select count(*) from users u where u.id <> :userId and u.active = true and (:name is null or u.name ilike concat('%',:name,'%')",
        nativeQuery = true
    )
    Page<MessageSummary> findLatest(UUID userId, String name, Pageable pageable);
}
