package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UndoTests {

    private URI uri1;
    private String data1 = "She sells seashells by the seashore. Google is a search engine.";

    private URI uri2;
    private String data2 = "Facebook is where you can see pictures posted by your friends. It is like Instagram and is cool.";

    private URI uri3;
    private String data3 = "Twitter is fun. Is it a bird that says \"tweet?\" No! It is social media.";

    //method to create the uris
    @BeforeEach
    public void uris() throws URISyntaxException {
        this.uri1 = new URI("http://google.com");
        this.uri2 = new URI("http://facebook.com");
        this.uri3 = new URI("http://twitter.com");
    }

    //method to create a document store and put one document
    private DocumentStoreImpl docStorePutOne() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        InputStream inputStream = new ByteArrayInputStream(this.data1.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);

        return store;
    }

    //method to create a document store and put 2 documents
    private DocumentStoreImpl docStorePutTwo() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        InputStream inputStream1 = new ByteArrayInputStream(this.data1.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream1, this.uri1, DocumentStore.DocumentFormat.TXT);

        InputStream inputStream2 = new ByteArrayInputStream(this.data2.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream2, this.uri2, DocumentStore.DocumentFormat.TXT);

        return store;
    }

    //method to create a document store and put 3 documents
    private DocumentStoreImpl docStorePutThree() throws IOException {
        DocumentStoreImpl store = new DocumentStoreImpl();

        InputStream inputStream1 = new ByteArrayInputStream(this.data1.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream1, this.uri1, DocumentStore.DocumentFormat.TXT);

        InputStream inputStream2 = new ByteArrayInputStream(this.data2.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream2, this.uri2, DocumentStore.DocumentFormat.TXT);

        InputStream inputStream3 = new ByteArrayInputStream(this.data3.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream3, this.uri3, DocumentStore.DocumentFormat.TXT);

        return store;
    }

    //test undo when empty stack
    @Test
    public void undoEmpty() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        assertThrows(IllegalStateException.class, () -> store.undo(), "Should have thrown IllegalStateException on empty stack");
    }

    //test undo(uri) when empty stack
    @Test
    public void undoEmpty2() {
        DocumentStoreImpl store = new DocumentStoreImpl();
        assertThrows(IllegalStateException.class, () -> store.undo(this.uri1), "Should have thrown IllegalStateException on empty stack");
    }

    //test undo on one new put
    @Test
    public void undoOnePut() throws IOException {
        DocumentStoreImpl store = docStorePutOne();
        store.undo();
        assertNull(store.get(this.uri1), "Should have been null after undo");
    }

    //test undo on two new puts
    @Test
    public void undoTwoPuts() throws IOException {
        DocumentStoreImpl store = docStorePutTwo();

        List<Document> expected = new ArrayList<>();
        expected.add(store.get(this.uri2));
        expected.add(store.get(this.uri1));

        assertEquals(expected, store.search("is"));

        store.undo();
        List<Document> newExpected = new ArrayList<>();
        newExpected.add(store.get(this.uri1));

        assertEquals(newExpected, store.search("is"));
        assertNull(store.get(this.uri2));
        assertNotNull(store.get(this.uri1));

        store.undo();

        assertNull(store.get(this.uri2));
    }

    //test undo(uri) on three new puts
    @Test
    public void undoUriThreePuts() throws IOException {
        DocumentStoreImpl store = docStorePutThree();

        List<Document> expected = new ArrayList<>();
        expected.add(store.get(this.uri3));
        expected.add(store.get(this.uri1));

        store.undo(this.uri2);

        assertNull(store.get(this.uri2), "uri2 should have been null after undo");
        assertNotNull(store.get(this.uri1), "uri1 should have been unaffected by undo call to uri2");
        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt());
        assertNotNull(store.get(this.uri3), "uri3 should have been unaffected by undo call to uri2");
        assertEquals(this.data3, store.get(this.uri3).getDocumentTxt());
        assertEquals(expected, store.search("is"));

        store.undo(this.uri1);

        assertNull(store.get(this.uri1), "uri1 should have been null after undo");
        assertNotNull(store.get(this.uri3), "uri3 should have been unaffected by undo call to uri1");

        store.undo();

        assertNull(store.get(this.uri3), "uri3 should have been null after undo");
    }

    //test undo on one delete
    @Test
    public void undoOneDelete() throws IOException {
        DocumentStoreImpl store = docStorePutOne();

        store.delete(this.uri1);

        assertNull(store.get(this.uri1));

        store.undo();

        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt());
    }

    //test undo(uri) on two deletes
    @Test
    public void undoUriTwoDeletes() throws IOException {
        DocumentStoreImpl store = docStorePutTwo();

        store.delete(this.uri1);

        assertNull(store.get(this.uri1), "uri1 should be null after delete");
        assertNotNull(store.get(this.uri2), "uri2 should not be null after deleting uri1");

        store.delete(this.uri2);
        store.undo(this.uri1);

        assertNotNull(store.get(this.uri1));
        assertNull(store.get(this.uri2));

        store.undo(this.uri2);

        assertNotNull(store.get(this.uri2));
        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt(), "after delete to uri1 was undone it should contain the same data");
    }

    //test undo on put override
    @Test
    public void undoPutOverride() throws IOException {
        DocumentStoreImpl store = docStorePutOne();

        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt());

        InputStream inputStream = new ByteArrayInputStream(this.data3.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);

        assertEquals(this.data3, store.get(this.uri1).getDocumentTxt());

        store.undo();

        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt());
    }

    //test undo(uri) on put override
    @Test
    public void undoUriPutOverride() throws IOException {
        DocumentStoreImpl store = docStorePutThree();

        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt());
        assertEquals(this.data2, store.get(this.uri2).getDocumentTxt());
        assertEquals(this.data3, store.get(this.uri3).getDocumentTxt());

        String newString = "new data";
        InputStream inputStream = new ByteArrayInputStream(newString.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream, this.uri1, DocumentStore.DocumentFormat.TXT);

        assertEquals(newString, store.get(this.uri1).getDocumentTxt());

        store.undo(this.uri1);

        assertEquals(this.data1, store.get(this.uri1).getDocumentTxt());
    }

    //Test undo(uri) after deleteAll(keyword)
    @Test
    public void undoDeleteAll() throws IOException{
        DocumentStoreImpl store = docStorePutThree();

        Set<URI> deleted = new HashSet<>();
        deleted.add(uri1);
        deleted.add(uri2);

        assertEquals(deleted, store.deleteAll("by"));
        assertNull(store.get(uri1));
        assertNull(store.get(uri2));

        store.undo(uri1);

        List<Document> expected = new ArrayList<>();
        expected.add(store.get(this.uri3));
        expected.add(store.get(this.uri1));

        assertEquals(data1, store.get(uri1).getDocumentTxt());
        assertNull(store.get(uri2));
        assertEquals(expected, store.search("is"));

        store.undo(uri2);
        assertEquals(data2, store.get(uri2).getDocumentTxt());
    }

    //Test undo(uri) after deleteAllWithPrefix(keywordPrefix)
    @Test
    public void undoDeleteAllWithPrefix() throws IOException{
        DocumentStoreImpl store = docStorePutThree();

        Set<URI> deleted = new HashSet<>();
        deleted.add(uri1);
        deleted.add(uri2);

        List<Document> list = new ArrayList<>();
        list.add(store.get(this.uri3));

        assertEquals(deleted, store.deleteAllWithPrefix("se"));
        assertNull(store.get(uri1));
        assertNull(store.get(uri2));
        assertEquals(list, store.search("is"));

        store.undo(uri1);

        List<Document> expected = new ArrayList<>();
        expected.add(store.get(this.uri3));
        expected.add(store.get(this.uri1));

        List<Document> list2 = new ArrayList<>();
        list2.add(store.get(this.uri1));

        assertEquals(data1, store.get(uri1).getDocumentTxt());
        assertNull(store.get(uri2));
        assertEquals(list2, store.searchByPrefix("se"));
        assertEquals(expected, store.search("is"));

        store.undo(uri2);

        List<Document> expected2 = new ArrayList<>();
        expected2.add(store.get(this.uri1));
        expected2.add(store.get(this.uri2));

        List<Document> expected3 = new ArrayList<>();
        expected3.add(store.get(this.uri2));
        expected3.add(store.get(this.uri3));
        expected3.add(store.get(this.uri1));

        assertEquals(data2, store.get(uri2).getDocumentTxt());
        assertEquals(expected3, store.search("is"));
    }
}