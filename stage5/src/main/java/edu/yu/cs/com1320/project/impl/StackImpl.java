package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {

    //inner class to represent a node in the stack
    private class Node<T>{
        T data;
        Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private Node<T> top;  //node representing the top of the stack

    //initialize the null stack
    public StackImpl() {
        top = null;
    }

    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        Node<T> node = new Node<>(element);
        if(element != null) {
            if (top != null) {
                node.next = top;
            }
            top = node;
        }
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        if(top != null) {
            T element = top.data;
            top = top.next;
            return element;
        }
        return null;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek() {
        if(top != null) {
            return top.data;
        }
        return null;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        int counter = 0;
        Node<T> current = top;
        while(current != null){
            counter++;
            current = current.next;
        }
        return counter;
    }
}
