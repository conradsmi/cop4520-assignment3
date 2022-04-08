import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Problem1 {
    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) throws InterruptedException {
        int gifts = 500000;
        ArrayList<Integer> seqBag = new ArrayList<Integer>();

        for (int i = 0; i < gifts; i++) {
            seqBag.add(i);
        }
        Collections.shuffle(seqBag);

        ConcurrentSkipListSet<Integer> chain = new ConcurrentSkipListSet<>();
        ConcurrentLinkedQueue<Integer> conBag = new ConcurrentLinkedQueue<>(seqBag);

        Thread[] servants = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            servants[i] = new Thread(new Servant(chain, conBag, gifts), "" + i);
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