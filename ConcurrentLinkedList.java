import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentLinkedList<T> {
    class Window {
        public Node<T> pred, curr;
        Window(Node<T> pred, Node<T> curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }

    private Node<T> head;
    private AtomicInteger size;

    public ConcurrentLinkedList() {
        this.head = new Node(Integer.MIN_VALUE);
        this.head.next = new AtomicMarkableReference<Node<T>>(null, false);
        this.size = new AtomicInteger(0);
    }

    public Window find(Node<T> head, int key) {
        Node<T> pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        retry: while (true) {
            pred = head;
            // the list must be empty
            if (pred.next.getReference() == null) {
                return new Window(head, null);
            }
            curr = pred.next.getReference();
            while (true) {
                try {
                    succ = curr.next.get(marked);
                } catch (Exception e) {
                    continue retry;
                }
                while (marked[0]) {
                    try {
                        if (!pred.next.compareAndSet(curr, succ, false, false)) {
                            continue retry;
                        }
                    } catch (Exception e) {
                        continue retry;
                    }
                    // if true, the successor node is the end of the list (i.e. null)
                    if (succ == null) {
                        return new Window(pred, null);
                    }

                    curr = succ;
                    succ = (curr == null || curr.next == null) ? null : curr.next.get(marked);
                }
                // the predecessor node has a lower value and the current node has a higher value
                if (curr.key >= key) {
                    return new Window(pred, curr);
                }
                if (succ == null) {
                    return new Window(curr, null);
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
            Node<T> node = new Node(x);
            if (curr == null) {
                curr = node;
                node.next = new AtomicMarkableReference<Node<T>>(null, false);
                if (pred.next.compareAndSet(null, node, false, false)) {
                    size.getAndIncrement();
                    return true;
                }
            }
            else if (curr.key == key) {
                return false;
            }
            else {
                /*if (curr != null) {
                    node.next = new AtomicMarkableReference<Node<T>>(curr, false);
                }
                if (pred.next == null) {
                    pred.next = new AtomicMarkableReference<Node<T>>(node, false);
                    return true;
                }*/
                node.next = new AtomicMarkableReference<Node<T>>(curr, false);
                if (pred.next.compareAndSet(curr, node, false, false)) {
                    size.getAndIncrement();
                    return true;
                }
            }
        }
    }

    public boolean remove(T x) {
        int key = x.hashCode();
        while (true) {
            Window window = find(head, key);
            Node<T> pred = window.pred, curr = window.curr;
            if (curr == null || curr.key != key) {
                return false;
            }
            else if (curr != null) {
                Node<T> succ = null;
                if (curr.next != null) {
                    succ = curr.next.getReference();
                    if (!curr.next.compareAndSet(succ, succ, false, true)) {
                        continue;
                    }
                }
                if (pred.next != null) {
                    pred.next.compareAndSet(curr, succ, false, false);
                }
                size.getAndDecrement();
                return true;
            }
        }
    }

    public boolean contains(T x) {
        
        T ret = get(x);
        return ret != null;
    }

    public T get(T x) {
        boolean[] marked = {false};
        int key = x.hashCode();
        Node<T> curr = head;
        while (curr != null && curr.next != null && curr.key < key) {
            curr = curr.next.getReference();
            if (curr != null && curr.next != null) {
                Node<T> succ = curr.next.get(marked);
            }
        }
        if (curr != null && curr.key == key && !marked[0]) {
            return curr.item;
        }
        else {
            return null;
        }
    }

    public T poll() {
        try {
            return head.next.getReference().item;
        } catch (Exception e) {
            return null;
        }
    }

    public int size() {
        return size.get();
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
