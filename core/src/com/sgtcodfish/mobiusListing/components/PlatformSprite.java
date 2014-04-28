package com.sgtcodfish.mobiusListing.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlatformSprite extends MobiusSprite {
	public int							size			= 0;
	public TextureRegion				textureRegion	= null;
	private Texture						texture			= null;
	public PlatformSpriteOrientation	orientation		= PlatformSpriteOrientation.NONE;
	/** Do not trust the x,y coordinates of this rectangle. */
	public Rectangle					rectangle		= null;

	@Override
	public void reset() {
		super.reset();

		textureRegion = null;
		size = 0;
		orientation = PlatformSpriteOrientation.NONE;
		rectangle = null;

		if (texture != null) {
			texture.dispose();
			texture = null;
		}
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
		this.textureRegion = new TextureRegion(texture);
	}

	public enum PlatformSpriteOrientation {
		NONE, HORIZONTAL, VERTICAL;
	}
}
