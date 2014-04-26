package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Texture;
import com.sgtcodfish.mobiusListing.SpriteRenderHandler;

/**
 * Indicates that the Entity is renderable in some way.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Renderable implements Component {
	public Texture			texture			= null;
	public SpriteRenderHandler	renderHandler	= null;

	public Renderable() {
	}

	@Override
	public void reset() {
		if (texture != null) {
			texture.dispose();
		}

		texture = null;
		renderHandler = null;
	}

}
