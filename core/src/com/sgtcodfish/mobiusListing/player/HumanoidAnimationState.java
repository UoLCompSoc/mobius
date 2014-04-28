package com.sgtcodfish.mobiusListing.player;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Animation;

/**
 * <p>
 * Holds the different animation states that the player can be in and contains
 * helpers for generating related data structures.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public enum HumanoidAnimationState {
	STANDING, RUNNING, JUMPING, USING, MANIPULATING;

	public static HashMap<HumanoidAnimationState, Animation> makeAnimationMapFromAnimations(Animation standing,
			Animation running, Animation jumping, Animation using, Animation manipulating) {
		final int numStates = HumanoidAnimationState.values().length;
		HashMap<HumanoidAnimationState, Animation> animationMap = new HashMap<>(numStates);

		if (standing == null || running == null || jumping == null || using == null || manipulating == null) {
			throw new IllegalArgumentException("Null animation passed to makeAnimationMap");
		}

		animationMap.put(STANDING, standing);
		animationMap.put(RUNNING, running);
		animationMap.put(JUMPING, jumping);
		animationMap.put(USING, using);
		animationMap.put(MANIPULATING, manipulating);

		return animationMap;
	}
}