package com.sgtcodfish.mobiusListing.components;

import com.badlogic.gdx.maps.MapProperties;

/**
 * <p>
 * Holds a platform that can be moved up/down. Positive y -> up.
 * </p>
 * <p>
 * <strong>WARNING: </strong> When loading, dy being positive correlates to
 * downwards movement. After loading, positive y movement is up.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class DyLayer extends MovingLayer {
	@Override
	protected void move(Position p, int degree) {
		p.position.y -= degree * layer.getTileHeight();
	}

	@Override
	public void fromTiledProperties(MapProperties properties) {
		tiledPropertiesHelper(properties, "dy", true);
	}

}
