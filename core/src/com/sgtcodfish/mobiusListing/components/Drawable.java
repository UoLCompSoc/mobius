package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Texture;

/**
 * Indicates that the Entity is renderable in some way.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Drawable implements Component {
	public Texture	texture	= null;

	public Drawable() {
	}

	@Override
	public void reset() {
		if (texture != null) {
			texture.dispose();
		}

		texture = null;
	}

}
