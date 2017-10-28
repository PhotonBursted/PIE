package st.photonbur.misc.image;

import java.io.File;
import java.security.InvalidParameterException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Utils {
    public static int findLastIndexInDirectory(String dirPath, final String extension) {
        File directory = new File(dirPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (directory.isDirectory()) {

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

    public static LocalTime getTimeDifference(LocalTime tStart, LocalTime tEnd) {
        long diff = Math.floorMod(tEnd.toNanoOfDay() - tStart.toNanoOfDay(), LocalTime.MAX.toNanoOfDay());

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
