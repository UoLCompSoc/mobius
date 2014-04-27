package com.sgtcodfish.mobiusListing.levels;

import java.util.ArrayList;
import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.World;
import com.artemis.managers.GroupManager;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.components.DxLayer;
import com.sgtcodfish.mobiusListing.components.DyLayer;
import com.sgtcodfish.mobiusListing.components.FadableLayer;
import com.sgtcodfish.mobiusListing.components.Opacity;
import com.sgtcodfish.mobiusListing.components.PlatformInputListener;
import com.sgtcodfish.mobiusListing.components.PlatformSprite;
import com.sgtcodfish.mobiusListing.components.PlatformSprite.PlatformSpriteOrientation;
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
	public static final String		DEFAULT_LEVEL_FOLDER	= "levels/";
	public static final String[]	MAP_NAMES				= { "titlescreen.tmx", "level1.tmx", "level2.tmx",
			"level3.tmx", "level4.tmx"						};

	private static final int		PLATFORM_TILE_WIDTH		= 32;
	private static final int		PLATFORM_TILE_HEIGHT	= 32;

	public ArrayList<String>		levels					= null;
	private int						levelNumber				= -1;

	private World					world					= null;

	private Batch					batch					= null;

	public LevelEntityFactory(World world, Batch batch) {
		this(world, batch, DEFAULT_LEVEL_FOLDER);
	}

	public LevelEntityFactory(World world, Batch batch, String folderName) {
		this.world = world;
		this.batch = batch;
		loadLevelsFromFolder(folderName);
	}

	/**
	 * <p>
	 * Generates an array of entities for the next level, and returns it.
	 * Returns null if there are no more levels.
	 * </p>
	 * 
	 * @return The next level entity, or null if there are no more.
	 */
	public Array<Entity> getNextLevelEntityList() {
		nextLevel();

		if (levels.size() <= levelNumber) {
			return null;
		} else {
			return world.getManager(GroupManager.class).getEntities(levels.get(levelNumber));
		}
	}

	protected void nextLevel() {
		if (levelNumber >= 0) {
			for (Entity e : world.getManager(GroupManager.class).getEntities(getCurrentLevelGroupName())) {
				e.deleteFromWorld();
			}
		}

		levelNumber++;
	}

	public String getCurrentLevelGroupName() {
		return levels.get(levelNumber);
	}

	protected void loadLevelsFromList(String[] nameList) {
		if (nameList == null) {
			throw new IllegalArgumentException("Trying to load levels from null list.");
		}

		for (String s : nameList) {
			FileHandle handle = Gdx.files.internal(s);

			if (!handle.exists()) {
				Gdx.app.debug("LOAD_LEVELS_LIST", "Level does not exist: " + s + " when loading from list. Skipping.");
				continue;
			}
		}
	}

	/**
	 * <p>
	 * Loads all the levels in a specified folder. For internal use; construct a
	 * new LevelEntityFactory to load the levels in a new folder.
	 * </p>
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

		levels = new ArrayList<>();
		TmxMapLoader loader = new TmxMapLoader();

		for (FileHandle fh : levelHandles) {
			TiledMap map = loader.load(fh.path());
			GroupManager groupManager = world.getManager(GroupManager.class);

			if (!isValidLevel(map, fh.path())) {
				Gdx.app.debug("LOAD_LEVELS", fh.path() + " is an invalid level format. Skipping.");
				continue;
			}

			groupManager.add(generateLevelEntity(map), fh.name());

			for (int i = 0; i < map.getLayers().getCount(); i++) {
				TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
				if (isInteractableLayer(layer)) {
					groupManager.add(generateInteractablePlatformEntity(layer), fh.name());
				}
			}

			levels.add(fh.name());
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

	protected boolean isInteractableLayer(TiledMapTileLayer layer) {
		MapProperties props = layer.getProperties();

		if (props != null) {
			for (String key : WorldConstants.interactableLayersProperties) {
				if (props.get(key) != null) {
					return true;
				}
			}
		}

		return false;
	}

	protected Entity generateLevelEntity(TiledMap map) {
		Entity e = world.createEntity();

		Position p = world.createComponent(Position.class);
		e.addComponent(p);

		TiledRenderable r = world.createComponent(TiledRenderable.class);
		r.map = map;
		r.renderer = new OrthogonalTiledMapRenderer(r.map, 1.0f, batch);
		r.renderableLayers = new String[map.getLayers().getCount()];
		r.renderableLayers[0] = "floor";
		r.renderableLayers[1] = "levelnumber";
		e.addComponent(r);

		return e;
	}

	protected Entity generateInteractablePlatformEntity(TiledMapTileLayer layer) {
		Entity e = world.createEntity();

		MapProperties properties = layer.getProperties();

		Rectangle platformRect = calculatePlatformRect(layer);
		Texture texture = null;

		int platSize = 0;

		PlatformSpriteOrientation orientation = PlatformSpriteOrientation.NONE;

		if (platformRect.height == 1.0f) {
			texture = generateHorizontalPlatformTexture((int) platformRect.width);
			platSize = (int) platformRect.width;
			orientation = PlatformSpriteOrientation.HORIZONTAL;
		} else if (platformRect.width == 1.0f) {
			texture = generateVerticalPlatformTexture((int) platformRect.height);
			platSize = (int) platformRect.height;
			orientation = PlatformSpriteOrientation.VERTICAL;
		}

		Position position = world.createComponent(Position.class);
		position.position.x = platformRect.x;
		position.position.y = platformRect.y;

		PlatformSprite platformSprite = world.createComponent(PlatformSprite.class);
		platformSprite.texture = texture;
		platformSprite.size = platSize;
		platformSprite.orientation = orientation;
		platformSprite.rectangle = new Rectangle(platformRect);
		platformSprite.rectangle.width *= PLATFORM_TILE_WIDTH;
		platformSprite.rectangle.height *= PLATFORM_TILE_HEIGHT;

		e.addComponent(position);
		e.addComponent(platformSprite);
		e.addComponent(world.createComponent(PlatformInputListener.class));

		if (properties.get("dx") != null) {
			DxLayer dxLayer = world.createComponent(DxLayer.class);
			dxLayer.fromTiledProperties(properties);
			dxLayer.layer = layer;

			e.addComponent(dxLayer);
		} else if (properties.get("dy") != null) {
			DyLayer dyLayer = world.createComponent(DyLayer.class);
			dyLayer.fromTiledProperties(properties);
			dyLayer.layer = layer;

			e.addComponent(dyLayer);
		} else if (properties.get("minOpacity") != null) {
			Opacity opacity = world.createComponent(Opacity.class);
			opacity.opacity = layer.getOpacity();

			FadableLayer fadableLayer = world.createComponent(FadableLayer.class);
			fadableLayer.layer = layer;
			fadableLayer.setupOpacity(opacity, layer.getOpacity(), properties);

			e.addComponent(opacity);
			e.addComponent(fadableLayer);
		}

		return e;
	}

	/**
	 * <p>
	 * Creates a horizontal platform texture in the requested width (in tiles).
	 * </p>
	 * 
	 * @param size
	 *        The width of the platform in tiles.
	 * @return The platform texture; this is unmanaged so if context is lost it
	 *         will need to be rebuilt.
	 */
	public Texture generateHorizontalPlatformTexture(int size) {
		final String PLATFORM_TILESET_LOCATION = "tilesets/platformw.png";

		if (size < 2) {
			throw new IllegalArgumentException("Platforms must have a size of at least two.");
		}

		FileHandle fh = Gdx.files.internal(PLATFORM_TILESET_LOCATION);

		if (!fh.exists()) {
			throw new GdxRuntimeException("Could not load " + PLATFORM_TILESET_LOCATION
					+ " when generating horizontal platform texture.");
		}

		Texture texture = new Texture(PLATFORM_TILE_WIDTH * size, PLATFORM_TILE_HEIGHT, Format.RGBA8888);
		Pixmap platform = new Pixmap(fh);

		Pixmap leftEnd = new Pixmap(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT, Format.RGBA8888);
		Pixmap middle = new Pixmap(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT, Format.RGBA8888);
		Pixmap rightEnd = new Pixmap(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT, Format.RGBA8888);

		// image is RIGHT LEFT MIDDLE
		leftEnd.drawPixmap(platform, 0, 0, PLATFORM_TILE_WIDTH * 1, 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);
		middle.drawPixmap(platform, 0, 0, PLATFORM_TILE_WIDTH * 2, 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);
		rightEnd.drawPixmap(platform, 0, 0, PLATFORM_TILE_WIDTH * 0, 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);

		texture.draw(leftEnd, 0, 0);

		if (size > 2) {
			for (int i = 0; i < (size - 2); i++) {
				texture.draw(middle, PLATFORM_TILE_WIDTH * (i + 1), 0);
			}
		}

		texture.draw(rightEnd, PLATFORM_TILE_WIDTH * (size - 1), 0);

		if (leftEnd != null) {
			leftEnd.dispose();
		}

		if (middle != null) {
			middle.dispose();
		}

		if (rightEnd != null) {
			rightEnd.dispose();
		}

		if (platform != null) {
			platform.dispose();
		}

		return texture;
	}

	/**
	 * <p>
	 * Creates a vertical platform with the requested height (in tiles).
	 * </p>
	 * 
	 * @param size
	 *        The height of the platform in tiles.
	 */
	public Texture generateVerticalPlatformTexture(int size) {
		final String PLATFORM_TILESET_LOCATION = "tilesets/platformh.png";

		if (size < 2) {
			throw new IllegalArgumentException("Platforms must have a size of at least two.");
		}

		FileHandle fh = Gdx.files.internal(PLATFORM_TILESET_LOCATION);

		if (!fh.exists()) {
			throw new GdxRuntimeException("Could not load " + PLATFORM_TILESET_LOCATION
					+ " when generating vertical platform texture.");
		}

		Texture texture = new Texture(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT * size, Format.RGBA8888);
		Pixmap platform = new Pixmap(fh);

		Pixmap topEnd = new Pixmap(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT, Format.RGBA8888);
		Pixmap middle = new Pixmap(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT, Format.RGBA8888);
		Pixmap bottomEnd = new Pixmap(PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT, Format.RGBA8888);

		// image is BOTTOM TOP MIDDLE
		topEnd.drawPixmap(platform, 0, 0, 0, PLATFORM_TILE_HEIGHT * 1, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);
		middle.drawPixmap(platform, 0, 0, 0, PLATFORM_TILE_HEIGHT * 2, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);
		bottomEnd.drawPixmap(platform, 0, 0, 0, PLATFORM_TILE_HEIGHT * 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);

		texture.draw(topEnd, 0, 0);

		if (size > 2) {
			for (int i = 0; i < (size - 2); i++) {
				texture.draw(middle, 0, PLATFORM_TILE_HEIGHT * (i + 1));
			}
		}

		texture.draw(bottomEnd, 0, PLATFORM_TILE_HEIGHT * (size - 1));

		if (topEnd != null) {
			topEnd.dispose();
		}

		if (middle != null) {
			middle.dispose();
		}

		if (bottomEnd != null) {
			bottomEnd.dispose();
		}

		if (platform != null) {
			platform.dispose();
		}

		return texture;
	}

	/**
	 * <p>
	 * Finds the first non-null cell in a layer. Only really meaningful in some
	 * layers.
	 * </p>
	 * <p>
	 * Cells are searched across x before y is incremented.
	 * </p>
	 * 
	 * @param layer
	 *        The layer whose cell we'll find.
	 * @return An initialised {@link Vector2} with the x and y coordinates (in
	 *         tile coordinates), or null if the layer is empty.
	 */
	protected static Vector2 findFirstCell(TiledMapTileLayer layer) {
		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				if (layer.getCell(x, y) != null) {
					// found first
					Vector2 retVal = new Vector2();

					retVal.x = (float) x;
					retVal.y = (float) y;

					return retVal;
				}
			}
		}

		return null;
	}

	/**
	 * <p>
	 * Calculates the size (width or height) of a horizontal or vertical
	 * platform which is the sole contents of <tt>layer</tt>.
	 * </p>
	 * <p>
	 * If the layer doesn't contain just a platform, behaviour is undefined.
	 * </p>
	 * 
	 * @param layer
	 *        The layer containing just a platform.
	 * @return A {@link Rectangle} containing the platform, with position in
	 *         world coordinates, and size in tiles or null if the layer
	 *         contains only one cell, or null if if the platform's first cell
	 *         is orphaned and doesn't form a platform.
	 */
	protected static Rectangle calculatePlatformRect(TiledMapTileLayer layer) {
		Rectangle retVal = new Rectangle();

		Vector2 first = findFirstCell(layer);
		int firstX = (int) first.x;
		int firstY = (int) first.y;

		if (layer.getCell(firstX + 1, firstY) != null) {
			// horizontal
			int width = 1;

			while (layer.getCell(firstX + width, firstY) != null) {
				width++;
			}

			retVal.width = (float) width;
			retVal.height = 1.0f;
		} else if (layer.getCell(firstX, firstY + 1) != null) {
			// vertical
			int height = 1;

			while (layer.getCell(firstX, firstY + height) != null) {
				height++;
			}

			retVal.width = 1.0f;
			retVal.height = (float) height;
		} else {
			// single cell
			return null;
		}

		retVal.x = firstX * layer.getTileWidth();
		retVal.y = firstY * layer.getTileHeight();

		return retVal;
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

		Gdx.app.debug("IS_VALID_LEVEL", mapName);

		for (int i = 0; i < map.getLayers().getCount(); i++) {
			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
			String nlower = layer.getName().toLowerCase();

			if (!layer.getName().equals(nlower)) {
				Gdx.app.debug("IS_VALID_LEVEL", "Warning: non-lowercase layer name \"" + layer.getName() + "\".");
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

					if (calculatePlatformRect(layer) == null) {
						Gdx.app.debug("IS_VALID_LEVEL",
								"Error: could not produce valid rectangle for platform in layer " + layer.getName());
						retVal = false;
					}
				}

				if (propCount > 1) {
					Gdx.app.debug("IS_VALID_LEVEL", "Warning: layer \"" + layer.getName() + "\":- Has " + propCount
							+ " properties that change behaviour, 1 recommended.");
				}
			}
		}

		for (String key : found.keySet()) {
			if (!found.get(key).booleanValue()) {
				Gdx.app.debug("IS_VALID_LEVEL", "Error: Layer not found in map " + mapName + ": " + key);
				retVal = false;
			}
		}

		Gdx.app.debug("IS_VALID_LEVEL", "Contains " + otherLayers + " non-essential layers, including:\n" + dxLayers
				+ " dx-layers,\n" + dyLayers + " dy-layers,\n" + minOpacityLayers + " fadable-layers,\nFor a total of "
				+ propLayers + " layers which can be changed.\n");

		return retVal;
	}

	@Override
	public void dispose() {
		if (levels != null) {
			levels.clear();
			levels = null;
		}
	}
}
