package dev.rudrade.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.repository.MessageRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    private MessageService target;

    @BeforeEach
    void init() {
        target = new MessageService(messageRepository);
    }

    //==================
    //  findSummaries
    //==================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"__EMPTY__"})
    void itShouldFindSummariesWithBlankTerm(String term) {
        var userId = UUID.randomUUID();

        var expected = List.of(
            new MessageSummary("test 1", UUID.randomUUID(), "text", LocalDateTime.now(), UUID.randomUUID()),
            new MessageSummary("test 2", UUID.randomUUID(), null, null, UUID.randomUUID())
        );

         when(messageRepository.findLatest(eq(userId), nullable(String.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(expected));

        var result = target.findSummaries(userId, term);
        assertThat(result)
            .isNotNull()
            .isNotEmpty()
            .containsExactlyInAnyOrderElementsOf(expected);

        verify(messageRepository, times(1)).findLatest(eq(userId), nullable(String.class), any(Pageable.class));
        verifyNoMoreInteractions(messageRepository);
    }

    @Test
    void itShouldFindSummaries() {
        var userId = UUID.randomUUID();
        var term = "test";

        var expected = List.of(
            new MessageSummary("test 1", UUID.randomUUID(), "text", LocalDateTime.now(), UUID.randomUUID()),
            new MessageSummary("test 2", UUID.randomUUID(), null, null, UUID.randomUUID())
        );

        when(messageRepository.findLatest(eq(userId), eq(term), any(Pageable.class)))
            .thenReturn(new PageImpl<>(expected));

        var result = target.findSummaries(userId, term);
        assertThat(result)
            .isNotNull()
            .isNotEmpty()
            .containsExactlyInAnyOrderElementsOf(expected);

        verify(messageRepository, times(1)).findLatest(eq(userId), eq(term), any(Pageable.class));
        verifyNoMoreInteractions(messageRepository);
    }

}
