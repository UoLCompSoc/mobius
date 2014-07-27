package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.math.Vector2;
import com.sgtcodfish.mobiusListing.TerrainCollisionMap;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.player.HumanoidAnimationState;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TerrainCollisionSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper	= null;
	private ComponentMapper<Velocity>		velocityMapper	= null;
	private ComponentMapper<Solid>			solidMapper		= null;

	private ComponentMapper<PlayerState>	stateMapper		= null;

	private TerrainCollisionMap				collisionMap	= null;
	private Vector2							collisionVector	= null;

	@SuppressWarnings("unchecked")
	public TerrainCollisionSystem(TerrainCollisionMap collisionMap) {
		this(Filter.allComponents(Position.class, Velocity.class, Solid.class), collisionMap);
	}

	protected TerrainCollisionSystem(Filter filter, TerrainCollisionMap collisionMap) {
		super(filter);
		this.collisionMap = collisionMap;
	}

	public void setCollisionMap(TerrainCollisionMap collisionMap) {
		this.collisionMap = collisionMap;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);
		solidMapper = world.getMapper(Solid.class);
		stateMapper = world.getMapper(PlayerState.class);

		collisionVector = new Vector2();
	}

	@Override
	protected void process(Entity e) {
		Position position = positionMapper.get(e);
		Velocity v = velocityMapper.get(e);
		Solid s = solidMapper.get(e);

		PlayerState ps = stateMapper.get(e);

		float playerOffset = 0.0f; // TODO: Fix dirty hack for player collision
									// box.

		if (ps != null) {
			playerOffset = 16.0f;
		}

		collisionVector.set(position.position);

		if (v.velocity.x > 0.0f) {
			collisionVector.x += s.boundingBox.width - playerOffset;
		} else {
			collisionVector.x += playerOffset;
		}

		if (v.velocity.y > 0.0f) {
			collisionVector.y += s.boundingBox.height;
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
