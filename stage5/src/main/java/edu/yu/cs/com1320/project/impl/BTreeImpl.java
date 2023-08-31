package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.IOException;
import java.util.*;


public class BTreeImpl<Key extends Comparable<Key>, Value> implements BTree<Key, Value> {

    //max children per B-tree node = MAX-1 (must be an even number and greater than 2)
    private static final int MAX = 6;
    private BTreeImpl.Node root; //root of the B-tree
    private int height; //height of the B-tree
    private int n; //number of key-value pairs in the B-tree

    private PersistenceManager<Key, Value> pm = null;
    private Set<Key> keysOnDisk;


    //B-tree node data type
    private static final class Node {

        private int entryCount; // number of entries
        private BTreeImpl.Entry[] entries = new BTreeImpl.Entry[BTreeImpl.MAX]; // the array of children
        private BTreeImpl.Node next;
        private BTreeImpl.Node previous;

        // create a node with k entries
        private Node(int k) {

            this.entryCount = k;
        }

        private void setNext(BTreeImpl.Node next) {

            this.next = next;
        }

        private BTreeImpl.Node getNext() {

            return this.next;
        }

        private void setPrevious(BTreeImpl.Node previous) {

            this.previous = previous;
        }

        private BTreeImpl.Node getPrevious() {

            return this.previous;
        }

        private BTreeImpl.Entry[] getEntries() {

            return Arrays.copyOf(this.entries, this.entryCount);
        }

    }

    //internal nodes: only use key and child
    //external nodes: only use key and value
    private static class Entry {

        private Comparable key;
        private Object val;
        private BTreeImpl.Node child;

        public Entry(Comparable key, Object val, BTreeImpl.Node child) {

            this.key = key;
            this.val = val;
            this.child = child;
        }

        public Object getValue() {

            return this.val;
        }

        public Comparable getKey() {

            return this.key;
        }
    }

    public BTreeImpl() {

        this.root = new BTreeImpl.Node(0);
        this.keysOnDisk = new HashSet<>();
    }

    /**
     * Returns the value associated with the given key.
     *
     * @param k the key
     * @return the value associated with the given key if the key is in the symbol table and {@code null} if the key is not in the symbol table
     * @throws IllegalArgumentException if {@code k} is {@code null}
     */
    @Override
    public Value get(Key k) {

        if(k == null) {

            throw new IllegalArgumentException("argument to get() is null");
        }

        if(keysOnDisk.contains(k)) {

            try {

                Value v = this.pm.deserialize(k);
                put(k, v);
                keysOnDisk.remove(k);
                return v;

            } catch(IOException e) {

                e.printStackTrace();
            }
        }

        BTreeImpl.Entry entry = this.get(this.root, k, this.height);

        if(entry != null) {

            return (Value) entry.val;
        }

        return null;
    }

    private BTreeImpl.Entry get(BTreeImpl.Node currentNode, Key key, int height) {

        BTreeImpl.Entry[] entries = currentNode.entries;

        //current node is external (i.e. height == 0)
        if(height == 0) {

            for(int j = 0; j < currentNode.entryCount; j++) {

                if(isEqual(key, entries[j].key)) {

                    //found desired key. Return its value
                    return entries[j];
                }
            }

            //didn't find the key
            return null;

        //current node is internal (height > 0)
        }else{

            for(int j = 0; j < currentNode.entryCount; j++) {

                //if (we are at the last key in this node OR the key we are looking for is less than the next key,
                //i.e. the desired key must be in the subtree below the current entry),then recurse into the current entry’s child
                if(j + 1 == currentNode.entryCount || less(key, entries[j + 1].key)) {

                    return this.get(entries[j].child, key, height - 1);
                }
            }

            //didn't find the key
            return null;
        }
    }

    /**
     * Inserts the key-value pair into the symbol table, overwriting the old value with the new value if the key is already in the symbol table.
     * If the value is {@code null}, this effectively deletes the key from the symbol table.
     *
     * @param k the key
     * @param v the value
     * @throws IllegalArgumentException if {@code k} is {@code null}
     */
    @Override
    public Value put(Key k, Value v) {

        if (k == null) {

            throw new IllegalArgumentException("argument key to put() is null");
        }

        //if the key already exists in the b-tree, simply replace the value
        BTreeImpl.Entry alreadyThere = this.get(this.root, k, this.height);
        Value oldVal = null;

        if(alreadyThere != null) {

            if(keysOnDisk.contains(k)) {

                try {

                    oldVal = this.pm.deserialize(k);
                    this.pm.delete(k);
                    keysOnDisk.remove(k);

                } catch(IOException e) {

                    e.printStackTrace();
                }

            }else{

                oldVal = (Value) alreadyThere.val;
            }

            alreadyThere.val = v;
            return oldVal;
        }

        if(v == null) {

            return null;
        }

        BTreeImpl.Node newNode = this.put(this.root, k, v, this.height);
        this.n++;

        if (newNode == null) {

            return null;
        }

        //split the root:
        //Create a new node to be the root.
        //Set the old root to be new root's first entry.
        //Set the node returned from the call to put to be new root's second entry
        BTreeImpl.Node newRoot = new BTreeImpl.Node(2);
        newRoot.entries[0] = new BTreeImpl.Entry(this.root.entries[0].key, null, this.root);
        newRoot.entries[1] = new BTreeImpl.Entry(newNode.entries[0].key, null, newNode);
        this.root = newRoot;

        //a split at the root always increases the tree height by 1
        this.height++;
        return null;
    }

