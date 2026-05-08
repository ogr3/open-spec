package com.openspec.usernameservice.blocklist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlocklistToolTest {

    @TempDir
    Path tempDir;

    @Test
    void createsAndNormalizesBlocklistFileWithAdditions() throws IOException {
        Path blocklist = tempDir.resolve("data").resolve("blocklist.json");

        BlocklistTool.main(new String[] {"--file", blocklist.toString(), "--add", "kuk", "--add", "Åäö"});

        String content = Files.readString(blocklist);
        assertThat(content).contains("\"KUK\"");
        assertThat(content).contains("\"ÅÄÖ\"");
    }

    @Test
    void rejectsUnknownArgument() {
        assertThatThrownBy(() -> BlocklistTool.main(new String[] {"--unknown"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown argument: --unknown");
    }

    @Test
    void rejectsAddWithoutValue() {
        assertThatThrownBy(() -> BlocklistTool.main(new String[] {"--add"}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("--add requires a trigram");
    }

    @Test
    void createsFileWhenPathHasNoParentDirectory() throws IOException {
        Path fileNameOnly = Path.of("blocklist-" + UUID.randomUUID() + ".json");
        try {
            BlocklistTool.main(new String[] {"--file", fileNameOnly.toString(), "--add", "  ala  "});
            assertThat(Files.exists(fileNameOnly)).isTrue();
            assertThat(Files.readString(fileNameOnly)).contains("\"ALA\"");
        } finally {
            Files.deleteIfExists(fileNameOnly);
        }
    }
}
