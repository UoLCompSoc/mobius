package com.sgtcodfish.mobiusListing;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sgtcodfish.mobiusListing.levels.LevelEntityFactory;
import com.sgtcodfish.mobiusListing.player.PlayerEntityFactory;
import com.sgtcodfish.mobiusListing.systems.FocusTakerSystem;
import com.sgtcodfish.mobiusListing.systems.MovementSystem;
import com.sgtcodfish.mobiusListing.systems.PlayerInputSystem;
import com.sgtcodfish.mobiusListing.systems.SpriteRenderingSystem;
import com.sgtcodfish.mobiusListing.systems.TiledRenderingSystem;

/**
 * Contains all the main logic; most should be in classes that extend
 * EntityProcessingSystem.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusListingGame extends ApplicationAdapter {
	public static boolean		DEBUG				= false;

	private SpriteBatch			batch				= null;
	private Camera				camera				= null;

	public World				world				= null;

	public PlayerEntityFactory	playerEntityFactory	= null;
	private Entity				playerEntity		= null;

	public LevelEntityFactory	levelEntityFactory	= null;
	private Entity				currentLevelEntity	= null;

	public MobiusListingGame() {
		this(false);
	}

	public MobiusListingGame(boolean debug) {
		MobiusListingGame.DEBUG = debug;
	}

	@Override
	public void create() {
		if (DEBUG) {
			Gdx.app.setLogLevel(Application.LOG_DEBUG);
			Gdx.app.debug("DEBUG", "Debug logging enabled.");
		} else {
			Gdx.app.setLogLevel(Application.LOG_ERROR);
		}

		world = new World();

		batch = new SpriteBatch();
		// camera = new PerspectiveCamera(60.0f, 320.0f, 240.0f);
		camera = new OrthographicCamera(320.0f, 240.0f);

		world.setSystem(new PlayerInputSystem());
		world.setSystem(new MovementSystem());
		world.setSystem(new FocusTakerSystem(camera));
		world.setSystem(new TiledRenderingSystem(batch, camera));
		world.setSystem(new SpriteRenderingSystem(batch, camera));

		world.initialize();

		playerEntityFactory = new PlayerEntityFactory(world);
		playerEntity = playerEntityFactory.createEntity(5.0f, 5.0f, true);
		world.addEntity(playerEntity);

		levelEntityFactory = new LevelEntityFactory(world, batch);
		currentLevelEntity = levelEntityFactory.generateNextLevelEntity();
		world.addEntity(currentLevelEntity);
	}

	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());

		camera.update();

		Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		world.process();
	}

	@Override
	public void dispose() {
		if (playerEntityFactory != null) {
			playerEntityFactory.dispose();
		}

		if (levelEntityFactory != null) {
			levelEntityFactory.dispose();
		}

		if (batch != null) {
			batch.dispose();
		}

		if (camera != null) {
			camera = null;
		}

		playerEntity = null;
		if (world != null) {
			world.dispose();
		}
	}
}
