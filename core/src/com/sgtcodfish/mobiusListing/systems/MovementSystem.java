package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.player.HumanoidAnimationState;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class MovementSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper	= null;
	private ComponentMapper<Velocity>		velocityMapper	= null;

	private ComponentMapper<PlayerState>	stateMapper		= null;

	@SuppressWarnings("unchecked")
	public MovementSystem() {
		this(Filter.allComponents(Position.class, Velocity.class));
	}

	protected MovementSystem(Filter filter) {
		super(filter);
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);
		stateMapper = world.getMapper(PlayerState.class);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		Velocity v = velocityMapper.get(e);
		PlayerState ps = stateMapper.get(e);

		v.velocity.x *= 0.75f; // friction

		v.velocity.x = (Math.abs(v.velocity.x) < 0.1f ? 0.0f : v.velocity.x);
		v.velocity.y = (Math.abs(v.velocity.y) < 0.1f ? 0.0f : v.velocity.y);

		p.position.add(v.velocity);

		if (p.position.x < 0.0f) {
			p.position.x = 0.0f;
			v.velocity.x = 0.0f;
		}

		if (p.position.y < 0.0f) {
			p.position.y = 0.0f;
			v.velocity.y = 0.0f;
		}

		if (ps != null) {
			if (v.velocity.x == 0.0f && (v.velocity.y == 0.0f && ps.state != HumanoidAnimationState.JUMPING)) {
				ps.state = HumanoidAnimationState.STANDING;
			}
		}
	}
}
