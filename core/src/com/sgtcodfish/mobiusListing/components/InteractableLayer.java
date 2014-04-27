package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

/**
 * <p>
 * Holds a layer which can be interacted with in some way; moved left, right, up
 * or down, or made invisible/opaque.
 * </p>
 * 
 * <p>
 * To actually implement the interaction, override this class and implement
 * interact.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public abstract class InteractableLayer<T extends Component> implements Component {
	public TiledMapTileLayer	layer	= null;

	/**
	 * <p>
	 * Indicates that the layer be modified in some way.
	 * </p>
	 * <p>
	 * A degree of 1 means 1 step's worth of interaction.
	 * </p>
	 * 
	 * @param c
	 *        A component, probably linked to this one by the Entity containing
	 *        the both, that the interaction may change.
	 * @param degree
	 *        The amount of the interaction to carry out.
	 */
	public abstract void interact(T c, int degree);

	public abstract void fromTiledProperties(MapProperties properties);

	@Override
	public void reset() {
		layer = null;
	}
}
