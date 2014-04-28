package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.sgtcodfish.mobiusListing.WorldConstants;

/**
 * <p>
 * Indicates that the component's alpha value can vary between 0% and 100% (0.0f
 * and 1.0f).
 * </p>
 * <p>
 * <em>Note: {@link Entity}s without this component are rendered with a=1.0f.</em>
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Opacity implements Component {
	// should be between 0.0f and 1.0f inclusive, corresponds to a percentage.
	public float	opacity	= 1.0f;

	public Opacity() {
		reset();
	}

	/**
	 * Sets the opacity according to
	 */
	public void setTransparent() {
		opacity = WorldConstants.GLOBAL_PASSTHROUGH_OPACITY;
	}

	public void setOpaque() {
		opacity = WorldConstants.GLOBAL_SOLID_OPACITY;
	}

	@Override
	public void reset() {
		opacity = 1.0f;
	}
}
