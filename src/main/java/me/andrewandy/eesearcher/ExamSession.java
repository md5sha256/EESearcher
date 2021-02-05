package me.andrewandy.eesearcher;

import org.jetbrains.annotations.NotNull;

import java.time.Month;
import java.util.Calendar;

public class ExamSession {

    public final int year;
    public final Month month;
    public final String displayName;

    private final int hashCode;

    private ExamSession(final int year, final Month month) {
        this.year = year;
        if (month != Month.MAY && month != Month.NOVEMBER) {
            throw new IllegalArgumentException(String.format("Invalid exam session month %s", month));
        }
        // 1974 was the year the EE was first introduced.
        if (year < 1974 || year > Calendar.getInstance().get(Calendar.YEAR)) {
            throw new IllegalArgumentException("Possible year for an exam session: " + year);
        }
        this.month = month;
        // Example: M21 or N20
        this.displayName = (month == Month.MAY ? "M" : "N") + year % 1000;
        this.hashCode = 13 * this.displayName.hashCode();
    }

    public static @NotNull ExamSession of(final Month month, int year) {
        return new ExamSession(year, month);
    }

    public static @NotNull ExamSession of(final String displayName) throws IllegalArgumentException {
        final char rawMonth = displayName.charAt(0);
        final Month month;
        switch (rawMonth) {
            case 'M':
            case 'm':
                month = Month.MAY;
                break;
            case 'N':
            case 'n':
                month = Month.NOVEMBER;
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid exam session month %s", rawMonth));
        }
        final int parsed = Integer.parseInt(displayName.substring(1));
        // Determine century.
        final int year = (parsed > 50 ? 1900 : 2000) + parsed;
        return of(month, year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamSession that = (ExamSession) o;

        return hashCode == that.hashCode;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
