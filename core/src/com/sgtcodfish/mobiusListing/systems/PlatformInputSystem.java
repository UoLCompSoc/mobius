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
import com.sgtcodfish.mobiusListing.components.ChildLinked;
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

	private ComponentMapper<ChildLinked>	childLinkedMapper		= null;

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
		childLinkedMapper = world.getMapper(ChildLinked.class);
	}

	@Override
	protected void process(Entity e) {
		Position p = positionMapper.get(e);
		PlatformSprite platformSprite = platformSpriteMapper.get(e);

		boolean isChild = childLinkedMapper.get(e) != null;

		timeSinceLastClick += world.getDelta();

		if (timeSinceLastClick > CLICK_WAIT_TIME) {
			platformSprite.rectangle.x = p.position.x;
			platformSprite.rectangle.y = p.position.y;

			if (isChild) {
				platformSprite.rectangle.y -= platformSprite.spriteHeight;
			}

			if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
				clickFlag = true;

				mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0.0f);
				mouse = camera.unproject(mouse, 0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

				if (platformSprite.rectangle.contains((float) mouse.x, (float) mouse.y)) {
					// if this entity is a child, move its parent.
					Entity actualEntity = (isChild ? childLinkedMapper.get(e).parentEntity : e);

					DxLayer dxLayer = dxLayerMapper.get(actualEntity);
					MovingLayer movingLayer = (dxLayer != null ? dxLayer : dyLayerMapper.get(actualEntity));
					FadableLayer fadableLayer = fadableLayerMapper.get(actualEntity);

					if (movingLayer != null) {
						movingLayer.interact(positionMapper.get(actualEntity), 1);
					}

					if (fadableLayer != null) {
						fadableLayer.interact(opacityMapper.get(actualEntity), 1);
					}
				}
			}

			if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
				if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
					clickFlag = true;

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
