package dev.rudrade.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import dev.rudrade.chat.dto.MessageInputDto;
import dev.rudrade.chat.dto.request.MessageSearchFilter;
import dev.rudrade.chat.dto.request.MessageSearchFilter.FilterType;
import dev.rudrade.chat.exception.InvalidDataException;
import dev.rudrade.chat.model.Chat;
import dev.rudrade.chat.model.Message;
import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.model.User;
import dev.rudrade.chat.repository.ChatRepository;
import dev.rudrade.chat.repository.MessageRepository;
import dev.rudrade.chat.util.MapperUtil;
import dev.rudrade.chat.util.ValidationUtil;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ValidationUtil validationUtil;
    @Mock private UserService userService;
    @Mock private ChatRepository chatRepository;
    private MessageService target;

    @BeforeEach
    void init() {
        target = new MessageService(messageRepository, validationUtil, userService, chatRepository);
    }
    //==================
    //  findSummaries
    //==================

    @Test
    void itShouldFindSummariesWithOnlyMessages() {
        var userId = UUID.randomUUID();
        var filter = new MessageSearchFilter(FilterType.ONLY_WITH_MESSAGES, null, null, null);

        var expected = List.of(
            new MessageSummary("test 1", UUID.randomUUID(), "text", LocalDateTime.now(), UUID.randomUUID()),
            new MessageSummary("test 2", UUID.randomUUID(), null, null, UUID.randomUUID())
        );

        var expectedDto = expected.stream().map(MapperUtil::messageSummaryDto).toList();

         when(messageRepository.findLatestWithMessages(eq(userId), any(Pageable.class)))
            .thenReturn(new PageImpl<>(expected));

        var result = target.findSummaries(filter, userId);
        assertThat(result)
            .isNotNull()
            .isNotEmpty()
            .containsExactlyInAnyOrderElementsOf(expectedDto);

        verify(messageRepository, times(1)).findLatestWithMessages(eq(userId), any(Pageable.class));
        verifyNoMoreInteractions(messageRepository, validationUtil, userService, chatRepository);
    }

    @Test
    void itShouldFindSummaries() {
        var userId = UUID.randomUUID();
        var term = "test";
        var filter = new MessageSearchFilter(FilterType.SEARCH, term, null, null);

        var expected = List.of(
            new MessageSummary("test 1", UUID.randomUUID(), "text", LocalDateTime.now(), UUID.randomUUID()),
            new MessageSummary("test 2", UUID.randomUUID(), null, null, UUID.randomUUID())
        );

        var expectedDto = expected.stream().map(MapperUtil::messageSummaryDto).toList();

        when(messageRepository.findLatest(eq(userId), eq(term), any(Pageable.class)))
            .thenReturn(new PageImpl<>(expected));

        var result = target.findSummaries(filter, userId);
        assertThat(result)
            .isNotNull()
            .isNotEmpty()
            .containsExactlyInAnyOrderElementsOf(expectedDto);

        verify(messageRepository, times(1)).findLatest(eq(userId), eq(term), any(Pageable.class));
        verifyNoMoreInteractions(messageRepository);
    }

    //================
    //  sendMessage
    //================

    @Test
    void itShouldThrowWhenMessageInvalid() {
        var user = new User();
        user.setId(UUID.randomUUID());

        var input = new MessageInputDto(null, UUID.randomUUID(), "");

        doThrow(InvalidDataException.class)
            .when(validationUtil)
            .validate(input);

        assertThrows(InvalidDataException.class,
            () -> target.sendMessage(input, user)
        );

        verify(validationUtil, times(1)).validate(input);
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldThrowWhenHasNoDestination() {
        var user = new User();
        user.setId(UUID.randomUUID());

        var input = new MessageInputDto(null, null, "test message");

        assertThrows(InvalidDataException.class,
            () -> target.sendMessage(input, user)
        );

        verify(validationUtil, times(1)).validate(input);
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldThrowWhenTargetUserIsSelf() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);

        var input = new MessageInputDto(null, userId, "test message");

        assertThrows(InvalidDataException.class,
            () -> target.sendMessage(input, user)
        );

        verify(validationUtil, times(1)).validate(input);
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldThrowWhenTargetUserDoesntExist() {
        var userId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);

        var input = new MessageInputDto(null, targetUserId, "test message");

        when(userService.findActiveById(targetUserId))
            .thenReturn(Optional.empty());

        assertThrows(InvalidDataException.class,
            () -> target.sendMessage(input, user)
        );

        verify(validationUtil, times(1)).validate(input);
        verify(userService, times(1)).findActiveById(targetUserId);
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldThrowWhenTargetChatDoesntExistForUser() {
        var userId = UUID.randomUUID();
        var chatId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);

        var input = new MessageInputDto(chatId, null, "test message");

        when(chatRepository.findByIdAndUser(chatId, userId))
            .thenReturn(Optional.empty());

        assertThrows(InvalidDataException.class,
            () -> target.sendMessage(input, user)
        );

        verify(validationUtil, times(1)).validate(input);
        verify(chatRepository, times(1)).findByIdAndUser(chatId, userId);
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldSendToExistingOneChat() {
        var userId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();
        var chatId = UUID.randomUUID();

        var user = new User();
        user.setId(userId);

        var targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setActive(true);

        var chat = new Chat();
        chat.setId(chatId);
        chat.setType(Chat.ChatType.ONE);

        var input = new MessageInputDto(null, targetUserId, "test message");

        when(userService.findActiveById(targetUserId))
            .thenReturn(Optional.of(targetUser));

        when(chatRepository.findByTypeAndUsers(Chat.ChatType.ONE.name(), List.of(userId, targetUserId)))
            .thenReturn(Optional.of(chat));

        when(messageRepository.save(any(Message.class)))
            .thenAnswer(invocation -> {
                var msg = (Message) invocation.getArgument(0);
                msg.setId(UUID.randomUUID());
                return msg;
            });

        var result = target.sendMessage(input, user);

        assertThat(result)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isNotNull();
                assertThat(r.idFrom()).isEqualTo(userId);
                assertThat(r.idChatTo()).isEqualTo(chatId);
                assertThat(r.text()).isEqualTo("test message");
                assertThat(r.dtSent()).isNotNull();
            });
        
            verify(validationUtil, times(1)).validate(input);
        verify(userService, times(1)).findActiveById(targetUserId);
        verify(chatRepository, times(1)).findByTypeAndUsers(Chat.ChatType.ONE.name(), List.of(userId, targetUserId));
        verify(messageRepository, times(1)).save(any(Message.class));
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldSendToUser() {
        var userId = UUID.randomUUID();
        var targetUserId = UUID.randomUUID();

        var user = new User();
        user.setId(userId);

        var targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setActive(true);

        var input = new MessageInputDto(null, targetUserId, "test message");

        when(userService.findActiveById(targetUserId))
            .thenReturn(Optional.of(targetUser));

        when(chatRepository.findByTypeAndUsers(Chat.ChatType.ONE.name(), List.of(userId, targetUserId)))
            .thenReturn(Optional.empty());

        when(messageRepository.save(any(Message.class)))
            .thenAnswer(invocation -> {
                var msg = (Message) invocation.getArgument(0);
                msg.setId(UUID.randomUUID());
                return msg;
            });

        var result = target.sendMessage(input, user);

        assertThat(result)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isNotNull();
                assertThat(r.idFrom()).isEqualTo(userId);
                assertThat(r.text()).isEqualTo("test message");
                assertThat(r.dtSent()).isNotNull();
            });

        verify(validationUtil, times(1)).validate(input);
        verify(userService, times(1)).findActiveById(targetUserId);
        verify(chatRepository, times(1)).findByTypeAndUsers(Chat.ChatType.ONE.name(), List.of(userId, targetUserId));
        verify(chatRepository, times(1)).save(any(Chat.class));
        verify(messageRepository, times(1)).save(any(Message.class));
        verifyNoMoreInteractions(validationUtil, messageRepository, userService, chatRepository);
    }

    @Test
    void itShouldSendToGroup() {
        var userId = UUID.randomUUID();
        var chatId = UUID.randomUUID();

        var user = new User();
        user.setId(userId);

        var chat = new Chat();
        chat.setId(chatId);
        chat.setType(Chat.ChatType.GROUP);

        var input = new MessageInputDto(chatId, null, "test message");

        when(chatRepository.findByIdAndUser(chatId, userId))
            .thenReturn(Optional.of(chat));

        when(messageRepository.save(any(Message.class)))
            .thenAnswer(invocation -> {
                var msg = (Message) invocation.getArgument(0);
                msg.setId(UUID.randomUUID());
                return msg;
            });

        var result = target.sendMessage(input, user);

        assertThat(result)
            .isNotNull()
            .satisfies(r -> {
                assertThat(r.id()).isNotNull();
                assertThat(r.idFrom()).isEqualTo(userId);
                assertThat(r.idChatTo()).isEqualTo(chatId);
                assertThat(r.text()).isEqualTo("test message");
                assertThat(r.dtSent()).isNotNull();
            });
        
    }
}
