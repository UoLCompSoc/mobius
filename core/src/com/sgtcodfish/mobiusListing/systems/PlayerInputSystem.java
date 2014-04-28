package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.sgtcodfish.mobiusListing.components.PlayerInputListener;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.player.HumanoidAnimationState;
import com.sgtcodfish.mobiusListing.player.PlayerConstants;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerInputSystem extends EntityProcessingSystem {
	private ComponentMapper<PlayerState>	animationStateMapper	= null;
	private ComponentMapper<Velocity>		velocityMapper			= null;

	@SuppressWarnings("unchecked")
	public PlayerInputSystem() {
		this(Filter.allComponents(PlayerInputListener.class, Position.class, PlayerState.class, Velocity.class));
	}

	protected PlayerInputSystem(Filter filter) {
		super(filter);
	}

	@Override
	public void initialize() {
		velocityMapper = world.getMapper(Velocity.class);
		animationStateMapper = world.getMapper(PlayerState.class);
	}

	@Override
	protected void process(Entity e) {
		// TODO: More portable implementation

		PlayerState ps = animationStateMapper.get(e);

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG && Gdx.input.isKeyPressed(Keys.S)) {
			velocityMapper.get(e).velocity.x = 0;
			ps.state = HumanoidAnimationState.STANDING;
		} else if (Gdx.app.getLogLevel() == Application.LOG_DEBUG && Gdx.input.isKeyPressed(Keys.A)) {
			velocityMapper.get(e).velocity.x = -PlayerConstants.RUN_VELOCITY;
			if (ps.state != HumanoidAnimationState.JUMPING)
				ps.state = HumanoidAnimationState.RUNNING;
		}

		if (Gdx.input.isKeyPressed(Keys.D)) {
			velocityMapper.get(e).velocity.x = +PlayerConstants.RUN_VELOCITY;
			if (ps.state != HumanoidAnimationState.JUMPING)
				ps.state = HumanoidAnimationState.RUNNING;
		}

		if (Gdx.input.isKeyPressed(Keys.SPACE) && ps.state != HumanoidAnimationState.JUMPING) {
			velocityMapper.get(e).velocity.y = PlayerConstants.JUMP_VELOCITY;
			ps.state = HumanoidAnimationState.JUMPING;
		}
	}

}
