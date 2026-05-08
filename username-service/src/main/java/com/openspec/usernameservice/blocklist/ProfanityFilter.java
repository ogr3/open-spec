package com.openspec.usernameservice.blocklist;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ProfanityFilter {

    private final BlocklistLoader blocklistLoader;

    public ProfanityFilter(BlocklistLoader blocklistLoader) {
        this.blocklistLoader = blocklistLoader;
    }

    public boolean isBlocked(String handle) {
        if (handle == null || handle.isBlank()) {
            return false;
        }
        return blocklistLoader.getEntries().contains(handle.toUpperCase(Locale.ROOT));
    }

    public Optional<String> firstAllowed(Stream<String> candidates) {
        if (candidates == null) {
            return Optional.empty();
        }
        return candidates
                .filter(candidate -> candidate != null && !isBlocked(candidate))
                .findFirst();
    }
}
