package com.openspec.usernameservice.blocklist;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

class BlocklistLoaderTest {

    @Test
    void loadsAndNormalizesEntries() {
        BlocklistLoader loader = new BlocklistLoader(new DefaultResourceLoader(), "classpath:test-blocklist.json");
        loader.load();

        assertThat(loader.getEntries()).containsExactly("KUK", "FAN", "ÅÄÖ");
    }
}
