package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.stage5.*;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static edu.yu.cs.com1320.project.stage5.DocumentStore.DocumentFormat.*;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentStoreTest {

    private URI uri1;
    private URI uri2;
    private URI uri3;
    private URI uri4;

    public void uris() {
        this.uri1 = URI.create("http://www.google.com/she/sells/seashells");
        this.uri2 = URI.create("http://www.youtube.com/homepage/seals");
        this.uri3 = URI.create("http://www.facebook.com/pictures/cool/posts");
        this.uri4 = URI.create("http://www.twitter.com/fun/tweet/bird/media");
    }

    public DocumentStoreImpl putDocs() throws IOException {
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        uris();

        String inputString1 = "She sells seashells by the seashore. Google is a search engine.";
        InputStream inputStream1 = new ByteArrayInputStream(inputString1.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream1, this.uri1, TXT);

        String inputString2 = "Homepage for youtube. This website is home to thousands (1000s) of videos, including videos of the sea and seals.";
        InputStream inputStream2 = new ByteArrayInputStream(inputString2.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream2, this.uri2, TXT);

        String inputString3 = "Facebook is where you can see pictures posted by your friends. It is like Instagram and is cool.";
        InputStream inputStream3 = new ByteArrayInputStream(inputString3.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream3, this.uri3, TXT);

        String inputString4 = "Twitter is fun. Is it a bird that says \"tweet?\" No! It is social media.";
        InputStream inputStream4 = new ByteArrayInputStream(inputString4.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream4, this.uri4, TXT);

        return docStore;
    }

    //Test put with no previous doc
    @Test
    public void putNoDoc() throws IOException {
        URI uri = URI.create("http://www.google.com");
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        String inputString = "Google";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
        assertEquals(0, docStore.put(inputStream, uri, TXT));
    }

    //Test put with a previous doc at the given uri
    @Test
    public void putDoc() throws IOException {
        URI uri1 = URI.create("http://www.google.com");
        URI uri2 = URI.create("http://www.google.com");
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        String inputString = "Google";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream, uri1, TXT);
        byte[] binaryData = {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64, 0x21};
        InputStream binaryInputStream = new ByteArrayInputStream(binaryData);
        assertEquals(docStore.get(uri1).hashCode(), docStore.put(binaryInputStream, uri2, BINARY));
    }

    //Test deleting a doc with null InputStream
    @Test
    public void deleteWithNull() throws IOException {
        URI uri = URI.create("http://www.google.com");
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        String inputString = "Google";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream, uri, TXT);
        assertEquals(docStore.get(uri).hashCode(), docStore.put(null, uri, TXT));
    }

    //Test delete method
    @Test
    public void delete() throws IOException {
        URI uri = URI.create("http://www.google.com");
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        String inputString = "Google";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream, uri, TXT);
        assertTrue(docStore.delete(uri));
        assertFalse(docStore.delete(uri));
    }

    //Test getting nonexistent and deleted uri
    @Test
    public void getNonexistent() throws IOException {
        URI uri = URI.create("http://www.google.com");
        DocumentStoreImpl docStore = new DocumentStoreImpl();
        assertNull(docStore.get(uri));
        String inputString = "Google";
        InputStream inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
        docStore.put(inputStream, uri, TXT);
        assertTrue(docStore.delete(uri));
        assertNull(docStore.get(uri));
    }

    //Test search method
    @Test
    public void searchTest() throws IOException {
        DocumentStoreImpl store = putDocs();

        List<Document> expected = new ArrayList<>();
        expected.add(store.get(this.uri3));
        expected.add(store.get(this.uri4));
        expected.add(store.get(this.uri1));
        expected.add(store.get(this.uri2));

        assertEquals(expected, store.search("is"));
    }

    //Test search method on a nonexistent word
    @Test
    public void searchNonexistent() throws IOException {
        DocumentStoreImpl store = putDocs();
        assertEquals(new ArrayList<>(), store.search("hello"));
    }

    //Test deleteAll method
    @Test
    public void deleteAllTest() throws IOException {
        DocumentStoreImpl store = putDocs();
        store.deleteAll("the");

        assertEquals(new ArrayList<>(), store.search("the"));
        assertNull(store.get(this.uri1));
        assertNull(store.get(this.uri2));
        assertNotNull(store.get(this.uri3));
        assertNotNull(store.get(this.uri4));
    }

    @Test
    public void deleteAllWithPrefixTest() throws IOException {

        DocumentStore store = new DocumentStoreImpl();

        String inputString1 = "Hello, World! Welcome to my website. I hope you enjoy your stay.";
        URI uri1 = URI.create("http://www.helloworld.com");
        InputStream inputStream1 = new ByteArrayInputStream(inputString1.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream1, uri1, TXT);

        String inputString2 = "Goodbye, friends. I hope to see you again soon. It was so nice to meet you.";
        URI uri2 = URI.create("http://www.goodbyeworld.com");
        InputStream inputStream2 = new ByteArrayInputStream(inputString2.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream2, uri2, TXT);

        String inputString3 = "Testing testing. This is a test.";
        URI uri3 = URI.create("http://www.testing.com");
        InputStream inputStream3 = new ByteArrayInputStream(inputString3.getBytes(StandardCharsets.UTF_8));
        store.put(inputStream3, uri3, TXT);

        assertEquals(uri1, store.get(uri1).getKey());
        assertEquals(uri2, store.get(uri2).getKey());
        assertEquals(uri3, store.get(uri3).getKey());
        assertEquals(inputString1, store.get(uri1).getDocumentTxt());
        assertEquals(inputString2, store.get(uri2).getDocumentTxt());
        assertEquals(inputString3, store.get(uri3).getDocumentTxt());


        store.deleteAllWithPrefix("ho");
        assertNull(store.get(uri1));
        assertNull(store.get(uri2));
        assertNotNull(store.get(uri3));
    }

    @AfterAll
    public static void cleanUp(){
        System.out.println("After All cleanUp() method called");
    }
}
