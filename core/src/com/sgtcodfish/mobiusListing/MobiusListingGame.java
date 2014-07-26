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
import com.sgtcodfish.mobiusListing.components.Inventory;
import com.sgtcodfish.mobiusListing.components.Linked;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.levels.LevelEntityFactory;
import com.sgtcodfish.mobiusListing.player.PlayerConstants;
import com.sgtcodfish.mobiusListing.player.PlayerEntityFactory;
import com.sgtcodfish.mobiusListing.systems.CollisionBoxRenderingDebugSystem;
import com.sgtcodfish.mobiusListing.systems.FocusTakerSystem;
import com.sgtcodfish.mobiusListing.systems.LevelAdvanceSystem;
import com.sgtcodfish.mobiusListing.systems.LinkingSystem;
import com.sgtcodfish.mobiusListing.systems.MovementSystem;
import com.sgtcodfish.mobiusListing.systems.PlatformInputSystem;
import com.sgtcodfish.mobiusListing.systems.PlayerInputSystem;
import com.sgtcodfish.mobiusListing.systems.SolidProcessingSystem;
import com.sgtcodfish.mobiusListing.systems.SpriteRenderingSystem;
import com.sgtcodfish.mobiusListing.systems.TerrainCollisionSystem;
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

	public static boolean			DEBUG					= false;

	private SpriteBatch				batch					= null;
	private Camera					camera					= null;

	public World					world					= null;

	private MovementSystem			movementSystem			= null;
	private TerrainCollisionSystem	terrainCollisionSystem	= null;
	private LinkingSystem			linkingSystem			= null;

	public PlayerEntityFactory		playerEntityFactory		= null;
	private Entity					playerEntity			= null;

	public LevelEntityFactory		levelEntityFactory		= null;

	private final float				DEBUG_COOLDOWN			= 1.0f;
	private float					timeSinceLastDebug		= DEBUG_COOLDOWN;

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
			LevelEntityFactory.VERBOSE_LOAD = false;
		}

		world = new World();

		batch = new SpriteBatch();
		camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		movementSystem = new MovementSystem(0.0f);
		terrainCollisionSystem = new TerrainCollisionSystem(null);
		linkingSystem = new LinkingSystem();

		world.setSystem(new PlayerInputSystem(this));
		world.setSystem(new PlatformInputSystem(camera));

		world.setSystem(new SolidProcessingSystem(linkingSystem));
		world.setSystem(terrainCollisionSystem);

		world.setSystem(movementSystem);

		world.setSystem(linkingSystem);

		world.setSystem(new FocusTakerSystem(camera));
		world.setSystem(new TiledRenderingSystem(batch, camera));
		world.setSystem(new SpriteRenderingSystem(batch, camera));

		if (DEBUG) {
			world.setSystem(new CollisionBoxRenderingDebugSystem(camera));
		}

		world.setSystem(new LevelAdvanceSystem(this), true);

		world.setManager(new GroupManager());

		world.initialize();

		playerEntityFactory = new PlayerEntityFactory(world);
		playerEntity = playerEntityFactory.createEntity(5.0f, 5.0f, true);

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

		// TODO: Fix dirty hack with global state in this line.
		PlayerConstants.interacting = Gdx.input.isKeyPressed(Keys.W);

		world.process();
		timeSinceLastDebug += deltaTime;
		if (timeSinceLastDebug > DEBUG_COOLDOWN) {
			if (Gdx.input.isKeyPressed(Keys.F10)) {
				nextLevel();
				timeSinceLastDebug = 0.0f;
			}
		}

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			timeSinceLastDebug += deltaTime;

			if (timeSinceLastDebug > DEBUG_COOLDOWN) {
				if (Gdx.input.isKeyPressed(Keys.F8)) {
					timeSinceLastDebug = 0.0f;
					Position p = playerEntity.getComponent(Position.class);
					Gdx.app.debug("PLAYER_POS", "Player is located at (" + p.position.x + ", " + p.position.y
							+ "), int=(" + (int) p.position.x + ", " + (int) p.position.y + ").");
				} else if (Gdx.input.isKeyPressed(Keys.F9)) {
					timeSinceLastDebug = 0.0f;
					debugEntities();
				} else if (Gdx.input.isKeyPressed(Keys.F11)) {
					String inventString = "";

					for (Item i : playerEntity.getComponent(Inventory.class).inventoryList) {
						inventString += i.name + "\n";
					}

					Gdx.app.debug("PLAYER_INVENTORY", "Player inventory contains:\n" + inventString);
					timeSinceLastDebug = 0.0f;
				}
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		camera = new OrthographicCamera(width, height);
	}

	public void nextLevel() {
		if (levelEntityFactory.loadNextLevel() == false) {
			Gdx.app.exit();
		}

		terrainCollisionSystem.setCollisionMap(levelEntityFactory.getCurrentLevelCollisionMap());
		movementSystem.doNextLevel(levelEntityFactory.getCurrentLevelCollisionMap().actualWidthInWorld());
		resetPlayer();
	}

	public void resetPlayer() {
		playerEntity.getComponent(Position.class).position.set(levelEntityFactory.getCurrentLevelSpawn());
		playerEntity.getComponent(Inventory.class).inventoryList.clear();
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
				components += c.getClass().getSimpleName();

				if (c.getClass() == Linked.class) {
					components += "->" + e.getComponent(Linked.class).child.id;
				}

				components += delim;
			}

			components = components.substring(0, components.length() - delim.length());

			Gdx.app.debug("DEBUG_ENTITIES", components);
		}
	}
}
