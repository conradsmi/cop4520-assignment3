import java.util.concurrent.atomic.AtomicMarkableReference;

public class Node<T> {
    T item;
    int key;
    AtomicMarkableReference<Node<T>> next;

    public Node(T item) {
        this.item = item;
        this.key = this.item.hashCode();
        this.next = null;
    }
}
