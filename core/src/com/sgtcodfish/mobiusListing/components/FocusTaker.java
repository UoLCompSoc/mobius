package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.badlogic.gdx.graphics.Camera;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class FocusTaker implements Component {
	Camera	camera	= null;

	@Override
	public void reset() {
		camera = null;
	}

}
