package dev.rudrade.chat.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import dev.rudrade.chat.model.Chat;

@Repository
public interface ChatRepository extends CrudRepository<Chat, UUID> {
    
}
