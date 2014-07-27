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
	public static final float				FRICTION		= 0.75f;	// TODO:
																		// Implement
																		// friction
																		// and
																		// air
																		// resistance
	public static final float				AIR_RESISTANCE	= 0.75f;

	private ComponentMapper<Position>		positionMapper	= null;
	private ComponentMapper<Velocity>		velocityMapper	= null;

	private ComponentMapper<PlayerState>	stateMapper		= null;

	private float							xBound			= 0.0f;

	@SuppressWarnings("unchecked")
	public MovementSystem(float xBound) {
		this(Filter.allComponents(Position.class, Velocity.class), xBound);
	}

	protected MovementSystem(Filter filter, float xBound) {
		super(filter);

		this.xBound = xBound;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);
		stateMapper = world.getMapper(PlayerState.class);
	}

	public void doNextLevel(float xBound) {
		this.xBound = xBound;
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		Velocity v = velocityMapper.get(e);
		PlayerState ps = stateMapper.get(e);

		if (ps != null) {
			if (ps.state != HumanoidAnimationState.JUMPING) {
				v.velocity.x *= FRICTION; // friction, doesn't apply in the air.
			} else {
				v.velocity.x *= AIR_RESISTANCE; // air resistance when jumping
			}
		} else {
			v.velocity.x *= FRICTION; // not player so we'll just apply friction
		}

		// if (v.velocity.y > 0.0f) {
		// v.velocity.y -= WorldConstants.GRAVITY;
		// }

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

		if (p.position.x > xBound) {
			p.position.x = 0.0f;
		}

		if (ps != null) {
			if (v.velocity.x == 0.0f && (v.velocity.y == 0.0f && ps.state != HumanoidAnimationState.JUMPING)) {
				ps.state = HumanoidAnimationState.STANDING;
			}
		}
	}
}
