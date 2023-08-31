package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.*;
import edu.yu.cs.com1320.project.impl.*;
import edu.yu.cs.com1320.project.stage5.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;


public class DocumentStoreImpl implements DocumentStore {

    private BTreeImpl<URI, Document> bTree;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<URI> trie;
    private MinHeapImpl<Node> minHeap;

    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int documentCount;
    private int documentBytes;

    private class Node implements Comparable<Node> {

        private URI uri;
        private long lastUseTime;

        public Node(URI uri, long lastUseTime) {

            this.uri = uri;
            this.lastUseTime = lastUseTime;
        }

        @Override
        public int compareTo(Node o) {

            return Long.compare(this.lastUseTime, o.lastUseTime);
        }

        @Override
        public boolean equals(Object o) {

            if(this == o){

                return true;
            }

            if(o == null || getClass() != o.getClass()) {

                return false;
            }

            Node node = (Node) o;
            return uri.equals(node.uri);
        }

        @Override
        public int hashCode() {

            return uri.toString().hashCode();
        }
    }

    public DocumentStoreImpl() {

        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(new DocumentPersistenceManager(null));
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.documentCount = 0;
        this.documentBytes = 0;
    }

    public DocumentStoreImpl(File baseDir) {

        this.bTree = new BTreeImpl<>();
        this.bTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
        this.commandStack = new StackImpl<>();
        this.trie = new TrieImpl<>();
        this.minHeap = new MinHeapImpl<>();
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.documentCount = 0;
        this.documentBytes = 0;
    }

    /**
     * @param input the document being put
     * @param uri unique identifier for the document
     * @param format indicates which type of document format is being passed
     * @return if there is no previous doc at the given URI, return 0. If there is a previous doc, return the hashCode of the previous doc. If InputStream is null, this is a delete, and thus return either the hashCode of the deleted doc or 0 if there is no doc to delete.
     * @throws IOException if there is an issue reading input
     * @throws IllegalArgumentException if uri or format are null
     */
    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {

        if(uri == null || format == null){

            throw new IllegalArgumentException();
        }

        if(input == null){  //if InputStream is null, delete the document at the given uri

            return deleteDocument(uri);

        }else{  //if InputStream is not null put the document into the store

            return putInStore(input, uri, format);
        }
    }

    /**
     * @param uri the unique identifier of the document to get
     * @return the given document
     */
    @Override
    public Document get(URI uri) {

        Document doc = bTree.get(uri);

        if(doc != null){

            doc.setLastUseTime(System.nanoTime());

            try {

                minHeap.reHeapify(new Node(uri, doc.getLastUseTime()));

            } catch (NoSuchElementException e) {

                incrementDocCount(doc);
                putInHeap(doc, null);
            }
        }

        return doc;
    }

    /**
     * @param uri the unique identifier of the document to delete
     * @return true if the document is deleted, false if no document exists with that URI
     */
    @Override
    public boolean delete(URI uri) {

        Document docToDelete = bTree.get(uri);

        if(docToDelete == null){

            return false;

        }else{

            undoDelete(uri, (DocumentImpl)docToDelete);
            bTree.put(uri, null);
            deleteFromTrie((DocumentImpl) docToDelete);
            deleteFromHeap(docToDelete);
            decrementDocCount(docToDelete);

            return true;
        }
    }

    /**
     * undo the last put or delete command
     * @throws IllegalStateException if there are no actions to be undone, i.e. the command stack is empty
     */
    @Override
    public void undo() throws IllegalStateException {

        if(commandStack.size() == 0){

            throw new IllegalStateException("The command stack is empty");
        }

        commandStack.pop().undo();
    }

