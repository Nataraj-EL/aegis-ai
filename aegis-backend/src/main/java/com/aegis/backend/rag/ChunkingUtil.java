package com.aegis.backend.rag;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ChunkingUtil {

    public ChunkingUtil() {
        // Default constructor
    }

    public List<String> splitIntoChunks(final String text, final int chunkSize, final int overlap) {
        final List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            final int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) {
                break;
            }
            start += chunkSize - overlap;
        }
        return chunks;
    }
}
