package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.sgtcodfish.mobiusListing.Item;

/**
 * <p>
 * Indicates that an entity can be collected by another and added to an
 * inventory.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Collectable implements Component {
	public Item	item	= null;

	@Override
	public void reset() {
		item = null;
	}
}
