package dev.rudrade.chat.dto.request;

public record MessageSearchFilter(FilterType filterType, String term, Integer offset, Integer limit) {

    public enum FilterType {
        ONLY_WITH_MESSAGES,
        SEARCH
    }
}
