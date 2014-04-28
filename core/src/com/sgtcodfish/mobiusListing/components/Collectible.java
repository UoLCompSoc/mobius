package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;

/**
 * <p>
 * Indicates that an entity can be collected by another and added to an
 * inventory.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Collectible implements Component {
	private static final String	DEFAULT_NAME	= "CollectibleEntity";
	public String				name			= DEFAULT_NAME;

	@Override
	public void reset() {
		name = DEFAULT_NAME;
	}

}
