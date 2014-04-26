package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TiledRenderable implements Component {
	public TiledMap						map			= null;
	public OrthogonalTiledMapRenderer	renderer	= null;

	public TiledRenderable() {
	}

	@Override
	public void reset() {
		map = null;
		renderer = null;
	}
}
