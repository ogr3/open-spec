package com.openspec.usernameservice.blocklist;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class ProfanityFilter {

    private final BlocklistLoader blocklistLoader;

    public ProfanityFilter(final @NonNull BlocklistLoader blocklistLoader) {
        this.blocklistLoader = blocklistLoader;
    }

    public boolean isBlocked(final @NonNull String handle) {
        Objects.requireNonNull(handle, "handle must not be null");
        return !handle.isBlank() && blocklistLoader.getEntries().contains(handle.toUpperCase(Locale.ROOT));
    }

    public Optional<String> firstAllowed(final @NonNull Stream<String> candidates) {
        Objects.requireNonNull(candidates, "candidates must not be null");
        return candidates
                .filter(Objects::nonNull)
                .filter(candidate -> !isBlocked(candidate))
                .findFirst();
    }
}
