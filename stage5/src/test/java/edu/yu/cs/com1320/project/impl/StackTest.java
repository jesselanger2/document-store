package edu.yu.cs.com1320.project.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StackTest {

    //Test stack with one element
    @Test
    public void testStackOneElement() {
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(1);
        assertEquals(1, stack.peek());
        assertEquals(1, stack.pop());
        assertNull(stack.peek());
        assertNull(stack.pop());
    }

    //Test stack with multiple elements
    @Test
    public void testStackMultipleElements() {
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.peek());
        assertEquals(3, stack.pop());
        assertEquals(2, stack.peek());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.peek());
        assertEquals(1, stack.pop());
        assertNull(stack.peek());
        assertNull(stack.pop());
    }

    //Test stack with null elements
    @Test
    public void testStackNullElements() {
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(null);
        stack.push(null);
        stack.push(null);
        assertNull(stack.peek());
        assertNull(stack.pop());
        assertNull(stack.peek());
        assertNull(stack.pop());
        assertNull(stack.peek());
        assertNull(stack.pop());
        assertNull(stack.peek());
        assertNull(stack.pop());
    }

    //Test stack size
    @Test
    public void testStackSize() {
        StackImpl<Integer> stack = new StackImpl<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.size());
        stack.pop();
        assertEquals(2, stack.size());
        stack.pop();
        assertEquals(1, stack.size());
        stack.pop();
        assertEquals(0, stack.size());
        stack.pop();
        assertEquals(0, stack.size());
    }
}