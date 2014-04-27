package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.sgtcodfish.mobiusListing.components.Opacity;
import com.sgtcodfish.mobiusListing.components.PlatformSprite;
import com.sgtcodfish.mobiusListing.components.Position;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlatformRenderingSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper			= null;
	private ComponentMapper<PlatformSprite>	platformSpriteMapper	= null;
	private ComponentMapper<Opacity>		opacityMapper			= null;

	private Batch							batch					= null;
	private Camera							camera					= null;

	@SuppressWarnings("unchecked")
	public PlatformRenderingSystem(Batch batch, Camera camera) {
		this(Filter.allComponents(PlatformSprite.class), batch, camera);
	}

	protected PlatformRenderingSystem(Filter filter, Batch batch, Camera camera) {
		super(filter);

		this.batch = batch;
		this.camera = camera;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		platformSpriteMapper = world.getMapper(PlatformSprite.class);
		opacityMapper = world.getMapper(Opacity.class);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		PlatformSprite ps = platformSpriteMapper.get(e);
		Opacity opacity = opacityMapper.get(e);

		if (opacity != null) {
			batch.setColor(1.0f, 1.0f, 1.0f, opacity.opacity);
		}

		batch.begin();
		batch.draw(ps.texture, p.position.x, p.position.y, ps.rectangle.width, ps.rectangle.height);
		batch.end();

		if (opacity != null) {
			batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	@Override
	protected void begin() {
		batch.setProjectionMatrix(camera.combined);
	}
}
