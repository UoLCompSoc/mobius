package com.sgtcodfish.mobiusListing.factories;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * <p>
 * Creates {@link Entity}s and handles related resources.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerEntityFactory {
	/**
	 * <p>
	 * Holds the different animation states that the player can be in and
	 * contains helpers for generating related data structures.
	 * </p>
	 * 
	 * @author Ashley Davis (SgtCoDFish)
	 */
	public enum PlayerAnimationState {
		STANDING, RUNNING, JUMPING, USING, MANIPULATING;

		public static HashMap<PlayerAnimationState, Animation> makeAnimationMapFromAnimations(Animation standing,
				Animation running, Animation jumping, Animation using, Animation manipulating) {
			final int numStates = PlayerAnimationState.values().length;
			HashMap<PlayerAnimationState, Animation> animationMap = new HashMap<>(numStates);

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

	public PlayerEntityFactory() {
		this(loadDefaultAnimationMap());
	}

	public PlayerEntityFactory(HashMap<PlayerAnimationState, Animation> animationMap) {

	}

	private static HashMap<PlayerAnimationState, Animation> loadDefaultAnimationMap() {
		final float DEFAULT_FRAME_DURATION = 0.1f;
		final float STANDING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float RUNNING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float JUMPING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float USING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float MANIPULATING_FRAME_DURATION = DEFAULT_FRAME_DURATION;

		Animation standing = null, running = null, jumping = null, using = null, manipulating = null;

		Texture texture = new Texture("player/sprites.png");
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		TextureRegion[][] regions = TextureRegion.split(texture, 32, 32);
		standing = new Animation(STANDING_FRAME_DURATION, regions[0]);

		return PlayerAnimationState.makeAnimationMapFromAnimations(standing, running, jumping, using, manipulating);
	}

}
