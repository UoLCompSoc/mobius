package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.sgtcodfish.mobiusListing.components.PlayerSprite;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;

/**
 * Handles drawing Entities with a Position and Renderable component
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class SpriteRenderingSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper		= null;
	private ComponentMapper<PlayerSprite>	renderableMapper	= null;
	private ComponentMapper<PlayerState>	playerStateMapper	= null;

	private Batch							batch				= null;
	private Camera							camera				= null;

	@SuppressWarnings("unchecked")
	public SpriteRenderingSystem(Batch batch, Camera camera) {
		this(Filter.allComponents(Position.class, PlayerState.class, PlayerSprite.class), batch, camera);
	}

	protected SpriteRenderingSystem(Filter filter, Batch batch, Camera camera) {
		super(filter);

		this.batch = batch;
		this.camera = camera;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		renderableMapper = world.getMapper(PlayerSprite.class);
		playerStateMapper = world.getMapper(PlayerState.class);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		PlayerSprite sprite = renderableMapper.get(e);

		batch.begin();

		batch.draw(sprite.getFrame(playerStateMapper.get(e).state, world.getDelta()), p.position.x, p.position.y);

		batch.end();
	}

	@Override
	protected void begin() {
		batch.setProjectionMatrix(camera.combined);
	}
}
