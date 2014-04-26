package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Indicates that an Entity can move.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class Velocity implements Component {
	public Vector2	velocity	= null;

	public Velocity() {
		reset();
	}

	@Override
	public void reset() {
		if (velocity == null) {
			velocity = new Vector2();
		}

		velocity.x = 0.0f;
		velocity.y = 0.0f;
	}

}
