# cop4520-assignment3

Both problems were written in Java and compiled with openjdk 17.0.2+8-Debian-1deb11u1 (essentially just Java 17.0.2). This will probably work on older versions though. As the openjdk name implies, this was written, compiled, tested on a Debian 11 (Bullseye) Linux machine.

## Problem 1

To compile and run this program, just do:
  
    javac *.java
    time java Problem1

A Lock-Free Concurrent Linked List was implemented to solve this problem. Given a reference to the unordered bag (represented by a thread-unsafe ArrayList) and an AtomicInteger indicating the next gift to access, each servant works their way through the bag and adds it to the chain (represented by the concurrent linked list), representing the first action. On every k additions (represented by `ADD_REMOVE_RATIO`, initially set to 2, which means a removal for every addition), the servant will attempt to remove a random gift from the chain, representing the second action. Finally, each servant will search for a present on the chain at predetermined intervals (represented by `CHECK_CHAIN_INTERVAL_DIVIDEND`, which is initially set to 30, meaning a total of 30 checks will occur throughout execution).  This represents action 3.

This is by no means the fastest implementation. I could not get the book's lock-free list to work properly without an abundance of try-catch blocks and null-checking insanity. This is noticeable when `ADD_REMOVE_RATIO` is any number greater than 2. Bumping it up to 3 makes average execution time go from 250ms to 12s. However, it appears to be correct. We are guaranteed to never add or remove the same item twice from the chain, since we make use of a shared AtomicInteger to add unique items from the unordered bag, and we only remove from the front of the linked list. Contains calls will also always complete eventually, and generally do so quickly when `ADD_REMOVE_RATIO` is 2 (which essentially means the average size of the chain is 0 at any given point, since we add as often as we remove in that case). Threads will remove gifts from the chain once the unordered bag is "empty" and will only despawn when both the bag and the chain are empty.

Hyperparameters are provided via constants at the beginning of Servant.java (`CHECK_CHAIN_INTERVAL_DIVIDEND` and `ADD_REMOVE_RATIO`). I would not recommend an `ADD_REMOVE_RATIO` above 3. `CHECK_CHAIN_INTERVAL_DIVIDEND` can be tweaked rather generously, but I keep it at 30 to make output more readable. Of course, any tweaks require recompilation. Calls to `contains()` (for action 3) will unsurprisingly return false almost always when `ADD_REMOVE_RATIO` is 2 (again, since there will likely be very few elements in the list), so if you care enough, you can change `ADD_REMOVE_RATIO` to 3 to get verification that `contains()` actually works.

## Problem 2

Again, to compile and run this, just do:

    javac *.java
    time java Problem2
    
Each sensor is represented by a thread running the TemperatureSensor class. At the start of execution, the main thread (the rover itself) provides the shared memory (the same lock-free list implementation as Problem 1), a shared id counter (which helps the sensor account for time), and the total number of readings to perform to every sensor. Every minute, the sensor will make a reading and add it to the shared memory chain. The sensor will then wait until the other sensors are done performing their reading for that minute. A minute passes when all 8 sensors have made their readings. This sequence continues until the total number of required readings is achieved. 

Meanwhile, the rover asynchronously (regarding the sensors) writes a report every hour. PriorityQueues facilitate the storage of minimum and maximum readings; the rover goes through the shared memory chain and adds each reading to the queues. At the same time, for every reading, it looks ahead 10 "minutes" into the chain to calculate the differences between all 8 readings in that minute and the current reading. The largest differences are updated accordingly. Once the data for that hour is parsed, results are collected and printed. After this, the rover deletes this now obsolete data, making future reports just as efficient to compile. While the rover spends time creating these reports, the sensors are unhindered in their data collection, usually having new data ready for the rover as soon as it completes a report.

For correctness, the lock free list implicitly guarantees that the rover and the sensors do not collide. Multiple sensors can safely add to the list at once, and the ID for each reading is guaranteed to be unique thanks to the AtomicInteger counter. Sensors will always stop execution once the counter exceeds or equals the number of required readings. Given that every reading created will be successfully written to the chain, the rover will always find at LEAST the required number of readings to make a report (since time is controlled by the sensors working together and the rover relies on this time, which also satisfies the progress guarantee). It necessarily follows that all list operations involving those readings will eventually be successful. The bounds are carefully managed such that the rover never looks past that hour's readings. Given that these are all of the major concurrent operations taking place in the program, we can be assured that correctness has been achieved.

Efficiency is acceptable. The sensors are implemented trivially and will typically always be ahead of the rover. The rover makes use of efficient priorityqueues for min/max readings, but has to brute-force all reading differences within the hour given an interval. However, given that 1. old data is deleted once a report is made, and 2. there are only 480 readings per hour (when an hour is 60 minutes and there are 8 threads), only a few thousand differences (and nodes) are calculated (and accessed) per report, which is negligible. 
