import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CyclicBarrier;

public class Problem2 {
    public static final int THREAD_COUNT = 8;
    public static final int HOUR = 60;
    public static final int INTERVAL = 10;

    public static void main(String[] args) throws InterruptedException {
        int minutes = HOUR * 100;

        ConcurrentLinkedList<TemperatureReading> sharedMem = new ConcurrentLinkedList<>();
        AtomicInteger idCounter = new AtomicInteger(0);

        Thread[] sensors = new Thread[THREAD_COUNT];
        CyclicBarrier bar = new CyclicBarrier(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            sensors[i] = new Thread(new TemperatureSensor(sharedMem, idCounter, minutes, bar), "" + i);
            sensors[i].start();
        }
        int reportsDone = 0;
        int reportsToDo = minutes / HOUR;
        int nextReportTime = HOUR * THREAD_COUNT;
        while (reportsDone < reportsToDo) {
            int id = idCounter.get();
            if (id - nextReportTime >= 0) {
                // make a report
                TemperatureReading min = new TemperatureReading(Integer.MAX_VALUE, -1);
                TemperatureReading max = new TemperatureReading(Integer.MIN_VALUE, -1);
                TemperatureReading[] largestDiff = new TemperatureReading[2];
                int largestDiffVal = 0;
                int lower = reportsDone * HOUR * THREAD_COUNT, upper = ++reportsDone * HOUR * THREAD_COUNT;
                for (int i = lower; i < upper; i++) {
                    TemperatureReading t = sharedMem.get(new TemperatureReading(Integer.MIN_VALUE, i));
                    min = TemperatureReading.min(t, min);
                    max = TemperatureReading.max(t, max);
                    int jlower = i + (THREAD_COUNT * INTERVAL);
                    int jupper = (i + THREAD_COUNT) + (THREAD_COUNT * INTERVAL);
                    for (int j = jlower; j < jupper && j < upper - 10; j++) {
                        TemperatureReading u = sharedMem.get(new TemperatureReading(Integer.MIN_VALUE, j));
                        int diff = Math.abs(u.value - t.value);
                        if (largestDiffVal < diff) {
                            largestDiff[0] = t;
                            largestDiff[1] = u;
                            largestDiffVal = diff;
                        }
                    }
                    sharedMem.remove(t);
                }
                System.out.println("Report " + reportsDone + "...");
                System.out.println("    Lowest recording: " + min.value + " degrees at minute " + min.readingId / THREAD_COUNT);
                System.out.println("    Highest recording: " + max.value + " degrees at minute " + max.readingId / THREAD_COUNT);
                System.out.println("    Largest interval: " + largestDiffVal + " degree change between minutes "
                                    + largestDiff[0].readingId / THREAD_COUNT + " and " + largestDiff[1].readingId / THREAD_COUNT);
            }
        }
        for (Thread t : sensors) {
            try {
                t.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done!");
    }
}