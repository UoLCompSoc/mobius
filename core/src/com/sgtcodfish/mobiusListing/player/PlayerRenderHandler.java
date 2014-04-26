package com.sgtcodfish.mobiusListing.player;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sgtcodfish.mobiusListing.SpriteRenderHandler;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerRenderHandler implements SpriteRenderHandler {
	private PlayerAnimationState						state			= PlayerAnimationState.STANDING;
	private HashMap<PlayerAnimationState, Animation>	animationMap	= null;

	private float										stateTime		= 0.0f;

	public PlayerRenderHandler(HashMap<PlayerAnimationState, Animation> animationMap) {
		this.animationMap = animationMap;
		this.state = PlayerAnimationState.STANDING;
	}

	public void setState(PlayerAnimationState state) {
		this.state = state;
	}

	@Override
	public TextureRegion getFrame(float deltaTime) {
		stateTime += deltaTime;
		return animationMap.get(state).getKeyFrame(stateTime);
	}

}
