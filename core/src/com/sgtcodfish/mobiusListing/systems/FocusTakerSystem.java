package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.sgtcodfish.mobiusListing.components.FocusTaker;
import com.sgtcodfish.mobiusListing.components.Position;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class FocusTakerSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>	positionMapper	= null;

	private Camera						camera			= null;

	@SuppressWarnings("unchecked")
	public FocusTakerSystem(Camera camera) {
		this(camera, Filter.allComponents(Position.class, FocusTaker.class));
	}

	protected FocusTakerSystem(Camera camera, Filter filter) {
		super(filter);

		this.camera = camera;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
	}

	@Override
	protected void process(Entity e) {
		Vector2 pos = positionMapper.get(e).position;

		camera.position.x = pos.x;
		camera.position.y = pos.y;
		camera.update();
	}
}
