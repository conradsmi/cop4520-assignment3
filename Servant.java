import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Servant implements Runnable {
    private final int CHECK_CHAIN_INTERVAL_DIVIDEND = 30;

    private ConcurrentSkipListSet<Integer> chain;
    private ConcurrentLinkedQueue<Integer> bag;
    private int bagSize;
    private int checkChain;
    private int checkChainInterval;

    public Servant(ConcurrentSkipListSet<Integer> chain, ConcurrentLinkedQueue<Integer> bag, int bagSize) {
        this.chain = chain;
        this.bag = bag;
        this.bagSize = bagSize;
        this.checkChain = 0;
        this.checkChainInterval = bagSize / CHECK_CHAIN_INTERVAL_DIVIDEND;
    }

    @Override
    public void run() {
        while (!bag.isEmpty()) {
            if (checkChain < checkChainInterval) {
                if (checkChain % 2 == 0 && !bag.isEmpty()) {
                    chain.add(bag.poll());
                }
                else if (!chain.isEmpty()) {
                    chain.remove(chain.first());
                }
                checkChain++;
            }
            else {
                Random r = new Random();
                int gift = r.nextInt(bagSize);
                long tid = Thread.currentThread().getId();
                String status = chain.contains(gift) ? "found" : "not found";
                System.out.println("Servant " + tid + " attempted to find gift " + gift + ": " + status);
                checkChain = 0;
            }
        }
    }
}