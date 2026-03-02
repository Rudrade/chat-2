package dev.rudrade.chat.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.rudrade.chat.model.Chat;

import java.util.List;


@Repository
public interface ChatRepository extends CrudRepository<Chat, UUID> {

    @NativeQuery(
        " select c.* "
        + " from chat c "
        + " inner join chat_users cu on cu.chats_id = c.id "
        + " where c.type = :type and cu.users_id in :users "
        + " group by c.id "
        + " having count(distinct cu.users_id) = :#{#users.size()} and count(*) = :#{#users.size()}"
        + " limit 1 "
    )
    Optional<Chat> findByTypeAndUsers(String type, List<UUID> users);

    @NativeQuery(
        "select distinct c.* "
        + "from chat c "
        + "inner join chat_users cu on cu.chats_id = c.id "
        + "where c.id = :id and cu.users_id = :userId"
    )
    Optional<Chat> findByIdAndUser(UUID id, UUID userId);
    
}
