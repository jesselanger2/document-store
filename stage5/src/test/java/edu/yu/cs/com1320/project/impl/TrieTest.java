package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TrieTest {

    //Create a new TrieImpl
    public TrieImpl<Integer> createTrie() {

        TrieImpl<Integer> trie = new TrieImpl<>();

        trie.put("Hello", 1);
        trie.put("Hello", 50);
        trie.put("hello", 2);
        trie.put("hEllO", 3);
        trie.put("hell", 4);
        trie.put("he", 5);

        return trie;
    }

    //Create a comparator
    public Comparator<Integer> createComparator() {

        Comparator<Integer> comparator = new Comparator<Integer>() {

            @Override
            public int compare(Integer o1, Integer o2) {

                if(o1 < o2) {
                    return 1;
                }else if(o1 > o2) {
                    return -1;
                }else{
                    return 0;
                }
            }
        };

        return comparator;
    }

    //Test the getAllSorted method
    @Test
    public void getAllSortedTest() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        List<Integer> list = trie.getAllSorted("Hello", comparator);

        assertEquals(2, list.size());
        assertEquals(50, list.get(0));
    }

    //Test the getAllWithPrefixSorted method
    @Test
    public void getAllWithPrefixSortedTest() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        List<Integer> list = trie.getAllWithPrefixSorted("he", comparator);

        assertEquals(3, list.size());
        assertEquals(5, list.get(0));
    }

    //Test the delete method
    @Test
    public void deleteTest() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        trie.delete("Hello", 50);
        List<Integer> list = trie.getAllSorted("Hello", comparator);
        assertEquals(1, list.size());
        assertEquals(1, list.get(0));

        trie.delete("Hello", 1);
        List<Integer> list2 = trie.getAllSorted("Hello", comparator);
        assertEquals(0, list2.size());
    }

    //Test the deleteAll method
    @Test
    public void deleteAllTest() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        Set<Integer> deleted = trie.deleteAll("Hello");
        List<Integer> list = trie.getAllSorted("Hello", comparator);

        assertEquals(2, deleted.size());
        assertEquals(0, list.size());
    }

    //Test the deleteAllWithPrefix method
    @Test
    public void deleteAllWithPrefixTest() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        Set<Integer> deleted = trie.deleteAllWithPrefix("he");
        List<Integer> list = trie.getAllWithPrefixSorted("he", comparator);

        assertEquals(3, deleted.size());
        assertEquals(0, list.size());
    }

    //Test the deleteAll method with a non-existent key
    @Test
    public void deleteAllTest2() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        Set<Integer> deleted = trie.deleteAll("Helloo");
        List<Integer> list = trie.getAllSorted("Hello", comparator);

        assertEquals(0, deleted.size());
        assertEquals(2, list.size());
    }

    //Test the deleteAllWithPrefix method with a null prefix
    @Test
    public void deleteAllWithPrefixTest2() {

        TrieImpl<Integer> trie = createTrie();
        Comparator<Integer> comparator = createComparator();

        assertThrows(IllegalArgumentException.class, () -> {
            trie.deleteAllWithPrefix(null);
        });

    }
}