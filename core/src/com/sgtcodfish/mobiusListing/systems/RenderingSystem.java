package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.sgtcodfish.mobiusListing.components.Drawable;
import com.sgtcodfish.mobiusListing.components.Position;

/**
 * Handles drawing Entities with a Position and Drawable component
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class RenderingSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>	positionMapper	= null;
	private ComponentMapper<Drawable>	drawableMapper	= null;

	private Batch						batch			= null;
	private Camera						camera			= null;

	@SuppressWarnings("unchecked")
	public RenderingSystem(Batch batch, Camera camera) {
		this(Filter.allComponents(Position.class, Drawable.class), batch, camera);
	}

	protected RenderingSystem(Filter filter, Batch batch, Camera camera) {
		super(filter);

		this.batch = batch;
		this.camera = camera;

		positionMapper = new ComponentMapper<Position>(Position.class, world);
		drawableMapper = new ComponentMapper<Drawable>(Drawable.class, world);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		Drawable d = drawableMapper.get(e);

		batch.begin();

		batch.draw(d.texture, p.position.x, p.position.y);

		batch.end();
	}

	@Override
	protected void begin() {
		batch.setProjectionMatrix(camera.combined);
	}
}
