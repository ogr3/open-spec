package com.openspec.usernameservice.blocklist;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class BlocklistLoader {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String location;

    private Set<String> entries = Collections.emptySet();

    public BlocklistLoader(
            ResourceLoader resourceLoader,
            @Value("${handles.blocklist.location:classpath:blocklist-sv.json}") String location) {
        this.resourceLoader = resourceLoader;
        this.location = location;
    }

    @PostConstruct
    public void load() {
        Resource resource = resourceLoader.getResource(location);
        this.entries = Collections.unmodifiableSet(readEntries(resource));
    }

    public Set<String> getEntries() {
        return entries;
    }

    private Set<String> readEntries(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            List<String> raw = objectMapper.readValue(inputStream, new TypeReference<>() {});
            Set<String> normalized = new LinkedHashSet<>();
            for (String entry : raw) {
                if (entry == null || entry.trim().isEmpty()) {
                    continue;
                }
                normalized.add(entry.trim().toUpperCase(Locale.ROOT));
            }
            return normalized;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load blocklist from " + location, ex);
        }
    }
}
