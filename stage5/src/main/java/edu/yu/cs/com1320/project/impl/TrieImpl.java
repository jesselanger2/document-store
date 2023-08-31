package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Trie;

import java.util.*;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int alphabetSize = 128;
    private Node root; // root of trie

    public class Node<Value> {

        private Node[] links = new Node[alphabetSize];
        private List<Value> values = new ArrayList<>();
    }

    //Initialize the Trie
    public TrieImpl() {

    }

    /**
     * add the given value at the given key
     * @param key
     * @param val
     */
    @Override
    public void put(String key, Value val) {

        if(val == null) {

            return;

        }else{

            this.root = put(this.root, key, val, 0);
        }
    }

    private Node put(Node x, String key, Value val, int d) {

        //create a new node
        if(x == null) {

            x = new Node();
        }

        //we've reached the last node in the key,
        //add the value to the list of values for the key and return the node
        if(d == key.length()) {

            if(!x.values.contains(val)) {

                x.values.add(val);
            }

            return x;
        }

        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    /**
     * get all exact matches for the given key, sorted in descending order.
     * Search is CASE SENSITIVE.
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values, in descending order
     */
    @Override
    public List<Value> getAllSorted(String key, Comparator<Value> comparator) {

        if(key == null || comparator == null) {

            throw new IllegalArgumentException("Key or comparator cannot be null");
        }

        Node x = get(this.root, key, 0);

        if(x == null){

            return new ArrayList();
        }

        Collections.sort(x.values, comparator);
        return x.values;

    }

    /**
     * get all matches which contain a String with the given prefix, sorted in descending order.
     * For example, if the key is "Too", you would return any value that contains "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in descending order
     */
    @Override
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator) {

        if(prefix == null || comparator == null) {

            throw new IllegalArgumentException("Prefix or comparator cannot be null");
        }

        Set<Value> values = new HashSet<>();
        getAllValuesWithPrefix(get(root, prefix, 0), prefix, values);
        List<Value> valuesList = new ArrayList<>(values);
        Collections.sort(valuesList, comparator);

        return valuesList;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAllWithPrefix(String prefix) {

        if(prefix == null) {

            throw new IllegalArgumentException("Prefix cannot be null");
        }

        List<String> keys = new ArrayList<>();
        Set<Value> deletedValues = new HashSet<>();
        getWordsWithPrefix(get(root, prefix, 0), prefix, keys);

        for(String key : keys){

            deletedValues.addAll(deleteAll(key));
        }
        return deletedValues;
    }

    /**
     * Delete all values from the node of the given key (do not remove the values from other nodes in the Trie)
     * @param key
     * @return a Set of all Values that were deleted.
     */
    @Override
    public Set<Value> deleteAll(String key) {

        if(key == null){

            throw new IllegalArgumentException("Key cannot be null");
        }

        Node x = get(this.root, key, 0);

        if(x == null) {

            return new HashSet();
        }

        Set<Value> deletedValues = new HashSet<>();
        Set<Value> values = new HashSet<>(x.values);
        for(Value val : values) {

            deletedValues.add(val);
            delete(key, val);
        }

        return deletedValues;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the value from other nodes in the Trie)
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given value, return null.
     */
    @Override
    public Value delete(String key, Value val) {

        if(key == null || val == null){

            throw new IllegalArgumentException("Input cannot be null");
        }

        Node x = get(this.root, key, 0);

        boolean contains = false;
        if(x.values.contains(val)){

            contains = true;
        }

        if(x == null){

            return null;
        }

        this.root = delete(this.root, key, val, 0);

        return contains ? val : null;
    }

    // Deletes the value from the node of the given key
    // If the node has no other values or children, delete the node
    private Node delete(Node x, String key, Value val, int d) {

        //link was null - return null, indicating a miss
        if(x == null) {

            return null;
        }

        //we've reached the last node in the key
        //delete the value from the list of values for the key and return the node
        if(d == key.length()) {

            x.values.remove(val);
            return x;
        }

        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.delete(x.links[c], key, val, d + 1);

        //if the node has no children and no values, delete it
        if(x.links[c] == null && x.values.isEmpty()) {

            x = null;
        }

        return x;
    }

    private Node get(Node x, String key, int d) {

        //link was null - return null, indicating a miss
        if(x == null) {

            return null;
        }

        //we've reached the last node in the key,
        //return the node
        if(d == key.length()) {

            return x;
        }

        //proceed to the next node in the chain of nodes that
        //forms the desired key
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    // Adds all keys (words) with the given prefix to the given list
    private void getWordsWithPrefix(Node x, String prefix, List<String> keys) {

        if(x == null) {

            return;
        }

        if(!x.values.contains(prefix)) {

            keys.add(prefix);
        }

        for(char c = 0; c < alphabetSize; c++) {

            getWordsWithPrefix(x.links[c], prefix + c, keys);
        }
    }

    private void getAllValuesWithPrefix(Node x, String prefix, Set<Value> values) {

        if(x == null) {

            return;
        }

        if(!x.values.isEmpty()) {

            values.addAll(x.values);
        }

        for(char c = 0; c < alphabetSize; c++) {

            getAllValuesWithPrefix(x.links[c], prefix + c, values);
        }
    }
}
