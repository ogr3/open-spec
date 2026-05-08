package com.openspec.usernameservice.blocklist;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

class BlocklistLoaderTest {

    @Test
    void loadsAndNormalizesEntries() {
        BlocklistLoader loader = new BlocklistLoader(new DefaultResourceLoader(), "classpath:test-blocklist.json");
        loader.load();

        assertThat(loader.getEntries()).containsExactly("KUK", "FAN", "ÅÄÖ");
    }
}
