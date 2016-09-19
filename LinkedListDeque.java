package editor;
import javafx.scene.text.Text;

public class LinkedListDeque<Item> {
	public Node sentinel = new Node (new Text(5, 0, ""), null, null);
	private int currentPos;
    public Node currentNode = sentinel;
	public int size;


	public class Node {
		public Text item;
		public Node next;
		public Node prev;
        public int operation;


		private Node (Text i, Node p, Node n) {
			item = i;
			prev = p;
			next = n;
		}
	}

	public LinkedListDeque() {
		size = 0;
	}

	public void addFirst(Text x) {
		if (sentinel.next == null) {
			sentinel.next = new Node (x, sentinel, sentinel);
			sentinel.prev = sentinel.next;
			size ++;
		}
		else {
			sentinel.next = new Node (x, sentinel, sentinel.next);
			sentinel.next.next.prev = sentinel.next;
			size ++;
		}
	}

    public void addCurrent(Text x) {
        if (currentNode.next == null) {
            currentNode.next = new Node(x, currentNode, currentNode);
            currentNode.prev = currentNode.next;
            currentNode = currentNode.next;
            currentPos++;
        }
        else {
            currentNode.next = new Node(x, currentNode, currentNode.next);
            currentNode.next.next.prev = currentNode.next;
            currentNode = currentNode.next;
        }
        size++;
    }

    public void moveLeft() {
        if (currentNode != sentinel) {
            currentNode = currentNode.prev;
            currentPos--;
        }
    }

    public void moveRight() {
        if (currentNode.next != sentinel) {
            currentNode = currentNode.next;
            currentPos++;
        }
    }

    public void moveUp(int pos) {

    }

	public void addLast(Text x) {
		if (sentinel.prev == null) {
			sentinel.prev = new Node (x, sentinel, sentinel);
			sentinel.next = sentinel.prev;
			size ++;
		}
		else {
			sentinel.prev = new Node (x, sentinel.prev, sentinel);
			sentinel.prev.prev.next = sentinel.prev;
			size ++;
		}
	}

	public boolean isEmpty() {
		if (size == 0) {
			return true;
		}
		else {
			return false;
		}
	}



	public int size() {
		return size;
	}

	public void printDeque() {
		Node start = sentinel.next;
		if (size > 0) {
			while (start != sentinel) {
				System.out.print(start.item + " ");
				start = start.next;
			}
		}
	}

	public Text removeFirst() {
		if (size == 0) {
			return null;
		}
		Node front = sentinel.next;
		sentinel.next = sentinel.next.next;
		sentinel.next.prev = sentinel;
		size --;
		return front.item;

	}

    public Text removeCurrent() {
        if (size == 0) {
            return null;
        }
        else {
            Text Buffer = currentNode.item;
            currentNode.prev.next = currentNode.next;
            currentNode.next.prev = currentNode.prev;
            currentNode = currentNode.prev;
            currentPos--;
            size--;
            return Buffer;
        }
    }

	public Text removeLast() {
		if (size == 0) {
			return null;
		}
		Node back = sentinel.prev;
		sentinel.prev = sentinel.prev.prev;
		sentinel.prev.next = sentinel;
		size --;
		return back.item;

	}

	public Text get(int index) {
		if (size <= index) {
			return null;
		}
		int count = 0;
		Node target = sentinel.next;
		while (count != index) {
			target = target.next;
			count ++;

		}
		return target.item;

	}

	private Text helper(int i, Node n) {
		if (i == 0) {
			return n.item;
		}
		else {
			return helper (i - 1, n.next);
		}
	}

	public Text getRecursive(int index) {
		if (size <= index) {
			return null;
		}
		Node copy = sentinel.next;
		return helper(index, copy);
	}
}