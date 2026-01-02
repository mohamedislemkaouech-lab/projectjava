package tn.agricultureai.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for date and time operations.
 * Demonstrates: LocalDate/LocalDateTime API, date calculations.
 *
 * @author Your Name
 */
public final class DateUtils {

    private DateUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Common date formatters
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    public static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Get date range between two dates (inclusive)
     * Demonstrates: LocalDate operations, Stream generation
     */
    public static List<LocalDate> getDateRange(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }

        return dates;
    }

    /**
     * Calculate number of days between two dates
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate number of weeks between two dates
     */
    public static long weeksBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.WEEKS.between(start, end);
    }

    /**
     * Calculate number of months between two dates
     */
    public static long monthsBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Check if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(LocalDate.now());
    }

    /**
     * Check if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }

    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDate date) {
        return date.equals(LocalDate.now());
    }

    /**
     * Check if date is within last N days
     */
    public static boolean isWithinLastDays(LocalDate date, int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        return !date.isBefore(cutoff);
    }

    /**
     * Get start of month for a date
     */
    public static LocalDate startOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * Get end of month for a date
     */
    public static LocalDate endOfMonth(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * Get start of year for a date
     */
    public static LocalDate startOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * Get end of year for a date
     */
    public static LocalDate endOfYear(LocalDate date) {
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    /**
     * Get first day of quarter
     */
    public static LocalDate startOfQuarter(LocalDate date) {
        int month = date.getMonthValue();
        int quarterStartMonth = ((month - 1) / 3) * 3 + 1;
        return date.withMonth(quarterStartMonth).withDayOfMonth(1);
    }

    /**
     * Get last day of quarter
     */
    public static LocalDate endOfQuarter(LocalDate date) {
        return startOfQuarter(date).plusMonths(3).minusDays(1);
    }

    /**
     * Get quarter number (1-4) for a date
     */
    public static int getQuarter(LocalDate date) {
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * Check if two date ranges overlap
     */
    public static boolean rangesOverlap(
            LocalDate start1, LocalDate end1,
            LocalDate start2, LocalDate end2
    ) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    /**
     * Format date as display string
     */
    public static String formatDisplay(LocalDate date) {
        return date.format(DISPLAY_DATE);
    }

    /**
     * Format datetime as display string
     */
    public static String formatDisplay(LocalDateTime datetime) {
        return datetime.format(DISPLAY_DATETIME);
    }

    /**
     * Parse date from ISO string
     */
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, ISO_DATE);
    }

    /**
     * Parse datetime from ISO string
     */
    public static LocalDateTime parseDateTime(String datetimeString) {
        return LocalDateTime.parse(datetimeString, ISO_DATETIME);
    }

    /**
     * Get age in years from birth date
     */
    public static int getAgeInYears(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Add business days (skip weekends)
     */
    public static LocalDate addBusinessDays(LocalDate date, int days) {
        LocalDate result = date;
        int addedDays = 0;

        while (addedDays < days) {
            result = result.plusDays(1);
            if (result.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    result.getDayOfWeek() != DayOfWeek.SUNDAY) {
                addedDays++;
            }
        }

        return result;
    }

    /**
     * Check if date is a weekend
     */
    public static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * Check if date is a weekday
     */
    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    /**
     * Get relative time description (e.g., "2 days ago", "in 3 hours")
     */
    public static String getRelativeTime(LocalDateTime datetime) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(datetime, now);

        if (seconds < 0) {
            // Future
            seconds = -seconds;
            if (seconds < 60) return "in " + seconds + " seconds";
            if (seconds < 3600) return "in " + (seconds / 60) + " minutes";
            if (seconds < 86400) return "in " + (seconds / 3600) + " hours";
            return "in " + (seconds / 86400) + " days";
        } else {
            // Past
            if (seconds < 60) return seconds + " seconds ago";
            if (seconds < 3600) return (seconds / 60) + " minutes ago";
            if (seconds < 86400) return (seconds / 3600) + " hours ago";
            return (seconds / 86400) + " days ago";
        }
    }

    /**
     * Get current timestamp in milliseconds
     */
    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Convert LocalDateTime to epoch millis
     */
    public static long toEpochMillis(LocalDateTime datetime) {
        return datetime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Convert epoch millis to LocalDateTime
     */
    public static LocalDateTime fromEpochMillis(long epochMillis) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault()
        );
    }
}