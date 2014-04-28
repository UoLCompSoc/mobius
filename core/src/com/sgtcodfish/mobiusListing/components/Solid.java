package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Rectangle;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class Solid implements Component {
	public static final float	DEFAULT_WEIGHT	= 1.0f;

	public boolean				invertedGravity	= false;
	public float				weight			= DEFAULT_WEIGHT;

	public Rectangle			boundingBox		= null;

	public float getGravitySignum() {
		return (!invertedGravity ? 1.0f : -1.0f);
	}

	@Override
	public void reset() {
		boundingBox = null;
		weight = DEFAULT_WEIGHT;
		invertedGravity = false;
	}
}
