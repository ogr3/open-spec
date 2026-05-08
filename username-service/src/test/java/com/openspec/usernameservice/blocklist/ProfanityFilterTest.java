package com.openspec.usernameservice.blocklist;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class ProfanityFilterTest {

    private ProfanityFilter profanityFilter;

    @BeforeEach
    void setUp() {
        BlocklistLoader loader = new BlocklistLoader(new DefaultResourceLoader(), "classpath:test-blocklist.json");
        loader.load();
        profanityFilter = new ProfanityFilter(loader);
    }

    @Test
    void matchesBlockedEntriesRegardlessOfCase() {
        assertThat(profanityFilter.isBlocked("kuk")).isTrue();
        assertThat(profanityFilter.isBlocked("Kuk")).isTrue();
        assertThat(profanityFilter.isBlocked("BOB")).isFalse();
    }

    @Test
    void findsFirstAllowedCandidate() {
        Optional<String> result = profanityFilter.firstAllowed(Stream.of("KUK", "FAN", "ALA", "ÅÄÖ"));

        assertThat(result).contains("ALA");
    }

    @Test
    void returnsEmptyWhenAllCandidatesBlocked() {
        Optional<String> result = profanityFilter.firstAllowed(Stream.of("KUK", "FAN", "ÅÄÖ"));

        assertThat(result).isEmpty();
    }
}
