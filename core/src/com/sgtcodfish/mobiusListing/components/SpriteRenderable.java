package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.sgtcodfish.mobiusListing.SpriteRenderHandler;

/**
 * Indicates that the Entity is renderable in some way.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class SpriteRenderable implements Component {
	public SpriteRenderHandler	renderHandler	= null;

	public SpriteRenderable() {
	}

	@Override
	public void reset() {
		renderHandler = null;
	}

}
