package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TiledRenderable implements Component {
	public TiledMap						map					= null;
	public OrthogonalTiledMapRenderer	renderer			= null;
	public String[]						renderableLayers	= null;
	public int[]						layerArray			= null;

	public TiledRenderable() {
		reset();
	}

	public void initLayerArray() {
		layerArray = new int[renderableLayers.length];
		int found = 0;

		for (int i = 0; i < map.getLayers().getCount(); i++) {
			String name = map.getLayers().get(i).getName();

			for (String s : renderableLayers) {
				if (name.equals(s)) {
					layerArray[found] = i;
					found++;
					if (found == renderableLayers.length) {
						return;
					}
				}
			}
		}
	}

	@Override
	public void reset() {
		layerArray = null;
		renderableLayers = null;
		map = null;
		renderer = null;
	}
}
