package com.openspec.usernameservice.domain;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class HandleGenerationService {

    private static final int HANDLE_LENGTH = 3;
    private static final int MAX_SUFFIX = 9;

    public Stream<String> generateCandidates(String email) {
        Objects.requireNonNull(email, "email must not be null");
        int atIndex = email.indexOf('@');
        if (atIndex < 1) {
            throw new IllegalArgumentException("email must contain mailbox and domain");
        }

        String mailbox = email.substring(0, atIndex);
        List<LetterPosition> letters = extractLetters(mailbox);

        List<String> trigrams = buildTrigramSequence(mailbox, letters);
        String fallback = trigrams.isEmpty() ? generatePaddedTrigram(List.of()) : trigrams.get(trigrams.size() - 1);

        Stream<String> suffixes = Stream.empty();
        if (!fallback.isEmpty()) {
            suffixes = Stream.iterate(1, n -> n + 1)
                    .limit(MAX_SUFFIX)
                    .map(i -> fallback + i);
        }

        return Stream.concat(trigrams.stream(), suffixes);
    }

    private List<LetterPosition> extractLetters(String mailbox) {
        List<LetterPosition> letters = new ArrayList<>();
        for (int i = 0; i < mailbox.length(); i++) {
            char raw = mailbox.charAt(i);
            if (Character.isLetter(raw)) {
                letters.add(new LetterPosition(Character.toString(raw).toUpperCase(Locale.ROOT).charAt(0), i));
            }
        }
        return letters;
    }

    private List<String> buildTrigramSequence(String mailbox, List<LetterPosition> letters) {
        Set<String> ordered = new LinkedHashSet<>();
        dotPreferredTrigram(mailbox, letters).ifPresent(ordered::add);

        if (letters.size() >= HANDLE_LENGTH) {
            for (int i = 0; i <= letters.size() - HANDLE_LENGTH; i++) {
                String trigram = letters.subList(i, i + HANDLE_LENGTH)
                        .stream()
                        .map(lp -> String.valueOf(lp.letter()))
                        .collect(Collectors.joining());
                ordered.add(trigram);
            }
        } else if (!letters.isEmpty()) {
            ordered.add(generatePaddedTrigram(letters.stream().map(LetterPosition::letter).toList()));
        }

        if (ordered.isEmpty()) {
            ordered.add(generatePaddedTrigram(List.of()));
        }

        return new ArrayList<>(ordered);
    }

    private Optional<String> dotPreferredTrigram(String mailbox, List<LetterPosition> letters) {
        int dotIndex = mailbox.indexOf('.');
        if (dotIndex < 0 || letters.isEmpty()) {
            return Optional.empty();
        }

        Optional<LetterPosition> beforeDot = letters.stream()
                .filter(lp -> lp.index() < dotIndex)
                .findFirst();

        if (beforeDot.isEmpty()) {
            return Optional.empty();
        }

        List<Character> result = new ArrayList<>();
        List<Integer> usedIndexes = new ArrayList<>();
        result.add(beforeDot.get().letter());
        usedIndexes.add(beforeDot.get().index());

        for (LetterPosition lp : letters) {
            if (lp.index() > dotIndex && !usedIndexes.contains(lp.index())) {
                result.add(lp.letter());
                usedIndexes.add(lp.index());
                if (result.size() == HANDLE_LENGTH) {
                    break;
                }
            }
        }

        if (result.size() < HANDLE_LENGTH) {
            for (LetterPosition lp : letters) {
                if (usedIndexes.contains(lp.index())) {
                    continue;
                }
                result.add(lp.letter());
                usedIndexes.add(lp.index());
                if (result.size() == HANDLE_LENGTH) {
                    break;
                }
            }
        }

        while (result.size() < HANDLE_LENGTH) {
            result.add('X');
        }

        return Optional.of(result.stream()
                .limit(HANDLE_LENGTH)
                .map(String::valueOf)
                .collect(Collectors.joining()));
    }

    private String generatePaddedTrigram(List<Character> source) {
        List<Character> padded = new ArrayList<>(source);
        while (padded.size() < HANDLE_LENGTH) {
            padded.add('X');
        }
        return padded.stream()
                .limit(HANDLE_LENGTH)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private record LetterPosition(char letter, int index) {
    }
}
