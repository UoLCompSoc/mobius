package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Velocity;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class MovementSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>	positionMapper	= null;
	private ComponentMapper<Velocity>	velocityMapper	= null;

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
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		Velocity v = velocityMapper.get(e);

		v.velocity.y -= WorldConstants.GRAVITY;

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
	}
}
