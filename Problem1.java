import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class Problem1 {
    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) throws InterruptedException {
        int gifts = 500000;
        ArrayList<Integer> seqBag = new ArrayList<Integer>();

        for (int i = 0; i < gifts; i++) {
            seqBag.add(i);
        }
        Collections.shuffle(seqBag);
        System.out.println("Shuffled the bag...");

        ConcurrentLinkedList<Integer> chain = new ConcurrentLinkedList<>();
        AtomicInteger giftCounter = new AtomicInteger(0);

        Thread[] servants = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            servants[i] = new Thread(new Servant(chain, seqBag, giftCounter, gifts), "" + i);
            servants[i].start();
        }
        for (Thread t : servants) {
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done!");
    }
}