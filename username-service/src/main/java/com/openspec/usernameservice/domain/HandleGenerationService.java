package com.openspec.usernameservice.domain;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class HandleGenerationService {

    private static final int HANDLE_LENGTH = 3;
    private static final int MAX_SUFFIX = 9;

    public Stream<String> generateCandidates(final @NonNull String email) {
        Objects.requireNonNull(email, "email must not be null");
        final int atIndex = email.indexOf('@');
        if (atIndex < 1) {
            throw new IllegalArgumentException("email must contain mailbox and domain");
        }

        final String mailbox = email.substring(0, atIndex);
        final List<LetterPosition> letters = extractLetters(mailbox);

        final List<String> trigrams = buildTrigramSequence(mailbox, letters);
        final String fallback = trigrams.isEmpty() ? generatePaddedTrigram(List.of()) : trigrams.get(trigrams.size() - 1);

        final Stream<String> suffixes = fallback.isEmpty()
                ? Stream.empty()
                : IntStream.rangeClosed(1, MAX_SUFFIX).mapToObj(i -> fallback + i);

        return Stream.concat(trigrams.stream(), suffixes);
    }

    private List<LetterPosition> extractLetters(final @NonNull String mailbox) {
        Objects.requireNonNull(mailbox, "mailbox must not be null");

        return IntStream.range(0, mailbox.length())
                .mapToObj(i -> new LetterPosition(mailbox.charAt(i), i))
                .filter(lp -> Character.isLetter(lp.letter()))
                .map(lp -> new LetterPosition(
                        Character.toString(lp.letter()).toUpperCase(Locale.ROOT).charAt(0),
                        lp.index()))
                .toList();
    }

    private List<String> buildTrigramSequence(final @NonNull String mailbox, final @NonNull List<LetterPosition> letters) {
        Objects.requireNonNull(mailbox, "mailbox must not be null");
        Objects.requireNonNull(letters, "letters must not be null");

        final Set<String> ordered = new LinkedHashSet<>();
        dotPreferredTrigram(mailbox, letters).ifPresent(ordered::add);

        if (letters.size() >= HANDLE_LENGTH) {
            IntStream.rangeClosed(0, letters.size() - HANDLE_LENGTH)
                    .mapToObj(i -> letters.subList(i, i + HANDLE_LENGTH).stream()
                            .map(lp -> String.valueOf(lp.letter()))
                            .collect(Collectors.joining()))
                    .forEach(ordered::add);
        } else if (!letters.isEmpty()) {
            ordered.add(generatePaddedTrigram(
                    letters.stream().map(LetterPosition::letter).toList()));
        }

        if (ordered.isEmpty()) {
            ordered.add(generatePaddedTrigram(List.of()));
        }

        return ordered.stream().toList();
    }

    private Optional<String> dotPreferredTrigram(final @NonNull String mailbox, final @NonNull List<LetterPosition> letters) {
        Objects.requireNonNull(mailbox, "mailbox must not be null");
        Objects.requireNonNull(letters, "letters must not be null");

        final int dotIndex = mailbox.indexOf('.');
        if (dotIndex < 0 || letters.isEmpty()) {
            return Optional.empty();
        }

        final Optional<LetterPosition> beforeDot =
                letters.stream().filter(lp -> lp.index() < dotIndex).findFirst();

        if (beforeDot.isEmpty()) {
            return Optional.empty();
        }

        final LetterPosition anchor = beforeDot.orElseThrow();
        final List<LetterPosition> selectedAfterDot = letters.stream()
                .filter(lp -> lp.index() > dotIndex)
                .limit(HANDLE_LENGTH - 1L)
                .toList();

        final Set<Integer> usedIndexes = Stream.concat(
                        Stream.of(anchor.index()),
                        selectedAfterDot.stream().map(LetterPosition::index))
                .collect(Collectors.toSet());

        final List<Character> result = Stream.concat(
                        Stream.concat(
                                Stream.of(anchor.letter()),
                                selectedAfterDot.stream().map(LetterPosition::letter)),
                        letters.stream()
                                .filter(lp -> !usedIndexes.contains(lp.index()))
                                .map(LetterPosition::letter))
                .limit(HANDLE_LENGTH)
                .toList();

        return Optional.of(generatePaddedTrigram(result));
    }

    private String generatePaddedTrigram(final @NonNull List<Character> source) {
        Objects.requireNonNull(source, "source must not be null");

        return IntStream.range(0, HANDLE_LENGTH)
                .mapToObj(i -> i < source.size() ? source.get(i) : 'X')
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private record LetterPosition(char letter, int index) {}
}
