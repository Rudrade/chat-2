package dev.rudrade.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.exception.InvalidDataException;
import dev.rudrade.chat.model.Chat;
import dev.rudrade.chat.model.Message;
import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.model.Chat.ChatType;
import dev.rudrade.chat.repository.ChatRepository;
import dev.rudrade.chat.repository.MessageRepository;
import dev.rudrade.chat.util.MapperUtil;
import dev.rudrade.chat.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ValidationUtil validationUtil; 
    private final UserService userService;
    private final ChatRepository chatRepository;

    public List<MessageSummary> findSummaries(UUID userId, String term) {
        log.trace("findSummaries - term:"+term);
        Objects.requireNonNull(userId, "userId must be passed to find user-specific summaries");

        var pageable = PageRequest.of(0, 10);

        var name = "__EMPTY__".equals(term) || term == null || term.isBlank() ? null : term;
        List<MessageSummary> messages = messageRepository.findLatest(userId, name, pageable).toList();

        log.trace("found messages:"+messages);
        return messages;
    }

    public MessageDto sendMessage(MessageInputDto input, User user)  {

        // Validate input
        Objects.requireNonNull(user, "user must exist to send a message");

        validationUtil.validate(input);

        if (input.idChat() == null && input.idUserTo() == null) {
            throw new InvalidDataException("a target for the message is necessary");
        }

        var message = new Message();
        if (input.idUserTo() != null) {
            if (input.idUserTo().equals(user.getId())) {
                throw new InvalidDataException("cannot send message to yourself");
            }
    
            // Validate if user to send message exists
            var userTo = userService.findActiveById(input.idUserTo());
            if (userTo.isEmpty()) {
                throw new InvalidDataException("user in question doesn't exist");
            }
            
            // Get chat between user and target exist
            var optChat = chatRepository.findByTypeAndUsers(ChatType.ONE.name(), List.of(user.getId(), input.idUserTo()));
            if (optChat.isPresent()) {
                message.setChat(optChat.get());
            } else {
                var chat = new Chat();
                chat.setType(ChatType.ONE);
                chat.setUsers(List.of(user, userTo.get()));
                message.setChat(chat);
                chatRepository.save(chat);
            }
        
        } else {
            var chat = chatRepository.findByIdAndUser(input.idChat(), user.getId());
            if (chat.isEmpty()) {
                throw new InvalidDataException("target chat must exist");
            }
            message.setChat(chat.get());
        }

        // Create message
        message.setDtCreated(LocalDateTime.now());
        message.setText(input.text());
        message.setUser(user);
        messageRepository.save(message);

        return MapperUtil.messageDto(message);
    }
}
