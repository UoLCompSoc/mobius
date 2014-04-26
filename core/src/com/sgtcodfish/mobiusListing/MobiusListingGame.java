package com.sgtcodfish.mobiusListing;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sgtcodfish.mobiusListing.factories.PlayerEntityFactory;
import com.sgtcodfish.mobiusListing.systems.MovementSystem;
import com.sgtcodfish.mobiusListing.systems.SpriteRenderingSystem;

/**
 * Contains all the main logic; most should be in classes that extend
 * EntityProcessingSystem.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusListingGame extends ApplicationAdapter {
	private SpriteBatch			batch				= null;
	private Camera				camera				= null;

	public World				world				= null;

	public PlayerEntityFactory	playerEntityFactory	= null;
	private Entity				playerEntity		= null;

	@Override
	public void create() {
		world = new World();

		batch = new SpriteBatch();
		// camera = new PerspectiveCamera(60.0f, 320.0f, 240.0f);
		camera = new OrthographicCamera(320.0f, 240.0f);

		world.setSystem(new MovementSystem());
		world.setSystem(new SpriteRenderingSystem(batch, camera));

		world.initialize();

		playerEntityFactory = new PlayerEntityFactory(world);
		playerEntity = playerEntityFactory.createEntity(5.0f, 5.0f);
		world.addEntity(playerEntity);
	}

	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());

		camera.update();

		Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		world.process();
	}
}
