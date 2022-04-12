import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CyclicBarrier;

public class TemperatureSensor implements Runnable {

    private ConcurrentLinkedList<TemperatureReading> sharedMem;
    private AtomicInteger idCounter;
    private int readings;

    public TemperatureSensor(ConcurrentLinkedList<TemperatureReading> sharedMem, AtomicInteger idCounter, int readings) {
        this.sharedMem = sharedMem;
        this.idCounter = idCounter;
        this.readings = readings;
    }

    @Override
    public void run() {
        int id = idCounter.getAndIncrement();
        Random r = new Random();

        while (id < readings) {
            if (id < idCounter.get()) {
                // System.out.println("ID " + id);
                TemperatureReading t = new TemperatureReading(r.nextInt(170) - 100, id);
                while (!sharedMem.add(t));
                id = idCounter.getAndIncrement();
            }
        }
    }
}