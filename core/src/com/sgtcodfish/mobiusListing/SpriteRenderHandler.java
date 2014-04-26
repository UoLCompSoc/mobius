package com.sgtcodfish.mobiusListing;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * <p>
 * Interface used by Renderable to handle getting the frame of the current thing
 * to be drawn. Wrapped to enable different rendering styles.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public interface SpriteRenderHandler {
	public TextureRegion getFrame(float deltaTime);
}
