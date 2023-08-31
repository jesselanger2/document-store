package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl() {

        this.elements = (E[]) new Comparable[10];
    }

    @Override
    public void reHeapify(E element) {

        if(element == null) {

            throw new NoSuchElementException("Element is null");
        }

        if(!contains(element)) {

            throw new NoSuchElementException("Element is not in heap");
        }

        int index = getArrayIndex(element);
        downHeap(index);
        upHeap(index);
    }

    @Override
    protected int getArrayIndex(E element) {

        // If element is null or is not in the elements array, throw NoSuchElementException
        if(element == null || !this.contains(element)) {

            throw new NoSuchElementException();
        }

        int i;
        for(i = 1; i < count; i++) {

            if(elements[i].equals(element)) {

                break;
            }
        }

        return i;
    }

    @Override
    protected void doubleArraySize() {

        E[] copy = (E[]) new Comparable[elements.length * 2];

        for(int i = 1; i < elements.length; i++) {

            copy[i] = elements[i];
        }

        elements = copy;
    }

    private boolean contains(E element) {

        for(int i = 1; i < count + 1; i++) {

            if(elements[i].equals(element)) {

                return true;
            }
        }

        return false;
    }
}
