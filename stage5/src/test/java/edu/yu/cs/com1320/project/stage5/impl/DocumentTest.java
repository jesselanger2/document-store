package edu.yu.cs.com1320.project.stage5.impl;

import org.junit.Test;

import java.net.URI;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentTest {

    //Test constructor with null URI
    @Test
    public void nullURI(){
        URI uri = URI.create("http://www.google.com");
        assertThrows(IllegalArgumentException.class, () -> {
            DocumentImpl newDoc = new DocumentImpl(null, "google", null);
        });
    }

    //Test getDocumentText() method
    @Test
    public void getTxt(){
        URI uri = URI.create("http://www.google.com");
        DocumentImpl newDoc = new DocumentImpl(uri, "google", null);
        assertEquals("google", newDoc.getDocumentTxt());
    }

    //Test getDocumentTxt() method with on document created with binary data
    @Test
    public void nullText(){
        URI uri = URI.create("http://www.google.com");
        byte[] binaryData = {(byte) 0b10011010, (byte) 0b01100100, (byte) 0b00011101, (byte) 0b11010101, (byte) 0b00110100,
                (byte) 0b10100010, (byte) 0b11100011, (byte) 0b01001000, (byte) 0b01110101, (byte) 0b00001010};
        DocumentImpl newDoc = new DocumentImpl(uri, binaryData);
        assertNull(newDoc.getDocumentTxt());
    }

    //Test getDocumentBinaryData() method
    @Test
    public void getBinaryData(){
        URI uri = URI.create("http://www.google.com");
        byte[] binaryData = {(byte) 0b10011010, (byte) 0b01100100, (byte) 0b00011101, (byte) 0b11010101, (byte) 0b00110100,
                (byte) 0b10100010, (byte) 0b11100011, (byte) 0b01001000, (byte) 0b01110101, (byte) 0b00001010};
        DocumentImpl newDoc = new DocumentImpl(uri, binaryData);
        assertArrayEquals(binaryData, newDoc.getDocumentBinaryData());
    }

    //Test equals()
    @Test
    public void testEquals(){
        URI uri1 = URI.create("http://www.google.com");
        URI uri2 = URI.create("http://www.google.com");
        DocumentImpl newDoc1 = new DocumentImpl(uri1, "google", null);
        DocumentImpl newDoc2 = new DocumentImpl(uri2, "google", null);
        assertTrue(newDoc1.equals(newDoc2));
    }

    //Test not equal
    @Test
    public void testNotEqual(){
        URI uri1 = URI.create("http://www.google.com");
        URI uri2 = URI.create("http://www.facebook.com");
        DocumentImpl newDoc1 = new DocumentImpl(uri1, "google", null);
        DocumentImpl newDoc2 = new DocumentImpl(uri2, "facebook", null);
        assertFalse(newDoc1.equals(newDoc2));
    }

    //Test word count
    @Test
    public void wordCount(){
        URI uri = URI.create("http://www.google.com");
        DocumentImpl newDoc = new DocumentImpl(uri, "Hello google. I am google. I am a search engine. Google is good. I like google.", null);
        assertEquals(3, newDoc.wordCount("google"));
        assertEquals(1, newDoc.wordCount("Google"));
    }

    //Test getWords()
    @Test
    public void getWordsTest(){
        URI uri = URI.create("http://www.google.com");
        DocumentImpl newDoc = new DocumentImpl(uri, "Hello google. I am google. I am a search engine. Google is good. I like google.", null);
        Set<String> words = newDoc.getWords();
        Set<String> testSet = Set.of("Hello", "google", "I", "am", "a", "search", "engine", "is", "good", "like", "Google");
        assertTrue(words.containsAll(testSet));
    }
}