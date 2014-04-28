package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.sgtcodfish.mobiusListing.components.Opacity;
import com.sgtcodfish.mobiusListing.components.PlatformSprite;
import com.sgtcodfish.mobiusListing.components.PlayerSprite;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.StaticSprite;

/**
 * Handles drawing Entities with a Position and Renderable component
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class SpriteRenderingSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper			= null;

	private ComponentMapper<PlayerSprite>	playerSpriteMapper		= null;
	private ComponentMapper<PlayerState>	playerStateMapper		= null;

	private ComponentMapper<StaticSprite>	staticSpriteMapper		= null;

	private ComponentMapper<PlatformSprite>	platformSpriteMapper	= null;
	private ComponentMapper<Opacity>		opacityMapper			= null;

	private Batch							batch					= null;
	private Camera							camera					= null;

	@SuppressWarnings("unchecked")
	public SpriteRenderingSystem(Batch batch, Camera camera) {
		this(Filter.allComponents(Position.class).any(PlayerSprite.class, StaticSprite.class, PlatformSprite.class),
				batch, camera);
	}

	protected SpriteRenderingSystem(Filter filter, Batch batch, Camera camera) {
		super(filter);

		this.batch = batch;
		this.camera = camera;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);

		playerSpriteMapper = world.getMapper(PlayerSprite.class);
		playerStateMapper = world.getMapper(PlayerState.class);

		staticSpriteMapper = world.getMapper(StaticSprite.class);

		platformSpriteMapper = world.getMapper(PlatformSprite.class);
		opacityMapper = world.getMapper(Opacity.class);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		PlayerSprite playerSprite = playerSpriteMapper.get(e);
		StaticSprite staticSprite = staticSpriteMapper.get(e);
		PlatformSprite platformSprite = platformSpriteMapper.get(e);

		batch.begin();

		if (playerSprite != null) {
			batch.draw(playerSprite.getFrame(playerStateMapper.get(e).state, world.getDelta()), p.position.x,
					p.position.y);
		} else if (staticSprite != null) {
			batch.draw(staticSprite.textureRegion, p.position.x, p.position.y);
		} else if (platformSprite != null) {
			Opacity opacity = opacityMapper.get(e);

			if (opacity != null) {
				batch.setColor(1.0f, 1.0f, 1.0f, opacity.opacity);
			}

			batch.draw(platformSprite.texture, p.position.x, p.position.y, platformSprite.rectangle.width,
					platformSprite.rectangle.height);

			if (opacity != null) {
				batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			}
		}

		batch.end();
	}

	@Override
	protected void begin() {
		batch.setProjectionMatrix(camera.combined);
	}
}
