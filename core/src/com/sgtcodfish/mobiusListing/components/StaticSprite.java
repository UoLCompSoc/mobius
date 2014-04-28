package com.sgtcodfish.mobiusListing.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * <p>
 * Holds the (non-animated) static sprite for an object as a
 * {@link TextureRegion}.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class StaticSprite extends MobiusSprite {
	public TextureRegion	textureRegion	= null;

	@Override
	public void reset() {
		super.reset();
		textureRegion = null;
	}
}
