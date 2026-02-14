package dev.rudrade.chat.repository;

import dev.rudrade.chat.model.User;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    @Query("select u from User u where u.id = ?#{principal?.id}")
    Optional<User> findDetails();

    @NativeQuery(
        "select distinct u.* "
        + "from users u "
        + "inner join chat_users cu on cu.users_id = u.id "
        + "where cu.chats_id = :chatId and u.active = true "
    )
    List<User> findActiveByChatId(UUID chatId);
}