    /**
     * undo the last put or delete that was done with the given URI as its key
     * @param uri
     * @throws IllegalStateException if there are no actions on the command stack for the given URI
     */
    @Override
    public void undo(URI uri) throws IllegalStateException {

        int count = containsAction(uri);  //call method to check the stack for a command with the given uri

        if(count == -1){

            throw new IllegalStateException("No actions on the command stack for the given URI");
        }

        StackImpl<Undoable> tempStack = new StackImpl<>();
        Undoable current = commandStack.peek();

        //add commands to a temporary stack until current == the command meant to be undone
        for(int i = 0; i < count; i++){

            tempStack.push(commandStack.pop());
            current = commandStack.peek();
        }

        if(current instanceof GenericCommand) {

            GenericCommand<URI> command = (GenericCommand<URI>) current;
            command.undo();
            commandStack.pop();

        }else{

            CommandSet<URI> commandSet = (CommandSet<URI>) current;
            commandSet.undo(uri);

            if(commandSet.size() == 0) {

                commandStack.pop();
            }
        }

        //add commands back to command stack
        while(tempStack.size() != 0){

            commandStack.push(tempStack.pop());
        }
    }

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) {

        List<URI> allUrisSorted = trie.getAllSorted(keyword, new Comparator<URI>() {
            public int compare(URI o1, URI o2) {
                if(bTree.get(o1).wordCount(keyword) < bTree.get(o2).wordCount(keyword)) {
                    return 1;
                }else if(bTree.get(o1).wordCount(keyword) > bTree.get(o2).wordCount(keyword)) {
                    return -1;
                }else{
                    return 0;
                }
            }
        });

        if(allUrisSorted.isEmpty()) {

            return new ArrayList<>();
        }

        long timeUsed = System.nanoTime();
        List<Document> allDocsSorted = new ArrayList<>();

        for(URI uri : allUrisSorted) {

            Document doc = bTree.get(uri);
            doc.setLastUseTime(timeUsed);

            try {

                minHeap.reHeapify(new Node(uri, timeUsed));

            } catch(NoSuchElementException e) {

                incrementDocCount(doc);
                putInHeap(doc, null);
            }

            allDocsSorted.add(doc);
        }

        return allDocsSorted;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) {

        List<URI> urisWithPrefixSorted = trie.getAllSorted(keywordPrefix, new Comparator<URI>() {
            public int compare(URI o1, URI o2) {
                if(bTree.get(o1).wordCount(keywordPrefix) < bTree.get(o2).wordCount(keywordPrefix)) {
                    return 1;
                }else if(bTree.get(o1).wordCount(keywordPrefix) > bTree.get(o2).wordCount(keywordPrefix)) {
                    return -1;
                }else{
                    return 0;
                }
            }
        });

        if(urisWithPrefixSorted.isEmpty()) {

            return new ArrayList<>();
        }

        long timeUsed = System.nanoTime();
        List<Document> docsWithPrefixSorted = new ArrayList<>();

        for(URI uri : urisWithPrefixSorted) {

            Document doc = bTree.get(uri);
            doc.setLastUseTime(timeUsed);

            try {

                minHeap.reHeapify(new Node(uri, timeUsed));

            } catch(NoSuchElementException e) {

                incrementDocCount(doc);
                putInHeap(doc, null);
            }

            docsWithPrefixSorted.add(doc);
        }

        return docsWithPrefixSorted;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {

        Set<URI> deletedURIs = trie.deleteAll(keyword);
        CommandSet commandSet = new CommandSet();

        for(URI uri : deletedURIs) {

            Document doc = bTree.get(uri);
            deleteFromTrie((DocumentImpl) doc);
            deleteFromHeap(doc);
            decrementDocCount(doc);
            bTree.put(uri, null);

            commandSet.addCommand(new GenericCommand(uri, undo -> {
                bTree.put(uri, doc);
                putInTrie((DocumentImpl) doc);
                putInHeap(doc, null);
                incrementDocCount(doc);
                return true;
            }));
        }

        commandStack.push(commandSet);

        return deletedURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has the given prefix
     * Search is CASE SENSITIVE.
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {

        Set<URI> deletedURIs = trie.deleteAllWithPrefix(keywordPrefix);
        CommandSet commandSet = new CommandSet();

        for(URI uri : deletedURIs) {

            Document doc = bTree.get(uri);
            deleteFromTrie((DocumentImpl) doc);
            deleteFromHeap(doc);
            decrementDocCount(doc);
            bTree.put(uri, null);

            commandSet.addCommand(new GenericCommand(uri, undo -> {
                bTree.put(uri, doc);
                putInTrie((DocumentImpl) doc);
                putInHeap(doc, null);
                incrementDocCount(doc);
                return true;
            }));
        }

        commandStack.push(commandSet);

        return deletedURIs;
    }

    /**
     * set maximum number of documents that may be stored
     * @param limit
     */
    @Override
    public void setMaxDocumentCount(int limit) {

        if(limit < 0) {

            throw new IllegalArgumentException("Limit cannot be negative");
        }

        this.maxDocumentCount = limit;

        //while the store is over the max document limit, delete the oldest documents until it is under the limit
        while(this.documentCount > this.maxDocumentCount) {

            try {

                removeUnusedDocs();

            } catch(Exception e) {

                e.printStackTrace();
            }
        }
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents in memory combined
     * @param limit
     */
    @Override
    public void setMaxDocumentBytes(int limit) {

       if(limit < 0) {

            throw new IllegalArgumentException("Limit cannot be negative");
       }

       this.maxDocumentBytes = limit;

       //while the store is over the max bytes limit, delete the oldest documents until it is under the limit
        while(this.documentBytes > this.maxDocumentBytes) {

            try {

                removeUnusedDocs();

            } catch(Exception e) {

                e.printStackTrace();
            }
        }
    }

    //Delete the document at the given uri and return the hash code of the deleted document. If no document was deleted, return 0.
    private int deleteDocument(URI uri) {

        Document toDelete = bTree.get(uri);

        if(toDelete != null) {

            delete(uri);
            return toDelete.hashCode();

        }else{

            return 0;
        }
    }

    //Put the document into the store and return the hash code of the old document. If no document was replaced, return 0.
    private int putInStore(InputStream input, URI uri, DocumentFormat format) throws IOException {

        //put new doc into store at given uri, replacing the old one
        Document newDoc = null;
        try {

            newDoc = newDocument(uri, input, format);

        } catch(Exception e) {

            e.printStackTrace();
        }

        if(newDoc == null) {

            return 0;
        }

        Document oldDoc = bTree.put(uri, newDoc);

        if(newDoc.getDocumentTxt() != null) {

            putInTrie((DocumentImpl) newDoc);
        }

        decrementDocCount(oldDoc);
        incrementDocCount(newDoc);
        putInHeap(newDoc, oldDoc);

        if(oldDoc == null){

            undoPutNull(uri);
            return 0;

        }else{

            undoOverride(uri, (DocumentImpl) oldDoc);
            return oldDoc.hashCode();
        }
    }

    //Create a new document based on the format
    private Document newDocument(URI uri, InputStream input, DocumentFormat format) throws Exception {

        if(format == DocumentFormat.TXT) {

            String txt = IOUtils.toString(input, StandardCharsets.UTF_8);
            Document doc = new DocumentImpl(uri, txt, null);

            if(txt.getBytes().length > maxDocumentBytes) {

                bTree.put(uri, doc);
                putInTrie((DocumentImpl) doc);
                bTree.moveToDisk(uri);
                return null;
            }

            return doc;

        }else{

            byte[] bytes = input.readAllBytes();
            Document doc = new DocumentImpl(uri, bytes);

            if(bytes.length > maxDocumentBytes) {

                bTree.put(uri, doc);
                bTree.moveToDisk(uri);
                return null;
            }

            return doc;
        }
    }

    //Remove the oldest document from the store and move it to disc in the bTree
    private void removeUnusedDocs() throws Exception {

        Node removed = minHeap.remove();
        decrementDocCount(bTree.get(removed.uri));
        bTree.moveToDisk(removed.uri);
    }

    //Method to check if the stack contains a command for the given uri
    //If there is a command for the given uri, return its distance from the top of the stack
    private int containsAction(URI uri){

        Undoable current = commandStack.peek();
        StackImpl<Undoable> tempStack = new StackImpl<>();
        int counter = 0;
        boolean flag = false;

        while(current != null){

            if(current instanceof GenericCommand<?>) {

                if (((GenericCommand<?>)current).getTarget().equals(uri)) {

                    flag = true;
                    break;
                }

            }else{

                if(((CommandSet<URI>)current).containsTarget(uri)){

                    flag = true;
                    break;
                }
            }

            tempStack.push(commandStack.pop());  //pop the top of the command stack into a temporary stack
            current = commandStack.peek();
            counter++;
        }

        //add all elements from the temporary stack back to the command stack
        while(tempStack.size() != 0){

            commandStack.push(tempStack.pop());
        }

        return flag ? counter : -1;  //if there was a command for the given uri return its distance from the top of the stack, else return -1
    }

    //Undo command for deleting a doc
    private void undoDelete(URI uri, DocumentImpl doc){

        Function<URI, Boolean> undo = (func) -> {
            putInTrie(doc);
            bTree.put(uri, doc);
            putInHeap(doc, null);
            incrementDocCount(doc);
            return true;
        };

        commandStack.push(new GenericCommand<>(uri, undo));
    }

    //Undo command for overriding a current doc
    private void undoOverride(URI uri, DocumentImpl oldDoc){

        Function<URI, Boolean> undo = (func) -> {
            undoTrieOverride(uri, oldDoc);
            bTree.put(uri, oldDoc);
            putInHeap(oldDoc, bTree.get(uri));
            decrementDocCount(bTree.get(uri));
            incrementDocCount(oldDoc);
            return true;
        };

        commandStack.push(new GenericCommand<>(uri, undo));
    }

    //Undo command for putting a doc that was never existent
    private void undoPutNull(URI uri){

        Function<URI, Boolean> undo = (func) -> {
            deleteFromTrie((DocumentImpl) bTree.get(uri));
            deleteFromHeap(bTree.get(uri));
            bTree.put(uri, null);
            decrementDocCount(bTree.get(uri));
            return true;
        };

        commandStack.push(new GenericCommand<>(uri, undo));
    }

    //Put a document into the trie
    private void putInTrie(DocumentImpl doc) {

        for(String word : doc.getWords()) {

            trie.put(word, doc.getKey());
        }
    }

    //Delete a document from the trie
    private void deleteFromTrie(DocumentImpl doc) {

        for(String word : doc.getWords()) {

            trie.delete(word, doc.getKey());
        }
    }

    //Undo for overriding a doc in the trie
    private void undoTrieOverride(URI uri, DocumentImpl oldDoc) {

        Document doc = bTree.get(uri);

        for(String word : doc.getWords()) {

            trie.delete(word, uri);
        }

        putInTrie(oldDoc);
    }

    //Put the document into the heap
    private void putInHeap(Document newDoc, Document oldDoc) {

        if(oldDoc != null){

            deleteFromHeap(oldDoc);
        }

        newDoc.setLastUseTime(System.nanoTime());
        minHeap.insert(new Node(newDoc.getKey(), newDoc.getLastUseTime()));
        checkStorage();
    }

    //Delete the given document from the heap
    private void deleteFromHeap(Document doc) {

        doc.setLastUseTime(Long.MIN_VALUE); //set the last use time to the smallest possible value so that it can be removed from the heap
        minHeap.reHeapify(new Node(doc.getKey(), doc.getLastUseTime()));
        minHeap.remove();
    }

    private void decrementDocCount(Document removed) {

        if(removed == null){

            return;
        }

        this.documentCount--;

        if(removed.getDocumentTxt() != null) {

            this.documentBytes -= removed.getDocumentTxt().getBytes().length;

        }else{

            this.documentBytes -= removed.getDocumentBinaryData().length;
        }
    }

    private void incrementDocCount(Document newDoc) {

        this.documentCount++;

        int addedBytes = 0;
        if(newDoc.getDocumentTxt() != null) {

            addedBytes = newDoc.getDocumentTxt().getBytes().length;

        }else{

            addedBytes = newDoc.getDocumentBinaryData().length;
        }

        this.documentBytes += addedBytes;
    }

    private void checkStorage() {

        //while the store is over either of the limits, delete the oldest documents until it is under both limits
        while(this.documentCount > this.maxDocumentCount || this.documentBytes > this.maxDocumentBytes) {

            try {

                removeUnusedDocs();

            } catch(Exception e) {

                e.printStackTrace();
            }
        }
    }
}