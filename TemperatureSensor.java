import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

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
                TemperatureReading t = new TemperatureReading(r.nextInt(171) - 100, id);
                sharedMem.add(t);
                id = idCounter.getAndIncrement();
            }
        }
    }
}