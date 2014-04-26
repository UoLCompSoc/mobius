package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.sgtcodfish.mobiusListing.player.HumanoidAnimationState;

/**
 * Indicates that the current {@link Entity} has a player state attached.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerState implements Component {
	public HumanoidAnimationState	state	= HumanoidAnimationState.STANDING;

	public PlayerState() {
		reset();
	}

	@Override
	public void reset() {
		this.state = HumanoidAnimationState.STANDING;
	}
}
