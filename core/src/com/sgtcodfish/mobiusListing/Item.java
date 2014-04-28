package com.sgtcodfish.mobiusListing;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class Item {
	public enum ItemType {
		NONE, KEY;
	}

	private static final String	DEFAULT_NAME	= "UnnamedItem";
	public String				name			= DEFAULT_NAME;
	public ItemType				type			= ItemType.NONE;

	public Item(String name, ItemType type) {
		this.name = name;
		this.type = type;
	}
}
