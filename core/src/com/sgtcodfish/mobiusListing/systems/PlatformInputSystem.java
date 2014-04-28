package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.sgtcodfish.mobiusListing.components.DxLayer;
import com.sgtcodfish.mobiusListing.components.DyLayer;
import com.sgtcodfish.mobiusListing.components.FadableLayer;
import com.sgtcodfish.mobiusListing.components.MovingLayer;
import com.sgtcodfish.mobiusListing.components.Opacity;
import com.sgtcodfish.mobiusListing.components.PlatformInputListener;
import com.sgtcodfish.mobiusListing.components.PlatformSprite;
import com.sgtcodfish.mobiusListing.components.Position;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlatformInputSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper			= null;

	private ComponentMapper<PlatformSprite>	platformSpriteMapper	= null;

	private ComponentMapper<MovingLayer>	movingLayerMapper		= null;
	private ComponentMapper<FadableLayer>	fadableLayerMapper		= null;

	private ComponentMapper<Opacity>		opacityMapper			= null;

	@SuppressWarnings("unchecked")
	public PlatformInputSystem() {
		this(Filter.allComponents(PlatformInputListener.class, PlatformSprite.class).any(DxLayer.class, DyLayer.class,
				FadableLayer.class));
	}

	protected PlatformInputSystem(Filter filter) {
		super(filter);
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		platformSpriteMapper = world.getMapper(PlatformSprite.class);
		movingLayerMapper = world.getMapper(MovingLayer.class);
		fadableLayerMapper = world.getMapper(FadableLayer.class);
		opacityMapper = world.getMapper(Opacity.class);
	}

	@Override
	protected void process(Entity e) {
		PlatformSprite platformSprite = platformSpriteMapper.get(e);

		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
			int x = Gdx.input.getX();
			int y = Gdx.graphics.getHeight() - Gdx.input.getY();

			if (platformSprite.rectangle.contains((float) x, (float) y)) {
				MovingLayer movingLayer = movingLayerMapper.get(e);
				FadableLayer fadableLayer = fadableLayerMapper.get(e);

				if (movingLayer != null) {
					movingLayer.interact(positionMapper.get(e), 1);
				}

				if (fadableLayer != null) {
					fadableLayer.interact(opacityMapper.get(e), 1);
				}
			}
		}
	}
}
