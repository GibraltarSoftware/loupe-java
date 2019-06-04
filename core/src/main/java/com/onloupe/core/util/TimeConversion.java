package com.onloupe.core.util;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.onloupe.model.exception.GibraltarException;

// TODO: Auto-generated Javadoc
/**
 * Helper class to assist with c# -> Java temporal behavior, and converting time definitions to manageable surrogates.
 * 
 * 1 tick = 100 nanos. The Loupe server deals in ticks and java has no native type for this, so we need to improvise.
 * 
 * @author RyanKelliher
 *
 */
public class TimeConversion {

	/**
	 * Express the minimum value of an OffsetDateTime, corresponding to .NET
	 */
	public static final OffsetDateTime MIN = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

	/**
	 * Express the ISO 8601 format of OffsetDateTime, corresponding to .NET
	 */
	public static final DateTimeFormatter CS_DATETIMEOFFSET_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS[XXX]");

	/** The Constant ticksPerDay. */
	public static final long ticksPerDay = 864000000000L;
	
	/** The Constant ticksPerWeek. */
	public static final long ticksPerWeek = 6048000000000L;
	
	/** The Constant ticksPerYear. */
	public static final long ticksPerYear = 315360000000000L;
	
	/** The Constant ticksPerCentury. */
	public static final long ticksPerCentury = 31556736000000000L;

	/** The Constant daysPerWeek. */
	public static final long daysPerWeek = ChronoUnit.WEEKS.getDuration().toDays();
	
	/** The Constant daysPerYear. */
	public static final long daysPerYear = ChronoUnit.YEARS.getDuration().toDays();
	
	/** The Constant daysPerCentury. */
	public static final long daysPerCentury = ChronoUnit.CENTURIES.getDuration().toDays();

	/**
	 * Return the value of a duration in ticks.
	 *
	 * @param duration the duration
	 * @return the long
	 */
	public static long durationInTicks(Duration duration) {
		if (duration == null)
			duration = Duration.ZERO;
		
		if (duration.toDays() > 0) {
			return durationInTicksLarge(duration);
		}
		return Math.floorDiv(duration.toNanos(), 100);
	}

	/**
	 * Duration in ticks large.
	 *
	 * @param duration the duration
	 * @return the long
	 */
	private static long durationInTicksLarge(Duration duration) {
		long ticks = 0;

		while (duration.toDays() > 0) {
			if (duration.toDays() > daysPerCentury) {
				ticks = Math.addExact(ticks, ticksPerCentury);
				duration = duration.minusDays(daysPerCentury);
			} else if (duration.toDays() > daysPerYear) {
				ticks = Math.addExact(ticks, ticksPerYear);
				duration = duration.minusDays(daysPerYear);
			} else if (duration.toDays() > daysPerWeek) {
				ticks = Math.addExact(ticks, ticksPerWeek);
				duration = duration.minusDays(daysPerWeek);
			} else {
				ticks = Math.addExact(ticks, ticksPerDay);
				duration = duration.minusDays(1);
			}
		}

		ticks += durationInTicks(duration);
		return ticks;
	}

	/**
	 * Return the relative duration from a number of ticks.
	 *
	 * @param ticks the ticks
	 * @return the duration
	 */
	public static Duration durationOfTicks(long ticks) {
		if (ticks < 0) {
			throw new GibraltarException("Tick value must not be negative.");
		}
		
		if (ticks > ticksPerDay) {
			return durationOfTicksLarge(ticks);
		}
		return Duration.ofNanos(Math.multiplyExact(ticks, 100));
	}

	/**
	 * Duration of ticks large.
	 *
	 * @param ticks the ticks
	 * @return the duration
	 */
	private static Duration durationOfTicksLarge(long ticks) {
		Duration duration = Duration.ZERO;
		while (ticks > ticksPerDay) {
			if (ticks > ticksPerCentury) {
				duration = duration.plusDays(daysPerCentury);
				ticks = Math.subtractExact(ticks, ticksPerCentury);
			} else if (ticks > ticksPerYear) {
				duration = duration.plusDays(daysPerYear);
				ticks = Math.subtractExact(ticks, ticksPerYear);
			} else if (ticks > ticksPerWeek) {
				duration = duration.plusDays(daysPerWeek);
				ticks = Math.subtractExact(ticks, ticksPerWeek);
			} else {
				duration = duration.plusDays(1);
				ticks = Math.subtractExact(ticks, ticksPerDay);
			}
		}

		return duration.plus(durationOfTicks(ticks));
	}

	/**
	 * Return the number of ticks from MIN to the provided offsetDateTime.
	 *
	 * @param offsetDateTime the offset date time
	 * @return the long
	 */
	public static long epochTicks(OffsetDateTime offsetDateTime) {
		try {
			return durationInTicks(Duration.between(MIN, offsetDateTime));
		} catch (Exception e) {
			return 0L;
		}
	}

	/**
	 * Return an offsetDateTime value of the number of ticks provided and in the
	 * offset provided.
	 *
	 * @param ticks the ticks
	 * @param offSet the off set
	 * @return the offset date time
	 */
	public static OffsetDateTime fromEpochTicks(long ticks, ZoneOffset offSet) {
		if (offSet == null)
			throw new GibraltarException("Zone offset must not be null.");
		
		if (ticks < 0) {
			throw new GibraltarException("Tick value must not be negative.");
		}
		
		return OffsetDateTime.ofInstant(MIN.plus(durationOfTicks(ticks)).toInstant(), ZoneId.of(offSet.getId()));
	}

}
