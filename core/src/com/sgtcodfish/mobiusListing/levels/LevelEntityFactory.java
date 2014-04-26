package com.sgtcodfish.mobiusListing.levels;

import java.util.ArrayList;
import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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

			if (!isValidLevel(map, fh.path())) {
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

	/**
	 * <p>
	 * Comprehensively checks the given map for defects as an early warning
	 * system against mistakes during map making.
	 * </p>
	 * <p>
	 * Most useful when called with debugging enabled.
	 * </p>
	 * 
	 * @param map
	 *        The map to check.
	 * @param mapName
	 *        The name of the map to use in debug output.
	 * @return True if the map is technically valid (could still have warnings
	 *         though), false if something is fundamentally wrong.
	 */
	public static boolean isValidLevel(TiledMap map, String mapName) {
		boolean retVal = true;

		HashMap<String, Boolean> found = new HashMap<>();
		found.put("floor", false);
		found.put("key", false);
		found.put("exit", false);
		found.put("playerspawn", false);

		int otherLayers = 0, dxLayers = 0, dyLayers = 0, minOpacityLayers = 0, propLayers = 0;

		for (int i = 0; i < map.getLayers().getCount(); i++) {
			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
			String nlower = layer.getName().toLowerCase();

			if (!layer.getName().equals(nlower)) {
				Gdx.app.debug("IS_VALID_LEVEL", "Warning: non-lowercase layer name \"" + layer.getName() + "\" in "
						+ mapName);
			}

			if (found.containsKey(nlower)) {
				found.put(nlower, true);
			} else {
				otherLayers++;
			}

			MapProperties props = layer.getProperties();

			if (props != null) {
				int propCount = 0;

				if (props.get("dx") != null) {
					dxLayers++;
					propCount++;
				}

				if (props.get("dy") != null) {
					dyLayers++;
					propCount++;
				}

				if (props.get("minOpacity") != null) {
					minOpacityLayers++;
					propCount++;
				}

				if (propCount >= 1) {
					propLayers++;
				}

				if (propCount > 1) {
					Gdx.app.debug("IS_VALID_LEVEL", "Warning: map: " + mapName + ", layer: " + layer.getName()
							+ ":- Has " + propCount + " properties that change behaviour.");
				}
			}
		}

		for (String key : found.keySet()) {
			if (!found.get(key).booleanValue()) {
				Gdx.app.debug("IS_VALID_LEVEL", "Layer not found in map " + mapName + ": " + key);
				retVal = false;
			}
		}

		Gdx.app.debug("IS_VALID_LEVEL", "\n" + mapName + " contains " + otherLayers
				+ " non-essential layers, including:\n" + dxLayers + " dx-layers,\n" + dyLayers + " dy-layers,\n"
				+ minOpacityLayers + " minOpacity-layers,\nFor a total of " + propLayers
				+ " layers which can be changed.\n");

		return retVal;
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
