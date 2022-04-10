import java.util.concurrent.atomic.AtomicMarkableReference;

public class TemperatureReading {
    int value;
    int readingId;

    public TemperatureReading(int value, int readingId) {
        this.value = value;
        this.readingId = readingId;
    }

    @Override
    public int hashCode() {
        return this.readingId;
    }

    public static TemperatureReading min(TemperatureReading a, TemperatureReading b) {
        return (a.value < b.value) ? a : b;
    }

    public static TemperatureReading max(TemperatureReading a, TemperatureReading b) {
        return (a.value >= b.value) ? a : b;
    }
}

