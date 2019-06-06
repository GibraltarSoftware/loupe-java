package com.onloupe.core.util;

import java.awt.font.NumericShaper.Range;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.onloupe.model.exception.GibraltarException;

public class TimeConversionTests {

	private final Random random = new Random();

	@Test
	public void testTickConversion() {
		// 1 tick = 100 nanos
		Assertions.assertEquals(1, TimeConversion.durationInTicks(Duration.ofNanos(100)));

		// check the other time units
		Assertions.assertEquals(TimeConversion.ticksPerDay, TimeConversion.durationInTicks(Duration.ofDays(1)));
		
		Assertions.assertEquals(TimeConversion.ticksPerWeek,
				TimeConversion.durationInTicks(Duration.ofDays(TimeConversion.daysPerWeek)));
		
		Assertions.assertEquals(TimeConversion.ticksPerYear,
				TimeConversion.durationInTicks(Duration.ofDays(TimeConversion.daysPerYear)));
		
		Assertions.assertEquals(TimeConversion.ticksPerCentury,
				TimeConversion.durationInTicks(Duration.ofDays(TimeConversion.daysPerCentury)));
	}
	
	@Test
	public void testDurationInTicks() {
		// sample 50 random longs
		Iterator<Long> testValues = random.longs(50).iterator();
		while (testValues.hasNext()) {
			long value = Math.abs(testValues.next());
			Duration duration = TimeConversion.durationOfTicks(value);
			Assertions.assertEquals(value, TimeConversion.durationInTicks(duration));
		}		
	}

	@Test
	public void testEpochTicks() {
		// sample every zone offset
		Iterator<Integer> offsets = IntStream.range(-18, 19).iterator();
		while (offsets.hasNext()) {
			long ticks = Math.abs(random.nextLong());
			OffsetDateTime offsetDateTime = TimeConversion.fromEpochTicks(ticks, ZoneOffset.ofHours(offsets.next()));
			Assertions.assertEquals(ticks, TimeConversion.epochTicks(offsetDateTime));
		}
	}
	
	@Test
	public void testNegativeDurationFails() {
		// test non acceptance of negative numbers
		Assertions.assertThrows(GibraltarException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				TimeConversion.durationOfTicks(-12345);
			}
		});

		Assertions.assertThrows(GibraltarException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				TimeConversion.fromEpochTicks(-67890, ZoneOffset.UTC);
			}
		});
	}
	
	@Test
	public void removeRange() {
		List<Integer> list = IntStream.range(0, 10).boxed().collect(Collectors.toList());
		
		System.out.print(list.toString());

		int committedListSize = 3;
		for (int i = list.size() - 1; i >= committedListSize; i--) {
			list.remove(i);
		}
		
		System.out.print(list.toString());
	}
}
