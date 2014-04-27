package com.sgtcodfish.mobiusListing.components;

import com.badlogic.gdx.maps.MapProperties;

/**
 * <p>
 * Holds a movable layer. Override and implement interact for specific
 * directions.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public abstract class MovingLayer extends InteractableLayer<Position> {
	public int	deltaPos		= 0;
	public int	maxDiff			= 0;

	// the actual direction the layer will move when going from deltaPos 0 ->
	// maxDiff
	// 1 = right/up
	// -1 = left/down
	public int	direction		= 1;

	// the direction we're currently moving in
	public int	tempDirection	= 1;

	@Override
	public void interact(Position p, int degree) {
		deltaPos += tempDirection * degree;

		move(p, degree * tempDirection);

		if (deltaPos >= maxDiff) {
			deltaPos = maxDiff;
			tempDirection = -1;
		} else if (deltaPos <= 0) {
			deltaPos = 0;
			tempDirection = 1;
		}
	}

	protected abstract void move(Position p, int degree);

	protected void tiledPropertiesHelper(MapProperties properties, String propertyString, boolean flipMax) {
		String prop = (String) properties.get(propertyString, null, String.class);

		if (prop != null) {
			int max = 0;

			try {
				max = Integer.parseInt(prop);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Parsing error when loading MovingLayer (" + propertyString
						+ "); no max found.");
			}

			direction = (flipMax ? -1 : 1) * Integer.signum(max);

			if (direction == 0) {
				throw new IllegalArgumentException("MovingLayer (" + propertyString
						+ "): No valid value for property found.");
			}

			maxDiff = Math.abs(max);
		} else {
			throw new IllegalArgumentException("Attempting to load MovingLayer (" + propertyString
					+ ") from a layer with no valid property.");
		}
	}

	@Override
	public void reset() {
		super.reset();
		deltaPos = 0;
		maxDiff = 0;
		direction = 1;
		tempDirection = 1;
	}
}
