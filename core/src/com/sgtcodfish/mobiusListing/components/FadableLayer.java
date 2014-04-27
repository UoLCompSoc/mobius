package com.sgtcodfish.mobiusListing.components;

import com.badlogic.gdx.maps.MapProperties;
import com.sgtcodfish.mobiusListing.WorldConstants;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class FadableLayer extends InteractableLayer<Opacity> {
	protected final float	BECOMING_OPAQUE			= 1.0f;
	protected final float	BECOMING_TRANSPARENT	= -1.0f;

	float					minOpacity				= WorldConstants.GLOBAL_SOLID_OPACITY;
	float					opacityDirection		= BECOMING_TRANSPARENT;

	@Override
	public void interact(Opacity c, int degree) {
	}

	/**
	 * <p>
	 * Sets up a FadableLayer with the given opacity details.
	 * </p>
	 * <p>
	 * <em>Note:</em> this function checks and sets up the opacity based on
	 * {@link WorldConstants#GLOBAL_SOLID_OPACITY_THRESHOLD}.
	 * </p>
	 * 
	 * @param c
	 * @param tiledOpacity
	 * @param properties
	 */
	public void setupOpacity(Opacity c, float tiledOpacity, MapProperties properties) {
		if (tiledOpacity < WorldConstants.GLOBAL_SOLID_OPACITY_THRESHOLD) {
			c.setTransparent();
			opacityDirection = BECOMING_OPAQUE;
		} else {
			c.setOpaque();
			opacityDirection = BECOMING_TRANSPARENT;
		}
	}

	@Override
	public void fromTiledProperties(MapProperties properties) {
		String prop = properties.get("minOpacity", null, String.class);

		if (prop != null) {
			float minOpacity = -1.0f;

			try {
				minOpacity = Float.parseFloat(prop);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Invalid value for opacity: " + prop);
			}

			this.minOpacity = minOpacity;

		} else {
			throw new IllegalArgumentException("Trying to load FadableLayer from layer with no minOpacity!");
		}
	}
}
