package tech.innovatelu;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> documentStorage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }
        documentStorage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documentStorage.values().stream()
                .filter(doc -> matchesTitlePrefixes(doc, request.getTitlePrefixes()))
                .filter(doc -> matchesContentContains(doc, request.getContainsContents()))
                .filter(doc -> matchesAuthorIds(doc, request.getAuthorIds()))
                .filter(doc -> matchesCreatedRange(doc, request.getCreatedFrom(), request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStorage.get(id));
    }
    private boolean matchesTitlePrefixes(Document document, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) {
            return true;
        }
        for (String prefix : prefixes) {
            if (document.getTitle() != null && document.getTitle().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesContentContains(Document document, List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return true;
        }
        for (String content : contents) {
            if (document.getContent() != null && document.getContent().contains(content)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesAuthorIds(Document document, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return true;
        }
        return authorIds.contains(document.getAuthor() != null ? document.getAuthor().getId() : null);
    }

    private boolean matchesCreatedRange(Document document, Instant from, Instant to) {
        Instant created = document.getCreated();
        if (from != null && (created == null || created.isBefore(from))) {
            return false;
        }
        if (to != null && (created == null || created.isAfter(to))) {
            return false;
        }
        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}