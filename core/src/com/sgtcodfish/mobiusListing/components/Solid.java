package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class Solid implements Component {
	public static final float	DEFAULT_WEIGHT	= 1.0f;

	public boolean				invertedGravity	= false;
	public float				weight			= DEFAULT_WEIGHT;

	public float getGravitySignum() {
		return (!invertedGravity ? 1.0f : -1.0f);
	}

	@Override
	public void reset() {
		weight = DEFAULT_WEIGHT;
		invertedGravity = false;
	}
}
