package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusSprite implements Component {
	public boolean	mirrored		= false;

	public float	spriteWidth		= 0.0f;
	public float	spriteHeight	= 0.0f;

	@Override
	public void reset() {
		spriteWidth = 0.0f;
		spriteHeight = 0.0f;
		mirrored = false;
	}

}
