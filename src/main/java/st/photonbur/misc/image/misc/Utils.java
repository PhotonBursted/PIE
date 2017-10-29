package st.photonbur.misc.image.misc;

import java.io.File;
import java.security.InvalidParameterException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Utils {
    /**
     * Finds the integer value of the file with the highest index in its file name.
     *
     * @param dirPath   The directory to search in
     * @param extension The extension to filter on
     * @return The integer depicting the highest current indexed file in the directory
     */
    public static int findLastIndexInDirectory(String dirPath, final String extension) {
        File directory = new File(dirPath);

        // Check if the directory exists
        if (!directory.exists()) return 0;

        // Check if the directory actually is a directory
        if (directory.isDirectory()) {
            // Find the highest index present in the directory
            //noinspection ConstantConditions
            return Arrays.stream(directory.listFiles((dir, name) -> {
                int i = name.lastIndexOf(".");

                return i >= 0 && name.substring(i + 1).equalsIgnoreCase(extension);
            }))
                    .mapToInt(file -> Integer.parseInt(file.getName().split("\\.")[0]))
                    .max().orElse(0);
        } else {
            throw new InvalidParameterException("dirPath was not a directory");
        }
    }

    /**
     * Calculates the difference in time between two instances of {@link LocalTime}.
     *
     * @param tStart The time marking the beginning of an event
     * @param tEnd   The time marking the end of an event
     * @return The difference in time between the two passed times. This wraps around towards tEnd.
     */
    public static LocalTime getTimeDifference(LocalTime tStart, LocalTime tEnd) {
        // Calculate the difference in nanoseconds
        long diff = Math.floorMod(tEnd.toNanoOfDay() - tStart.toNanoOfDay(), LocalTime.MAX.toNanoOfDay());

        // Convert the long into a new LocalTime object step by step
        int hour = (int) TimeUnit.NANOSECONDS.toHours(diff);
        diff -= TimeUnit.HOURS.toNanos(hour);
        int mins = (int) TimeUnit.NANOSECONDS.toMinutes(diff);
        diff -= TimeUnit.MINUTES.toNanos(mins);
        int secs = (int) TimeUnit.NANOSECONDS.toSeconds(diff);
        diff -= TimeUnit.SECONDS.toNanos(secs);
        int nano = (int) diff;

        return LocalTime.of(hour, mins, secs, nano);
    }
}
