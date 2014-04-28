package com.sgtcodfish.mobiusListing;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sgtcodfish.mobiusListing.levels.LevelEntityFactory;
import com.sgtcodfish.mobiusListing.player.PlayerEntityFactory;
import com.sgtcodfish.mobiusListing.systems.FocusTakerSystem;
import com.sgtcodfish.mobiusListing.systems.MovementSystem;
import com.sgtcodfish.mobiusListing.systems.PlatformInputSystem;
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
	public enum MobiusState {
		TITLE_SCREEN, PLAYING;
	}

	public static boolean		DEBUG				= false;

	private SpriteBatch			batch				= null;
	private Camera				camera				= null;

	public World				world				= null;

	public PlayerEntityFactory	playerEntityFactory	= null;
	private Entity				playerEntity		= null;

	public LevelEntityFactory	levelEntityFactory	= null;

	private final float			DEBUG_COOLDOWN		= 2.0f;
	private float				timeSinceLastDebug	= DEBUG_COOLDOWN;

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
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		world.setSystem(new PlayerInputSystem());
		world.setSystem(new PlatformInputSystem());
		world.setSystem(new MovementSystem());
		world.setSystem(new FocusTakerSystem(camera));
		world.setSystem(new TiledRenderingSystem(batch, camera));
		world.setSystem(new SpriteRenderingSystem(batch, camera));

		world.setManager(new GroupManager());

		world.initialize();

		playerEntityFactory = new PlayerEntityFactory(world);
		playerEntity = playerEntityFactory.createEntity(5.0f, 5.0f, true);
		world.addEntity(playerEntity);

		levelEntityFactory = new LevelEntityFactory(world, batch);
		nextLevel();
	}

	@Override
	public void render() {
		float deltaTime = Gdx.graphics.getDeltaTime();
		world.setDelta(deltaTime);

		camera.update();

		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		world.process();

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			timeSinceLastDebug += deltaTime;

			if (timeSinceLastDebug > DEBUG_COOLDOWN) {
				if (Gdx.input.isKeyPressed(Keys.F9)) {
					timeSinceLastDebug = 0.0f;
					debugEntities();
				} else if (Gdx.input.isKeyPressed(Keys.F10)) {
					nextLevel();
					timeSinceLastDebug = 0.0f;
				}
			}
		}
	}

	protected void nextLevel() {
		for (Entity e : levelEntityFactory.getNextLevelEntityList()) {
			e.enable();
		}
	}

	@Override
	public void resume() {

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

	public void debugEntities() {
		final String delim = ", ";

		Gdx.app.debug("DEBUG_ENTITIES", "There are " + world.getEntityManager().getActiveEntityCount()
				+ " active entities. Enabled entities:");

		for (Entity e : world.getEntityManager().entities) {
			if (e == null || !e.isEnabled())
				continue;
			Gdx.app.debug("DEBUG_ENTITIES", "----- Entity ID: " + e.id + " -----");

			String components = "";

			for (Component c : e.getComponents()) {
				components += c.getClass().getSimpleName() + delim;
			}

			components = components.substring(0, components.length() - delim.length());

			Gdx.app.debug("DEBUG_ENTITIES", components);
		}
	}
}
