package editor;
import java.util.Iterator;

public class ArrayRingBuffer<T> extends AbstractBoundedQueue<T> {

    private int last;
    private T[] rb;


    public ArrayRingBuffer(int capacity) {

        rb = (T[]) new Object[capacity];
        fillCount = 0;
        last = 0;
        this.capacity = capacity;
    }

    public void enqueue(T x) {
        rb[last] = x;
        if (last + 1 >= capacity) {
            last = 0;
        } else {
            last++;
        }
        fillCount++;
    }

    public T dequeue() {
        if (last - 1 < 0) {
            last = capacity - 1;
        } else {
            last--;
        }
        T temp = rb[last];
        rb[last] = null;
        fillCount--;
        System.out.println("removed" + temp);
        return temp;
    }

    public T peek() {
        int temp = last;
        if (temp - 1 < 0) {
            temp = capacity - 1;
        } else {
            temp--;
        }
        return rb[temp];
    }


    @Override
    public Iterator<T> iterator() {
        return new ArrayIterator();
    }

    private class ArrayIterator implements Iterator<T> {
        private int ind = 0;
        public ArrayIterator() {
            ind = 0;
        }
        public boolean hasNext() {
            return !isFull();
        }
        public T next() {
            T item = rb[ind];
            ind++;
            return item;
        }
    }
}
