import java.util.concurrent.atomic.AtomicMarkableReference;

public class ConcurrentLinkedList<T> {
    class Window {
        public Node<T> pred, curr;
        Window(Node<T> pred, Node<T> curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }

    private Node<T> head;

    public ConcurrentLinkedList() {
        this.head = new Node(Integer.MIN_VALUE);
    }

    public Window find(Node<T> head, int key) {
        Node<T> pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        boolean snip;
        retry: while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if(!snip) {
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.key >= key) {
                    return new Window(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    public boolean add(T x) {
        int key = x.hashCode();
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key == key) {
                return false;
            }
            else {
                Node<T> node = new Node(x);
                node.next = new AtomicMarkableReference<Node<T>>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    return true;
                }
            }
        }
    }

    public boolean remove(T x) {
        int key = x.hashCode();
        boolean snip;
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr.key != key) {
                return false;
            }
            else {
                Node<T> succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if (!snip) {
                    continue;
                }
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    }

    public boolean contains(T x) {
        boolean[] marked = {false};
        int key = x.hashCode();
        Node<T> curr = head;
        while (curr.key < key) {
            curr = curr.next.getReference();
            Node<T> succ = curr.next.get(marked);
        }
        return (curr.key == key && !marked[0]);
    }
}
