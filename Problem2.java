import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class Problem2 {
    public static final int TOTAL_REPORTS = 100;
    public static final int THREAD_COUNT = 8;
    public static final int HOUR = 60;
    public static final int INTERVAL = 10;
    public static final int RESULTS_LIMIT = 5;

    public static void main(String[] args) throws InterruptedException {
        // the number of minutes this execution encompasses
        int minutes = HOUR * TOTAL_REPORTS;
        // the total number of readings all sensors combined will take
        int readings = minutes * THREAD_COUNT;
        int readingsPerHour = HOUR * THREAD_COUNT;

        ConcurrentLinkedList<TemperatureReading> sharedMem = new ConcurrentLinkedList<>();
        AtomicInteger idCounter = new AtomicInteger(0);

        // start threads
        Thread[] sensors = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            sensors[i] = new Thread(new TemperatureSensor(sharedMem, idCounter, readings), "" + i);
            sensors[i].start();
        }

        int reportsDone = 0;
        int reportsToDo = readings / readingsPerHour;
        int nextReportTime = HOUR * THREAD_COUNT;
        while (reportsDone < reportsToDo) {
            if (idCounter.get() - nextReportTime >= 0) {
                // make a report
                int lower = reportsDone * HOUR * THREAD_COUNT, upper = ++reportsDone * HOUR * THREAD_COUNT;
                // we will get min and max readings by taking the first RESULTS_LIMIT elements from a priorityqueue
                // containing all readings for the hour
                PriorityQueue<TemperatureReading> min = new PriorityQueue(upper - lower);
                PriorityQueue<TemperatureReading> max = new PriorityQueue(upper - lower, Collections.reverseOrder());
                // keeping track of the actual TemperatureReading objects involved in the difference
                // lets us print them out later
                TemperatureReading[] largestDiff = new TemperatureReading[2];
                int largestDiffVal = 0;

                // calculate all possible intervals for this thread
                for (int i = lower; i < upper; i++) {
                    TemperatureReading t = sharedMem.get(new TemperatureReading(Integer.MIN_VALUE, i));
                    min.add(t);
                    max.add(t);
                    // we need to look 10 minutes ahead
                    int jlower = ((i / THREAD_COUNT) * THREAD_COUNT) + (THREAD_COUNT * INTERVAL);
                    int jupper = (((i + THREAD_COUNT) / THREAD_COUNT) * THREAD_COUNT) + (THREAD_COUNT * INTERVAL);
                    for (int j = jlower; j < jupper && j < upper - INTERVAL; j++) {
                        TemperatureReading u = null;
                        // sometimes, get() will fail. we do not want that to happen
                        while (u == null) {
                            u = sharedMem.get(new TemperatureReading(Integer.MIN_VALUE, j));
                        }
                        int diff = Math.abs(u.value - t.value);
                        if (largestDiffVal < diff) {
                            largestDiff[0] = t;
                            largestDiff[1] = u;
                            largestDiffVal = diff;
                        }
                    }
                    // we no longer need this reading's information
                    // discard it to make future linked list operations faster
                    sharedMem.remove(t);
                }

                // get results from the priority queues
                ArrayList<TemperatureReading> minResults = new ArrayList();
                ArrayList<TemperatureReading> maxResults = new ArrayList();
                minResults.add(min.poll());
                maxResults.add(max.poll());
                for (int i = 1; i < RESULTS_LIMIT && !min.isEmpty();) {
                    TemperatureReading temp = min.poll();
                    if(minResults.get(i-1).value != temp.value) {
                        i++;
                        minResults.add(temp);
                    }
                }
                for (int i = 1; i < RESULTS_LIMIT && !max.isEmpty();) {
                    TemperatureReading temp = max.poll();
                    if(maxResults.get(i-1).value != temp.value) {
                        i++;
                        maxResults.add(temp);
                    }
                }

                // print report
                System.out.println("Report " + reportsDone + "...");
                System.out.println("    Top " + minResults.size() + " lowest unique recordings:");
                for (TemperatureReading t : minResults) {
                    System.out.println(t.value + " degrees at minute " + t.readingId / THREAD_COUNT);
                }
                System.out.println("    Top " + maxResults.size() + " highest unique recordings: ");
                for (TemperatureReading t : maxResults) {
                    System.out.println(t.value + " degrees at minute " + t.readingId / THREAD_COUNT);
                }
                System.out.println("    Largest interval: " + largestDiffVal + " degree change between minutes "
                                    + largestDiff[0].readingId / THREAD_COUNT + " and " + largestDiff[1].readingId / THREAD_COUNT);
            }
        }
        // join threads
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