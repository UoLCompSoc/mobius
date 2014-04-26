package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.TiledRenderable;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TiledRenderingSystem extends EntityProcessingSystem {
	// private ComponentMapper<Position> positionMapper = null;
	private ComponentMapper<TiledRenderable>	tiledRenderableMapper	= null;

	private Batch								batch					= null;
	private Camera								camera					= null;

	@SuppressWarnings("unchecked")
	public TiledRenderingSystem(Batch batch, Camera camera) {
		this(Filter.allComponents(Position.class, TiledRenderable.class), batch, camera);
	}

	protected TiledRenderingSystem(Filter filter, Batch batch, Camera camera) {
		super(filter);
		this.batch = batch;
		this.camera = camera;
	}

	@Override
	public void initialize() {
		// positionMapper = world.getMapper(Position.class);
		tiledRenderableMapper = world.getMapper(TiledRenderable.class);
	}

	@Override
	protected void process(Entity e) {
		// Position p = positionMapper.get(e);
		TiledRenderable tr = tiledRenderableMapper.get(e);
		tr.renderer.setView((OrthographicCamera) camera);
		tr.renderer.render();
	}

	@Override
	protected void begin() {
		batch.setProjectionMatrix(camera.combined);
	}
}
