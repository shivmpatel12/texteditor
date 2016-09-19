package editor;
import java.util.Iterator;
/**
 * Created by shivmpatel12 on 2/20/16.
 */
public interface BoundedQueue<T> extends Iterable<T> {
    int capacity();
    int fillCount();
    void enqueue(T x);
    T dequeue();
    T peek();
    default boolean isEmpty() {
        return (fillCount() == 0);
    }
    default boolean isFull() {
        return  (fillCount() == capacity());
    }
    Iterator<T> iterator();
}
