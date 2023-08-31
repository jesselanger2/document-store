package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MinHeapTest {

    public MinHeapImpl<Integer> createMinHeap() {

        return new MinHeapImpl<>();
    }

    //Test constructor
    @Test
    public void testConstructor() {

        MinHeapImpl<Integer> minHeap = createMinHeap();
        assertNotNull(minHeap);
    }

    //Test insert() and remove()
    @Test
    public void testInsertAndRemove() {

        MinHeapImpl<Integer> minHeap = createMinHeap();
        minHeap.insert(10);
        minHeap.insert(20);
        minHeap.insert(30);
        minHeap.insert(15);
        minHeap.insert(5);
        minHeap.insert(25);
        minHeap.insert(35);
        minHeap.insert(40);
        minHeap.insert(45);
        minHeap.insert(1);

        assertEquals(1, minHeap.remove());
        assertEquals(5, minHeap.remove());
        assertEquals(10, minHeap.remove());
        assertEquals(15, minHeap.remove());
        assertEquals(20, minHeap.remove());
        assertEquals(25, minHeap.remove());
        assertEquals(30, minHeap.remove());
        assertEquals(35, minHeap.remove());
        assertEquals(40, minHeap.remove());
        assertEquals(45, minHeap.remove());
    }

    // test inserting 4 characters with the lowest one being inserted first and see if its position in the array changes correctly after insertions
    @Test
    public void testArrayPositions() {

        MinHeapImpl<Character> minHeap = new MinHeapImpl<>();

        minHeap.insert('m');
        assertEquals(1, minHeap.getArrayIndex('m'));

        minHeap.insert('c');
        assertEquals(2, minHeap.getArrayIndex('m'));

        minHeap.insert('e');
        minHeap.insert('i');
        assertEquals(4, minHeap.getArrayIndex('m'));
    }
}
