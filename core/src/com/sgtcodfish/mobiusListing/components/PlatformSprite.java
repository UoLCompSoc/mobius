package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlatformSprite implements Component {
	public int							size		= 0;
	public Texture						texture		= null;
	public PlatformSpriteOrientation	orientation	= PlatformSpriteOrientation.NONE;
	/** Do not trust the x,y coordinates of this rectangle. */
	public Rectangle					rectangle	= null;

	@Override
	public void reset() {
		size = 0;
		orientation = PlatformSpriteOrientation.NONE;
		rectangle = null;

		if (texture != null) {
			texture.dispose();
			texture = null;
		}
	}

	public enum PlatformSpriteOrientation {
		NONE, HORIZONTAL, VERTICAL;
	}
}
