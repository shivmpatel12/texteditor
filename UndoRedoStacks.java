package editor;

/**
 * Created by shivmpatel12 on 3/6/16.
 */
public class UndoRedoStacks {

    ArrayRingBuffer<LinkedListDeque.Node> UndoStack;
    ArrayRingBuffer<LinkedListDeque.Node> RedoStack;

    UndoRedoStacks() {
        UndoStack = new ArrayRingBuffer<>(100);
        RedoStack = new ArrayRingBuffer<>(100);
    }

    public void AddUndo(LinkedListDeque.Node N) {
        UndoStack.enqueue(N);
    }

    public void AddRedo(LinkedListDeque.Node N) {
        RedoStack.enqueue(N);
    }

    public LinkedListDeque.Node RemoveUndo() {
        return UndoStack.dequeue();
    }

    public LinkedListDeque.Node RemoveRedo() {
        return RedoStack.dequeue();
    }

    public void NewRedo() {
        RedoStack = new ArrayRingBuffer<>(100);
    }

}
