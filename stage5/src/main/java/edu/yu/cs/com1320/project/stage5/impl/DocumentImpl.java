package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;

import java.net.URI;
import java.util.*;

public class DocumentImpl implements Document {

    private URI uri;
    private String txt = null;
    private byte[] binaryData = null;
    private Map<String, Integer> wordCountMap;
    private long lastUseTime;

    public DocumentImpl(URI uri, String txt, Map<String, Integer> wordCountMap){

        if(uri == null || uri.toString().isEmpty() || txt == null || txt.isEmpty()){

            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.txt = txt;
        this.lastUseTime = 0;

        if(wordCountMap == null) {

            this.wordCountMap = new HashMap<>();
            addWordsToMap(txt);

        }else{

            this.wordCountMap = wordCountMap;
        }


    }

    public DocumentImpl(URI uri, byte[] binaryData){

        if(uri == null || uri.toString().isEmpty() || binaryData == null || binaryData.length == 0){

            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
        this.lastUseTime = 0;
    }

    /**
     * @return a copy of the word to count map so it can be serialized
     */
    public Map<String,Integer> getWordMap() {

        if(this.txt == null){

            return new HashMap<>();
        }

        Map<String, Integer> mapCopy = new HashMap<>();
        mapCopy.putAll(this.wordCountMap);
        return mapCopy;
    }

    /**
     * This must set the word to count map during deserialization
     * @param wordMap
     */
    public void setWordMap(Map<String,Integer> wordMap) {

        this.wordCountMap = wordMap;
    }

    /**
     * @return content of text document
     */
    @Override
    public String getDocumentTxt() {

        return this.txt;
    }

    /**
     * @return content of binary data document
     */
    @Override
    public byte[] getDocumentBinaryData() {

        return this.binaryData;
    }

    /**
     * @return URI which uniquely identifies this document
     */
    @Override
    public URI getKey() {

        return this.uri;
    }

    /**
     * how many times does the given word appear in the document?
     * @param word
     * @return the number of times the given words appears in the document. If it's a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {

        if(this.txt == null || !this.wordCountMap.containsKey(word)){

            return 0;

        }else{

            return this.wordCountMap.get(word);
        }
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {

        return this.txt == null ? new HashSet<>() : this.wordCountMap.keySet();
    }

    /**
     * return the last time this document was used, via put/get or via a search result
     */
    @Override
    public long getLastUseTime() {

        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {

        this.lastUseTime = timeInNanoseconds;
    }

    @Override
    public int compareTo(Document o) {

        if(o == null) {

            throw new NullPointerException("Cannot compare to null");
        }

        return Long.compare(this.getLastUseTime(), o.getLastUseTime());
    }

    /**
     * @return hash code of the Document
     */
    @Override
    public int hashCode() {

        int result = uri.hashCode();
        result = 31 * result + (txt != null ? txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(binaryData);
        return Math.abs(result);
    }

    /**
     * @return true if the given object has the same hash code as this one
     */
    @Override
    public boolean equals(Object o) {

        if (this == o){

            return true;
        }

        if (o == null || getClass() != o.getClass()){

            return false;
        }

        DocumentImpl document = (DocumentImpl) o;
        return document.hashCode() == this.hashCode();
    }

    // If the document is text, add the words to a hashmap to keep track of word count
    private void addWordsToMap(String txt) {

        //Eliminate everything except for letters and numbers
        String newTxt = txt.replaceAll("[^A-Za-z0-9 ]", "");
        //Split up text into multiple strings
        String[] allTxt = newTxt.split(" ");

        for(String word : allTxt){

            if(!this.wordCountMap.containsKey(word)){

                this.wordCountMap.put(word, 1);

            }else{

                this.wordCountMap.put(word, this.wordCountMap.get(word) + 1);
            }
        }
    }
}