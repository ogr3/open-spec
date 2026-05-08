package com.openspec.usernameservice.blocklist;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public final class BlocklistTool {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    public static void main(String[] args) throws IOException {
        Options options = Options.parse(args);
        Path file = options.file();
        if (!Files.exists(file)) {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, "[]\n");
        }

        List<String> current = OBJECT_MAPPER.readValue(Files.newInputStream(file), new TypeReference<>() {});
        Set<String> normalized = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String entry : current) {
            if (entry != null && !entry.isBlank()) {
                normalized.add(entry.trim().toUpperCase(Locale.ROOT));
            }
        }

        if (!options.additions().isEmpty()) {
            options.additions().stream()
                    .map(String::trim)
                    .filter(value -> !value.isBlank())
                    .map(value -> value.toUpperCase(Locale.ROOT))
                    .forEach(normalized::add);
            System.out.printf("Added %d entries%n", options.additions().size());
        }

        Files.writeString(file, OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(normalized));

        if (options.list()) {
            normalized.stream().sorted(Comparator.naturalOrder()).forEach(System.out::println);
        }
    }

    private record Options(Path file, List<String> additions, boolean list) {

        static Options parse(String[] args) {
            Path file = Paths.get("src/main/resources/blocklist-sv.json");
            List<String> additions = new ArrayList<>();
            boolean list = false;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                switch (arg) {
                    case "--file" -> {
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("--file requires a path");
                        }
                        file = Paths.get(args[++i]);
                    }
                    case "--add" -> {
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("--add requires a trigram");
                        }
                        additions.add(args[++i]);
                    }
                    case "--list" -> list = true;
                    default -> throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }

            return new Options(file, additions, list);
        }
    }
}
