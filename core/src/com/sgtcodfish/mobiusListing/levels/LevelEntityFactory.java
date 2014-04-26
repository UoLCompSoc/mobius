package com.sgtcodfish.mobiusListing.levels;

import java.util.ArrayList;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.TiledRenderable;

/**
 * <p>
 * Creates Level {@link Entity}s and handles related resources.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class LevelEntityFactory implements Disposable {
	public static String		DEFAULT_LEVEL_FOLDER	= "levels/";

	public ArrayList<TiledMap>	levels					= null;
	private int					levelNumber				= -1;

	private World				world					= null;

	private Batch				batch					= null;

	public LevelEntityFactory(World world, Batch batch) {
		this(world, batch, DEFAULT_LEVEL_FOLDER);
	}

	public LevelEntityFactory(World world, Batch batch, String folderName) {
		loadLevelsFromFolder(folderName);
		this.world = world;
		this.batch = batch;
	}

	/**
	 * <p>
	 * Generates an {@link Entity} for the next level, and returns it. Returns
	 * null if there are no more com.sgtcodfish.mobiusListing.levels.
	 * </p>
	 * 
	 * @return The next level entity, or null if there are no more.
	 */
	public Entity generateNextLevelEntity() {
		levelNumber++;

		if (levels.size() <= levelNumber) {
			return null;
		} else {
			Entity e = world.createEntity();

			Position p = world.createComponent(Position.class);
			e.addComponent(p);

			TiledRenderable r = world.createComponent(TiledRenderable.class);
			r.map = levels.get(levelNumber);
			r.renderer = new OrthogonalTiledMapRenderer(r.map, 1.0f, batch);
			e.addComponent(r);

			return e;
		}
	}

	/**
	 * Loads all the com.sgtcodfish.mobiusListing.levels in a specified folder.
	 * For internal use; construct a new LevelEntityFactory to load the
	 * com.sgtcodfish.mobiusListing.levels in a new folder.
	 * 
	 * @param levelFolder
	 *        The folder containing the level files.
	 */
	protected void loadLevelsFromFolder(String levelFolder) {
		FileHandle handle = Gdx.files.internal(levelFolder);
		FileHandle[] levelHandles = null;

		if (!handle.isDirectory()) {
			Gdx.app.debug("LOAD_LEVELS",
					"Non-directory detected by level loader, attempting to load files from list in: " + levelFolder);
			String[] levelNames = handle.readString().split("\n");
			levelHandles = new FileHandle[levelNames.length];

			for (int i = 0; i < levelNames.length; i++) {
				levelNames[i] = levelFolder + levelNames[i];
				FileHandle temp = Gdx.files.internal(levelNames[i]);

				if (temp.exists()) {
					Gdx.app.debug("LOAD_LEVELS", "Found level: " + levelNames[i]);
					levelHandles[i] = temp;
				}
			}
		} else {
			Gdx.app.debug("LOAD_LEVELS", "Directory detected, loading from handle.list.");
			levelHandles = handle.list(".tmx");
		}

		if (levelHandles.length == 0) {
			String message = "No levels found in folder: " + levelFolder;
			Gdx.app.debug("LOAD_LEVELS", message);
			throw new IllegalArgumentException(message);
		}

		Gdx.app.debug("LOAD_LEVELS", String.valueOf(levelHandles.length) + " levels found in " + levelFolder + ".");

		levels = new ArrayList<>(levelHandles.length);
		TmxMapLoader loader = new TmxMapLoader();

		for (FileHandle fh : levelHandles) {
			TiledMap map = loader.load(fh.path());

			if (!isValidLevel(map)) {
				Gdx.app.debug("LOAD_LEVELS", fh.path() + " is an invalid level format. Skipping.");
				continue;
			}

			levels.add(map);
		}

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			String levelNameDebug = "Following levels were loaded: ";
			String delim = ", ";

			for (FileHandle fh : levelHandles) {
				levelNameDebug += fh.name() + delim;
			}

			levelNameDebug = levelNameDebug.substring(0, levelNameDebug.length() - delim.length()) + ".";
			Gdx.app.debug("LOAD_LEVELS", levelNameDebug);
		}
	}

	public static boolean isValidLevel(TiledMap map) {
		return true;
	}

	@Override
	public void dispose() {
		if (levels != null) {
			for (TiledMap map : levels) {
				if (map != null) {
					map.dispose();
					map = null;
				}
			}
		}
	}

}
