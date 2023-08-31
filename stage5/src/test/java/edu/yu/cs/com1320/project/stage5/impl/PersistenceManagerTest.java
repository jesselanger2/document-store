package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceManagerTest {

    private URI uri1;
    private URI uri2;

    public Document[] createDocs(){

        Document[] docs = new Document[2];

        String txt = "This is a test. This is only a test. If this were a real document, it would contain actual words.";
        URI uri = URI.create("http://edu.yu.cs/com1320/project/doc1");
        this.uri1 = uri;
        DocumentImpl doc = new DocumentImpl(uri, txt, null);
        docs[0] = doc;

        byte[] binaryData = {(byte) 0b10011010, (byte) 0b01100100, (byte) 0b00011101, (byte) 0b11010101, (byte) 0b00110100,
                (byte) 0b10100010, (byte) 0b11100011, (byte) 0b01001000, (byte) 0b01110101, (byte) 0b00001010};
        URI uri2 = URI.create("http://edu.yu.cs/com1320/project/doc2");
        this.uri2 = uri2;
        DocumentImpl doc2 = new DocumentImpl(uri2, binaryData);
        docs[1] = doc2;

        return docs;
    }

    @Test
    public void testSerializeAndDeserializeWithTxtDoc() throws IOException {

        DocumentPersistenceManager pm = new DocumentPersistenceManager(null);
        Document[] docs = createDocs();

        Map<String, Integer> wordMap = docs[0].getWordMap();
        String txt = docs[0].getDocumentTxt();


        pm.serialize(this.uri1, docs[0]);
        Document deserialized = pm.deserialize(this.uri1);

        assertEquals(docs[0], deserialized);
        assertEquals(txt, deserialized.getDocumentTxt());
        assertEquals(wordMap, deserialized.getWordMap());
    }

    @Test
    public void testSerializeAndDeserializeWithBinaryDoc() throws IOException {

        DocumentPersistenceManager pm = new DocumentPersistenceManager(null);
        Document[] docs = createDocs();

        byte[] binaryData = docs[1].getDocumentBinaryData();

        pm.serialize(this.uri2, docs[1]);
        Document deserialized = pm.deserialize(this.uri2);

        assertEquals(docs[1], deserialized);
        assertArrayEquals(binaryData, deserialized.getDocumentBinaryData());
        assertEquals(new HashMap<>(), deserialized.getWordMap());
    }
}
