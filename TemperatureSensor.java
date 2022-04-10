import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CyclicBarrier;

public class TemperatureSensor implements Runnable {

    private ConcurrentLinkedList<TemperatureReading> sharedMem;
    private AtomicInteger idCounter;
    private int minutes;
    private CyclicBarrier bar;

    public TemperatureSensor(ConcurrentLinkedList<TemperatureReading> sharedMem, AtomicInteger idCounter, int minutes, CyclicBarrier bar) {
        this.sharedMem = sharedMem;
        this.idCounter = idCounter;
        this.minutes = minutes;
        this.bar = bar;
    }

    @Override
    public void run() {
        int id = idCounter.getAndIncrement();
        Random r = new Random();

        for (int i = 0; i < minutes; i++) {
            // System.out.println("ID " + id);
            TemperatureReading t = new TemperatureReading(r.nextInt(170) - 100, id);
            while (!sharedMem.add(t));

            id = idCounter.getAndIncrement();
            try {
                bar.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}