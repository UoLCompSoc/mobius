package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
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

	private ComponentMapper<DxLayer>		dxLayerMapper			= null;
	private ComponentMapper<DyLayer>		dyLayerMapper			= null;
	private ComponentMapper<FadableLayer>	fadableLayerMapper		= null;

	private ComponentMapper<Opacity>		opacityMapper			= null;

	private static final float				CLICK_WAIT_TIME			= 0.25f;
	private float							timeSinceLastClick		= CLICK_WAIT_TIME;
	private boolean							clickFlag				= false;

	private Camera							camera					= null;
	private Vector3							mouse					= null;

	@SuppressWarnings("unchecked")
	public PlatformInputSystem(Camera camera) {
		this(Filter.allComponents(PlatformInputListener.class, PlatformSprite.class).any(DxLayer.class, DyLayer.class,
				FadableLayer.class), camera);
	}

	protected PlatformInputSystem(Filter filter, Camera camera) {
		super(filter);

		this.camera = camera;
		this.mouse = new Vector3();
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		platformSpriteMapper = world.getMapper(PlatformSprite.class);
		dxLayerMapper = world.getMapper(DxLayer.class);
		dyLayerMapper = world.getMapper(DyLayer.class);
		fadableLayerMapper = world.getMapper(FadableLayer.class);
		opacityMapper = world.getMapper(Opacity.class);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		PlatformSprite platformSprite = platformSpriteMapper.get(e);

		timeSinceLastClick += world.getDelta();

		if (timeSinceLastClick > CLICK_WAIT_TIME) {
			platformSprite.rectangle.x = p.position.x;
			platformSprite.rectangle.y = p.position.y;

			if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
				clickFlag = true;

				mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
				mouse = camera.unproject(mouse, 0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

				Gdx.app.debug("PLATFORM_INPUT", "Left click at (x, y) = (" + mouse.x + ", " + mouse.y + ").");

				if (platformSprite.rectangle.contains((float) mouse.x, (float) mouse.y)) {
					Gdx.app.debug("PLATFORM_INPUT", "A rectangle contained this click!");
					DxLayer dxLayer = dxLayerMapper.get(e);
					MovingLayer movingLayer = (dxLayer != null ? dxLayer : dyLayerMapper.get(e));
					FadableLayer fadableLayer = fadableLayerMapper.get(e);

					if (movingLayer != null) {
						Gdx.app.debug("PLATFORM_INPUT", "Handling input on a moving platform.");
						movingLayer.interact(positionMapper.get(e), 1);
					}

					if (fadableLayer != null) {
						Gdx.app.debug("PLATFORM_INPUT", "Handling input on a fading platform.");
						fadableLayer.interact(opacityMapper.get(e), 1);
					}
				}
			}

			if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
				clickFlag = true;

				if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
					Gdx.app.debug("PLATFORM_INPUT", "Detected platform rectangle:\nx: " + platformSprite.rectangle.x
							+ "\ny: " + platformSprite.rectangle.y + "\nw: " + platformSprite.rectangle.width + "\nh: "
							+ platformSprite.rectangle.height);
				}
			}
		}
	}

	@Override
	public void end() {
		if (clickFlag) {
			timeSinceLastClick = 0.0f;
			clickFlag = false;
		}
	}
}