    /**
     *
     * @param currentNode
     * @param key
     * @param val
     * @param height
     * @return null if no new node was created (i.e. just added a new Entry into an existing node). If a new node was created due to the need to split, returns the new node
     */
    private BTreeImpl.Node put(BTreeImpl.Node currentNode, Key key, Value val, int height) {

        int j;
        BTreeImpl.Entry newEntry = new BTreeImpl.Entry(key, val, null);

        //external node
        if (height == 0) {

            //find index in currentNode’s entry[] to insert new entry
            //we look for key < entry.key since we want to leave j pointing to the slot to insert the new entry,
            //hence we want to find the first entry in the current node that key is LESS THAN
            for (j = 0; j < currentNode.entryCount; j++) {

                if (less(key, currentNode.entries[j].key)) {

                    break;
                }
            }

        // internal node
        }else{

            //find index in node entry array to insert the new entry
            for(j = 0; j < currentNode.entryCount; j++) {

                //if (we are at the last key in this node OR the key we are looking for is less than the next key,
                //i.e. the desired key must be added to the subtree below the current entry), then do a recursive call to put on the current entry’s child
                if((j + 1 == currentNode.entryCount) || less(key, currentNode.entries[j + 1].key)) {

                    //increment j (j++) after the call so that a new entry created by a split will be inserted in the next slot
                    BTreeImpl.Node newNode = this.put(currentNode.entries[j++].child, key, val, height - 1);

                    if(newNode == null) {

                        return null;
                    }

                    //if the call to put returned a node, it means I need to add a new entry to the current node
                    newEntry.key = newNode.entries[0].key;
                    newEntry.val = null;
                    newEntry.child = newNode;
                    break;
                }
            }
        }

        //shift entries over one place to make room for new entry
        for(int i = currentNode.entryCount; i > j; i--) {

            currentNode.entries[i] = currentNode.entries[i - 1];
        }

        //add new entry
        currentNode.entries[j] = newEntry;
        currentNode.entryCount++;

        if (currentNode.entryCount < BTreeImpl.MAX) {

            //no structural changes needed in the tree so just return null
            return null;

        }else{

            //will have to create new entry in the parent due to the split, so return the new node, which is the node for which the new entry will be created
            return this.split(currentNode, height);
        }
    }

    /**
     * split node in half
     * @param currentNode
     * @return new node
     */
    private BTreeImpl.Node split(BTreeImpl.Node currentNode, int height) {

        BTreeImpl.Node newNode = new BTreeImpl.Node(BTreeImpl.MAX / 2);

        //by changing currentNode.entryCount, we will treat any value at index higher than the new currentNode.entryCount as if it doesn't exist
        currentNode.entryCount = BTreeImpl.MAX / 2;

        //copy top half of h into t
        for(int j = 0; j < BTreeImpl.MAX / 2; j++) {

            newNode.entries[j] = currentNode.entries[BTreeImpl.MAX / 2 + j];
        }

        //external node
        if(height == 0) {

            newNode.setNext(currentNode.getNext());
            newNode.setPrevious(currentNode);
            currentNode.setNext(newNode);
        }

        return newNode;
    }

    @Override
    public void moveToDisk(Key k) throws Exception {

        if(k == null) {

            throw new IllegalArgumentException("argument key to moveToDisk() is null");
        }

        if(this.pm == null) {

            throw new IllegalStateException("no persistence manager");
        }

        if(get(k) == null) {

            throw new NoSuchElementException("key not found");
        }

        BTreeImpl.Entry alreadyThere = this.get(this.root, k, this.height);

        pm.serialize(k, get(k));
        alreadyThere.val = null;
        keysOnDisk.add(k);
    }

    @Override
    public void setPersistenceManager(PersistenceManager<Key,Value> pm) {

        this.pm = pm;
    }

    // comparison functions - make Comparable instead of Key to avoid casts
    private static boolean less(Comparable k1, Comparable k2) {

        return k1.compareTo(k2) < 0;
    }

    private static boolean isEqual(Comparable k1, Comparable k2) {

        return k1.compareTo(k2) == 0;
    }
}