package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * <p>
 * Holds the (non-animated) static sprite for an object as a
 * {@link TextureRegion}.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class StaticSprite implements Component {
	public TextureRegion	textureRegion	= null;

	@Override
	public void reset() {
		textureRegion = null;
	}

}
