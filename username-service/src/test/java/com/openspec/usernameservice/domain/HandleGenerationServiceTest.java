package com.openspec.usernameservice.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HandleGenerationServiceTest {

    private final HandleGenerationService service = new HandleGenerationService();

    @Test
    void prefersDotSeparatedWindow() {
        List<String> candidates = service.generateCandidates("anna.larsson@example.se").limit(3).collect(Collectors.toList());

        assertThat(candidates.get(0)).isEqualTo("ALA");
    }

    @Test
    void dotStrategyUsesFirstSegmentInitial() {
        List<String> candidates = service.generateCandidates("user.name@example.com").limit(1).collect(Collectors.toList());

        assertThat(candidates).containsExactly("UNA");
    }

    @Test
    void fallsBackToFirstLettersWhenNoDot() {
        List<String> candidates = service.generateCandidates("emil@example.se").limit(1).collect(Collectors.toList());

        assertThat(candidates).containsExactly("EMI");
    }

    @Test
    void skipsNonLettersAndUppercases() {
        List<String> candidates = service.generateCandidates("bo.b-å@example.se").limit(1).collect(Collectors.toList());

        assertThat(candidates).containsExactly("BBÅ");
    }

    @Test
    void padsWithXWhenMailboxTooShort() {
        List<String> candidates = service.generateCandidates("q@example.se").limit(1).collect(Collectors.toList());

        assertThat(candidates).containsExactly("QXX");
    }

    @Test
    void emitsNumericSuffixesAfterBaseTrigram() {
        List<String> candidates = service.generateCandidates("abc@example.se").limit(5).collect(Collectors.toList());

        assertThat(candidates).containsSequence("ABC", "ABC1", "ABC2");
    }
}
