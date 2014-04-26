package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
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
	private ComponentMapper<PlayerInputListener>	inputListenerMapper		= null;
	private ComponentMapper<Position>				positionMapper			= null;
	private ComponentMapper<PlayerState>			animationStateMapper	= null;
	private ComponentMapper<Velocity>				velocityMapper			= null;

	@SuppressWarnings("unchecked")
	public PlayerInputSystem() {
		this(Filter.allComponents(PlayerInputListener.class, Position.class, PlayerState.class, Velocity.class));
	}

	protected PlayerInputSystem(Filter filter) {
		super(filter);
	}

	@Override
	public void initialize() {
		inputListenerMapper = world.getMapper(PlayerInputListener.class);
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);
		animationStateMapper = world.getMapper(PlayerState.class);
	}

	@Override
	protected void process(Entity e) {
		// TODO: More portable implementation

		PlayerState ps = animationStateMapper.get(e);

		if (Gdx.input.isKeyPressed(Keys.A)) {
			velocityMapper.get(e).velocity.x = -PlayerConstants.RUN_VELOCITY;
			ps.state = HumanoidAnimationState.RUNNING;
		} else if (Gdx.input.isKeyPressed(Keys.D)) {
			velocityMapper.get(e).velocity.x = +PlayerConstants.RUN_VELOCITY;
			ps.state = HumanoidAnimationState.RUNNING;
		}

		if (Gdx.input.isKeyPressed(Keys.SPACE)) {
			velocityMapper.get(e).velocity.y = PlayerConstants.JUMP_VELOCITY;
			ps.state = HumanoidAnimationState.JUMPING;
		}
	}

}
