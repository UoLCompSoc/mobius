package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector2;
import com.sgtcodfish.mobiusListing.CollisionMap;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.player.HumanoidAnimationState;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class CollisionSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper	= null;
	private ComponentMapper<Velocity>		velocityMapper	= null;

	private ComponentMapper<PlayerState>	stateMapper		= null;

	private CollisionMap					collisionMap	= null;
	private Vector2							collisionVector	= null;

	@SuppressWarnings("unchecked")
	public CollisionSystem(CollisionMap collisionMap) {
		this(Filter.allComponents(Position.class, Velocity.class, Solid.class), collisionMap);
	}

	protected CollisionSystem(Filter filter, CollisionMap collisionMap) {
		super(filter);
		this.collisionMap = collisionMap;
	}

	public void setCollisionMap(CollisionMap collisionMap) {
		this.collisionMap = collisionMap;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);
		stateMapper = world.getMapper(PlayerState.class);

		collisionVector = new Vector2();
	}

	@Override
	protected void process(Entity e) {
		Position position = positionMapper.get(e);

		Velocity v = velocityMapper.get(e);

		PlayerState ps = stateMapper.get(e);

		collisionVector.set(position.position);
		if (v.velocity.x > 0.0f) {
			// TODO: fix magic numbers, dirtily increasing by player sprite
			// size.
			collisionVector.x += 32.0f;
		}

		if (v.velocity.y > 0.0f) {
			collisionVector.y += 32.0f;
		}

		v.velocity.y -= WorldConstants.GRAVITY;

		if (collisionMap.willCollideX(collisionVector, v.velocity)) {
			v.velocity.x = 0.0f;
			if (ps != null && ps.state != HumanoidAnimationState.JUMPING) {
				ps.state = HumanoidAnimationState.STANDING;
			}
		}

		if (collisionMap.willCollideY(collisionVector, v.velocity)) {
			v.velocity.y = 0.0f;
			if (ps != null && ps.state == HumanoidAnimationState.JUMPING) {
				ps.state = (v.velocity.x != 0.0f ? HumanoidAnimationState.RUNNING : HumanoidAnimationState.STANDING);
			}
		}
	}

	@Override
	protected boolean checkProcessing() {
		return (collisionMap != null);
	}
}
