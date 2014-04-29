package com.sgtcodfish.mobiusListing.components;

import java.util.ArrayList;

import com.artemis.Component;
import com.sgtcodfish.mobiusListing.Item;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class Inventory implements Component {
	public ArrayList<Item>	inventoryList	= new ArrayList<Item>();

	@Override
	public void reset() {
		inventoryList = new ArrayList<Item>();
	}
}
