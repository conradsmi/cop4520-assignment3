import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;

public class Servant implements Runnable {
    private final int CHECK_CHAIN_INTERVAL_DIVIDEND = 30;
    private final int ADD_REMOVE_RATIO = 3;

    // private ConcurrentSkipListSet<Integer> chain;
    // private ConcurrentLinkedQueue<Integer> bag;
    private ConcurrentLinkedList<Integer> chain;
    private ArrayList<Integer> bag;
    private AtomicInteger giftCounter;
    private int bagSize;
    private int checkChain;
    private int checkChainInterval;

    public Servant(ConcurrentLinkedList<Integer> chain, ArrayList<Integer> bag, AtomicInteger giftCounter, int bagSize) {
        this.chain = chain;
        this.bag = bag;
        this.giftCounter = giftCounter;
        this.bagSize = bagSize;
        this.checkChain = 0;
        this.checkChainInterval = bagSize / CHECK_CHAIN_INTERVAL_DIVIDEND;
    }

    @Override
    public void run() {
        while (giftCounter.get() < bagSize) {
            if (checkChain < checkChainInterval) {
                if (checkChain % ADD_REMOVE_RATIO != 0 && !bag.isEmpty()) {
                    int temp = giftCounter.getAndIncrement();
                    if (temp < bagSize) {
                        Integer item = bag.get(giftCounter.getAndIncrement());
                        if (item != null) {
                            chain.add(item);
                        }
                    }
                }
                else {
                    Integer item = chain.poll();
                    if (item != null) {
                        chain.remove(item);
                    }
                }
                checkChain++;
            }
            else {
                Random r = new Random();
                int gift = r.nextInt(bagSize);
                long tid = Thread.currentThread().getId();
                boolean res = chain.contains(gift);
                
                String status = res ? "found" : "not found";
                System.out.println("Servant " + tid + " attempted to find gift " + gift + ": " + status + ". Items left to add: " + (bagSize - giftCounter.get()));
                checkChain = 0;
            }
        }
        while (chain.size() > 0) {
            Integer item = chain.poll();
            if (item != null) {
                chain.remove(item);
            }
        }
    }
}