package com.itzrozzadev.fo.exception;

import lombok.Getter;

/**
 * Thrown when we load data from FoConstants.DATA but they have a location with a world
 * that no longer exists
 */
public final class InvalidWorldException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * The world that was invali
	 */
	@Getter
	private final String world;

	public InvalidWorldException(final String message, final String world) {
		super(message);

		this.world = world;
	}
}