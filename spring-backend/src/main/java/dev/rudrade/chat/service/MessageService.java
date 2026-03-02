package dev.rudrade.chat.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.rudrade.chat.dto.MessageDto;
import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.dto.MessageSummaryDto;
import dev.rudrade.chat.dto.request.ChatMessageFilter;
import dev.rudrade.chat.dto.request.MessageSearchFilter;
import dev.rudrade.chat.dto.request.MessageSearchFilter.FilterType;
import dev.rudrade.chat.dto.response.MessageResponse;
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

    @Transactional(readOnly = true)
    public MessageResponse findMessages(ChatMessageFilter filter, UUID userId) {
        var chat = chatRepository.findByIdAndUser(filter.chatId(), userId);
        if (chat.isEmpty()) {
            return new MessageResponse(null, 0L);
        }

        var pageable = pageRequest(filter.offset(), filter.limit());
        var result = messageRepository.findByChat(filter.chatId(), pageable);

        return new MessageResponse(result.getContent(), result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<MessageSummaryDto> findSummaries(MessageSearchFilter filter, UUID userId) {
        log.debug("findSummaries - filter:"+filter);
        Objects.requireNonNull(userId, "userId must be passed to find user-specific summaries");
        Objects.requireNonNull(filter, "a filter must be passed");

        var pageRequest = pageRequest(filter.offset(), filter.limit());
        Page<MessageSummary> result;
        if (FilterType.SEARCH.equals(filter.filterType())) {
            result = messageRepository.findLatest(userId, filter.term(), pageRequest);
        
        } else if (FilterType.ONLY_WITH_MESSAGES.equals(filter.filterType())) {
            result = messageRepository.findLatestWithMessages(userId, pageRequest);
        
        } else {
            throw new InvalidDataException("filter type invalid");
        }

        return result.map(MapperUtil::messageSummaryDto).toList();
    }

    @Transactional
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

    private PageRequest pageRequest(Integer offset, Integer limit) {
        var iOffset = offset == null ? 0 : offset.intValue();
        var iLimit = limit == null || limit.intValue() < 1 ? 10 : limit.intValue();
        return PageRequest.of(iOffset, iLimit);
    }
}
