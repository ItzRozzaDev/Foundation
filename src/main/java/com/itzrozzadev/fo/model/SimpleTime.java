package com.itzrozzadev.fo.model;

import com.itzrozzadev.fo.TimeUtil;
import lombok.Getter;
import lombok.NonNull;

/**
 * A simple class holding time values in human readable form such as 1 second or 5 minutes
 */
@Getter
public class SimpleTime {

	private final String raw;
	private long timeTicks;

	protected SimpleTime(@NonNull final String time) {
		if ("0".equals(time) || "none".equalsIgnoreCase(time)) {
			this.timeTicks = TimeUtil.toTicks(time);
			this.raw = "0";
			this.timeTicks = 0;

		} else {
			this.raw = time;
			this.timeTicks = TimeUtil.toTicks(time);
		}
	}

	/**
	 * Generate new time from the given seconds
	 *
	 * @param seconds
	 * @return
	 */
	public static SimpleTime fromSeconds(final int seconds) {
		return from(seconds + " seconds");
	}

	/**
	 * Generate new time. Valid examples: 15 ticks 1 second 25 minutes 3 hours etc.
	 *
	 * @param time
	 * @return
	 */
	public static SimpleTime from(final String time) {
		return new SimpleTime(time);
	}

	/**
	 * Get the time specified in seconds (ticks / 20)
	 *
	 * @return
	 */
	public long getTimeSeconds() {
		return this.timeTicks / 20L;
	}

	/**
	 * Get the time specified in ticks
	 * *WARNING* if the time ticks is over {@value Integer#MAX_VALUE} it will overflow!
	 *
	 * @return
	 */
	public int getTimeTicks() {
		return (int) this.timeTicks;
	}

	public String getRaw() {
		return this.timeTicks == 0 ? "0" : this.raw;
	}

	@Override
	public String toString() {
		return this.raw;
	}
}