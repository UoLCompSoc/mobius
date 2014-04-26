package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
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

		positionMapper = new ComponentMapper<Position>(Position.class, world);
		velocityMapper = new ComponentMapper<Velocity>(Velocity.class, world);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		Velocity v = velocityMapper.get(e);

		p.position.add(v.velocity);
	}
}
