package com.sgtcodfish.mobiusListing.components;

import com.badlogic.gdx.maps.MapProperties;

/**
 * <p>
 * Holds a platform that can be moved left/right. Positive x -> right.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class DxLayer extends MovingLayer {
	@Override
	protected void move(Position p, int degree) {
		p.position.x += degree * layer.getTileWidth();
	}

	@Override
	public void fromTiledProperties(MapProperties properties) {
		tiledPropertiesHelper(properties, "dx", false);
	}

}
