package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Indicates that an entity has a position in the world.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Position implements Component {
	public Vector2	position	= null;

	public Position() {
		reset();
	}

	@Override
	public void reset() {
		if (position == null) {
			position = new Vector2();
		}

		position.x = 0.0f;
		position.y = 0.0f;
	}

}
