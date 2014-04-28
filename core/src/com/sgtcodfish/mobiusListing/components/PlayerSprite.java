package com.sgtcodfish.mobiusListing.components;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sgtcodfish.mobiusListing.player.HumanoidAnimationState;

/**
 * Indicates that the Entity is renderable in some way.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerSprite extends MobiusSprite {
	public HashMap<HumanoidAnimationState, Animation>	animationMap	= null;
	private HumanoidAnimationState						lastState		= HumanoidAnimationState.STANDING;
	private float										stateTime		= 0.0f;

	public PlayerSprite() {
	}

	public TextureRegion getFrame(HumanoidAnimationState state, float deltaTime) {
		if (lastState != state) {
			lastState = state;
			stateTime = 0.0f;
		}

		stateTime += deltaTime;
		return animationMap.get(state).getKeyFrame(stateTime);
	}

	@Override
	public void reset() {
		super.reset();

		if (animationMap != null) {
			animationMap.clear();
			animationMap = null;
		}

		lastState = HumanoidAnimationState.STANDING;

		stateTime = 0.0f;
	}
}
