package dev.rudrade.chat.repository;

import dev.rudrade.chat.model.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    @Query("select u from User u where u.id = ?#{principal?.id}")
    Optional<User> findDetails();
}
