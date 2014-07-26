package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.sgtcodfish.mobiusListing.components.ChildLinked;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;

/**
 * Draws the rectangles of collision boxes for debugging purposes.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class CollisionBoxRenderingDebugSystem extends EntityProcessingSystem {
	private ShapeRenderer					renderer		= null;

	private ComponentMapper<Position>		positionMapper	= null;
	private ComponentMapper<Solid>			solidMapper		= null;

	private ComponentMapper<ChildLinked>	linkedMapper	= null;

	private Camera							camera			= null;

	@SuppressWarnings("unchecked")
	public CollisionBoxRenderingDebugSystem(Camera camera) {
		this(camera, Filter.allComponents(Position.class, Solid.class));
	}

	protected CollisionBoxRenderingDebugSystem(Camera camera, Filter filter) {
		super(filter);

		this.camera = camera;
	}

	@Override
	public void initialize() {
		renderer = new ShapeRenderer();
		renderer.setColor(Color.PINK);

		positionMapper = world.getMapper(Position.class);
		solidMapper = world.getMapper(Solid.class);
		linkedMapper = world.getMapper(ChildLinked.class);
	}

	@Override
	protected void begin() {
		renderer.setProjectionMatrix(camera.combined);
		renderer.begin(ShapeType.Line);
		super.begin();
	}

	@Override
	protected void process(Entity e) {
		Vector2 p = positionMapper.get(e).position;
		Solid s = solidMapper.get(e);
		ChildLinked cl = linkedMapper.get(e);

		renderer.rect(p.x, p.y - (cl == null ? 0 : s.boundingBox.height), s.boundingBox.width, s.boundingBox.height);
	}

	@Override
	protected void end() {
		renderer.end();
		super.end();
	}
}
