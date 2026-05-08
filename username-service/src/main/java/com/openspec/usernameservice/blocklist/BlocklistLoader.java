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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
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
            final @NonNull ResourceLoader resourceLoader,
            @Value("${handles.blocklist.location:classpath:blocklist-sv.json}") final @NonNull String location) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader must not be null");
        this.location = Objects.requireNonNull(location, "location must not be null");
    }

    @PostConstruct
    public void load() {
        final Resource resource = resourceLoader.getResource(location);
        this.entries = Collections.unmodifiableSet(readEntries(resource));
    }

    public Set<String> getEntries() {
        return entries;
    }

    private Set<String> readEntries(final @NonNull Resource resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        try (InputStream inputStream = resource.getInputStream()) {
            final List<String> raw = objectMapper.readValue(inputStream, new TypeReference<>() {});
            return raw.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(entry -> !entry.isEmpty())
                    .map(entry -> entry.toUpperCase(Locale.ROOT))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load blocklist from " + location, ex);
        }
    }
}
