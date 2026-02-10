package dev.rudrade.chat.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import dev.rudrade.chat.model.MessageSummary;
import dev.rudrade.chat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    public List<MessageSummary> findSummaries(UUID userId, String term) {
        log.trace("findSummaries - term:"+term);
        var pageable = PageRequest.of(0, 10);

        var name = "__EMPTY__".equals(term) || term.isBlank() ? null : term;
        List<MessageSummary> messages = messageRepository.findLatest(userId, name, pageable).toList();

        log.trace("found messages:"+messages);
        return messages;
    }
}
