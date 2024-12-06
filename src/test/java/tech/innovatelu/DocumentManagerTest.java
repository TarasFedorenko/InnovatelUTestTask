package tech.innovatelu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void testSaveDocumentWithGeneratedId() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Document")
                .content("Test Content")
                .author(DocumentManager.Author.builder().id("author1").name("Author Name").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId(), "Document ID should be generated");
        assertEquals(document.getTitle(), savedDocument.getTitle(), "Titles should match");
        assertEquals(document.getContent(), savedDocument.getContent(), "Content should match");
    }

    @Test
    void testSaveDocumentWithExistingId() {
        String existingId = UUID.randomUUID().toString();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(existingId)
                .title("Existing Document")
                .content("Existing Content")
                .author(DocumentManager.Author.builder().id("author2").name("Another Author").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertEquals(existingId, savedDocument.getId(), "Document ID should not be changed");
    }

    @Test
    void testFindByIdWhenDocumentExists() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Searchable Document")
                .content("Searchable Content")
                .author(DocumentManager.Author.builder().id("author3").name("Search Author").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        Optional<DocumentManager.Document> foundDocument = documentManager.findById(savedDocument.getId());

        assertTrue(foundDocument.isPresent(), "Document should be found");
        assertEquals(savedDocument, foundDocument.get(), "Found document should match the saved document");
    }

    @Test
    void testFindByIdWhenDocumentDoesNotExist() {
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(UUID.randomUUID().toString());

        assertTrue(foundDocument.isEmpty(), "Document should not be found");
    }

    @Test
    void testSearchByTitlePrefixes() {
        DocumentManager.Document doc1 = documentManager.save(DocumentManager.Document.builder()
                .title("First Document")
                .content("Content 1")
                .author(DocumentManager.Author.builder().id("author1").name("Author 1").build())
                .created(Instant.now())
                .build());
        DocumentManager.Document doc2 = documentManager.save(DocumentManager.Document.builder()
                .title("Second Document")
                .content("Content 2")
                .author(DocumentManager.Author.builder().id("author2").name("Author 2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("First"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match");
        assertEquals(doc1, results.get(0), "The first document should match");
    }

    @Test
    void testSearchByAuthorIds() {
        DocumentManager.Document doc1 = documentManager.save(DocumentManager.Document.builder()
                .title("Document 1")
                .content("Content 1")
                .author(DocumentManager.Author.builder().id("author1").name("Author 1").build())
                .created(Instant.now())
                .build());
        DocumentManager.Document doc2 = documentManager.save(DocumentManager.Document.builder()
                .title("Document 2")
                .content("Content 2")
                .author(DocumentManager.Author.builder().id("author2").name("Author 2").build())
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("author1"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(1, results.size(), "Only one document should match");
        assertEquals(doc1, results.get(0), "The first document should match");
    }

    @Test
    void testSearchByCreatedRange() {
        Instant now = Instant.now();
        DocumentManager.Document doc1 = documentManager.save(DocumentManager.Document.builder()
                .title("Document 1")
                .content("Content 1")
                .author(DocumentManager.Author.builder().id("author1").name("Author 1").build())
                .created(now.minusSeconds(3600))
                .build());
        DocumentManager.Document doc2 = documentManager.save(DocumentManager.Document.builder()
                .title("Document 2")
                .content("Content 2")
                .author(DocumentManager.Author.builder().id("author2").name("Author 2").build())
                .created(now.plusSeconds(3600))
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minusSeconds(1800))
                .createdTo(now.plusSeconds(1800))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(0, results.size(), "No documents should match the range");    }
}
