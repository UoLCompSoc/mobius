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
import com.sgtcodfish.mobiusListing.CollisionMap;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.components.Collectible;
import com.sgtcodfish.mobiusListing.components.DxLayer;
import com.sgtcodfish.mobiusListing.components.DyLayer;
import com.sgtcodfish.mobiusListing.components.FadableLayer;
import com.sgtcodfish.mobiusListing.components.Interactable;
import com.sgtcodfish.mobiusListing.components.Opacity;
import com.sgtcodfish.mobiusListing.components.PlatformInputListener;
import com.sgtcodfish.mobiusListing.components.PlatformSprite;
import com.sgtcodfish.mobiusListing.components.PlatformSprite.PlatformSpriteOrientation;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;
import com.sgtcodfish.mobiusListing.components.StaticSprite;
import com.sgtcodfish.mobiusListing.components.TiledRenderable;

/**
 * <p>
 * Creates Level {@link Entity}s and handles related resources.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class LevelEntityFactory implements Disposable {
	public static final String				DEFAULT_LEVEL_FOLDER	= "levels/";
	public static final String[]			MAP_NAMES				= { "level1.tmx", "level2.tmx", "level3.tmx",
			"level4.tmx", "level5.tmx", "level6.tmx", "level7.tmx", "level8.tmx", "level9.tmx", "level10.tmx",
			"level11.tmx", "level12.tmx", "level13.tmx", "level14.tmx", "level15.tmx", "level16.tmx" };

	private static final int				PLATFORM_TILE_WIDTH		= 32;
	private static final int				PLATFORM_TILE_HEIGHT	= 32;

	public ArrayList<String>				levels					= null;
	public HashMap<String, Vector2>			levelSpawns				= null;
	public HashMap<String, CollisionMap>	collisionMaps			= null;
	private int								levelNumber				= -1;

	private World							world					= null;

	private Batch							batch					= null;

	public LevelEntityFactory(World world, Batch batch) {
		this(world, batch, DEFAULT_LEVEL_FOLDER);
	}

	public LevelEntityFactory(World world, Batch batch, String folderName) {
		this.world = world;
		this.batch = batch;
		loadLevelsFromList(folderName, MAP_NAMES);
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

		if (levelNumber >= levels.size()) {
			return null;
		} else {
			if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
				Gdx.app.debug("NEXT_LEVEL", "Level's manager contains "
						+ world.getManager(GroupManager.class).getEntities(levels.get(levelNumber)).size + " entities.");
			}
			return world.getManager(GroupManager.class).getEntities(levels.get(levelNumber));
		}
	}

	protected void nextLevel() {
		if (levelNumber >= 0) {
			int deletedCount = 0;

			for (Entity e : world.getManager(GroupManager.class).getEntities(getCurrentLevelGroupName())) {
				e.deleteFromWorld();
				deletedCount++;
			}

			Gdx.app.debug("NEXT_LEVEL", "Deleted entities from old level: " + deletedCount + " deleted.");
		}

		levelNumber++;
		if (levelNumber <= levels.size()) {
			Gdx.app.debug("NEXT_LEVEL", "On level " + (levelNumber + 1) + " of " + levels.size() + ".");
		}
	}

	public String getCurrentLevelGroupName() {
		return levels.get(levelNumber);
	}

	public Vector2 getCurrentLevelSpawn() {
		return levelSpawns.get(getCurrentLevelGroupName());
	}

	public CollisionMap getCurrentLevelCollisionMap() {
		return collisionMaps.get(getCurrentLevelGroupName());
	}

	/**
	 * <p>
	 * Loads all of the levels in the given list. They must exist.
	 * <p>
	 * <strong>Warning:</strong Calling this function while levels are loaded
	 * will delete all loaded levels.
	 * </p>
	 * 
	 * @param prefix
	 *        The prefix to prepend to all level paths in nameList. Can be null.
	 * @param nameList
	 *        A list of paths to levels.
	 */
	protected void loadLevelsFromList(String prefix, String[] nameList) {
		if (nameList == null) {
			throw new IllegalArgumentException("Trying to load levels from null list.");
		} else if (levels != null) {
			dispose();
		}

		TmxMapLoader loader = new TmxMapLoader();
		levels = new ArrayList<>(nameList.length);
		levelSpawns = new HashMap<>(nameList.length);
		collisionMaps = new HashMap<>(nameList.length);

		for (String s : nameList) {
			if (prefix != null) {
				s = prefix + s;
			}

			FileHandle handle = Gdx.files.internal(s);

			if (!handle.exists()) {
				Gdx.app.debug("LOAD_LEVELS", "Level does not exist: " + s + " when loading from list. Skipping.");
				continue;
			}

			TiledMap map = loader.load(handle.path());

			GroupManager groupManager = world.getManager(GroupManager.class);
			String groupName = handle.name();

			Gdx.app.debug("LOAD_LEVELS", "-----");
			Gdx.app.debug("LOAD_LEVELS", "Loading " + handle.name() + ".");

			if (!isValidLevel(map, handle.name())) {
				Gdx.app.debug("LOAD_LEVELS", handle.name() + " is an invalid level format. Skipping.");
				continue;
			}

			Entity level = generateLevelEntity(map);
			Gdx.app.debug("LOAD_LEVELS", "Generated level entity for " + handle.name() + " - id = " + level.id + ".");
			groupManager.add(level, groupName);

			for (int i = 0; i < map.getLayers().getCount(); i++) {
				TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);

				if (isInteractableLayer(layer)) {
					Gdx.app.debug("LOAD_LEVELS", "Loading interactible layer: " + layer.getName());
					groupManager.add(generateInteractablePlatformEntity(layer), groupName);
				} else if (isKeyLayer(layer)) {
					Gdx.app.debug("LOAD_LEVELS", "Loading key layer.");
					String keyName = handle.nameWithoutExtension().substring(0, 0).toUpperCase()
							+ handle.nameWithoutExtension().substring(1) + "'s Key";
					groupManager.add(generateKeyEntity(layer, keyName), groupName);
				} else if (isExitLayer(layer)) {
					Gdx.app.debug("LOAD_LEVELS", "Loading exit layer.");
					groupManager.add(generateExitEntity(layer), groupName);
				} else if (isSpawnLayer(layer)) {
					Gdx.app.debug("LOAD_LEVELS", "Loading player spawn layer.");

					Vector2 spawn = findFirstCell(layer);

					spawn.x *= layer.getTileWidth();
					spawn.y *= layer.getTileHeight();

					levelSpawns.put(groupName, spawn);
				}

				if (isSolidLayer(layer)) {
					if (collisionMaps.get(groupName) == null) {
						collisionMaps.put(groupName, generateCollisionMap(layer));
					} else {
						throw new GdxRuntimeException("Modifying collision maps NYI");
					}
				}
			}

			levels.add(groupName);
			Gdx.app.debug("LOAD_LEVELS", "-----\n");
		}
	}

	protected Entity generateLevelEntity(TiledMap map) {
		Entity e = world.createEntity();

		Position p = world.createComponent(Position.class);
		e.addComponent(p);

		TiledRenderable r = world.createComponent(TiledRenderable.class);
		r.map = map;
		r.renderer = new OrthogonalTiledMapRenderer(r.map, 1.0f, batch);
		r.renderableLayers = new String[3];
		r.renderableLayers[0] = "floor";
		r.renderableLayers[1] = "levelnumber";
		r.renderableLayers[2] = "text";
		e.addComponent(r);

		e.addToWorld();
		e.disable();
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

		e.addToWorld();
		e.disable();
		return e;
	}

	protected Entity generateKeyEntity(TiledMapTileLayer layer, String keyName) {
		Vector2 keyLoc = findFirstCell(layer);

		if (keyLoc == null) {
			throw new IllegalArgumentException("Could not find key in layer \"" + layer.getName() + "\".");
		} else {
			Entity e = world.createEntity();

			Position position = world.createComponent(Position.class);
			position.position.x = keyLoc.x * layer.getTileWidth();
			position.position.y = keyLoc.y * layer.getTileHeight();
			e.addComponent(position);

			Solid solid = world.createComponent(Solid.class);
			e.addComponent(solid);

			Collectible collectible = world.createComponent(Collectible.class);
			collectible.name = keyName;
			e.addComponent(collectible);

			StaticSprite staticSprite = world.createComponent(StaticSprite.class);
			staticSprite.textureRegion = layer.getCell((int) keyLoc.x, (int) keyLoc.y).getTile().getTextureRegion();
			e.addComponent(staticSprite);

			e.addToWorld();
			e.disable();
			return e;
		}
	}

	protected Entity generateExitEntity(TiledMapTileLayer layer) {
		Vector2 exitLoc = findFirstCell(layer);

		if (exitLoc == null) {
			throw new IllegalArgumentException("Could not find exit in layer \"" + layer.getName() + "\".");
		} else {
			Entity e = world.createEntity();

			Position p = world.createComponent(Position.class);
			p.position.x = exitLoc.x * layer.getTileWidth();
			p.position.y = exitLoc.y * layer.getTileHeight();
			e.addComponent(p);

			Interactable i = world.createComponent(Interactable.class);
			e.addComponent(i);

			StaticSprite s = world.createComponent(StaticSprite.class);
			s.textureRegion = layer.getCell((int) exitLoc.x, (int) exitLoc.y).getTile().getTextureRegion();
			e.addComponent(s);

			e.addToWorld();
			e.disable();
			return e;
		}
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
	protected Texture generateHorizontalPlatformTexture(int size) {
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
	protected Texture generateVerticalPlatformTexture(int size) {
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

		// image is BOTTOM TOP MIDDLE (laid out horizontally)
		topEnd.drawPixmap(platform, 0, 0, PLATFORM_TILE_WIDTH * 1, 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);
		middle.drawPixmap(platform, 0, 0, PLATFORM_TILE_WIDTH * 2, 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);
		bottomEnd.drawPixmap(platform, 0, 0, PLATFORM_TILE_WIDTH * 0, 0, PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT);

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
	 * Scans every cell in the layer and indiscriminately marks it as solid in a
	 * collision map.
	 * </p>
	 * 
	 * @param layer
	 *        The layer to use to generate the map.
	 * @return A boolean array where map[layer.getWidth() * y + x] is a cell,
	 *         and true means "solid".
	 */
	protected CollisionMap generateCollisionMap(TiledMapTileLayer layer) {
		Array<Boolean> collisionMap = new Array<Boolean>();
		collisionMap.ensureCapacity(layer.getWidth() * layer.getHeight());

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				collisionMap.add((layer.getCell(x, y) != null));
			}
		}

		CollisionMap retVal = new CollisionMap(layer, collisionMap);
		return retVal;
	}

	protected static boolean isSolidLayer(TiledMapTileLayer layer) {
		return (layer.getProperties().get("solid", null, String.class) != null);
	}

	protected static boolean isExitLayer(TiledMapTileLayer layer) {
		return layer.getName().equals("exit");
	}

	protected static boolean isKeyLayer(TiledMapTileLayer layer) {
		return layer.getName().equals("key");
	}

	protected static boolean isSpawnLayer(TiledMapTileLayer layer) {
		return layer.getName().equals("playerspawn");
	}

	/**
	 * <p>
	 * Returns true if the layer has a property as listed in
	 * {@link WorldConstants#interactableLayersProperties}.
	 * </p>
	 * 
	 * @param layer
	 *        The layer whose properties are to be checked.
	 * @return true if the layer validates as an interactable layer, false for
	 *         all other layers.
	 */
	protected static boolean isInteractableLayer(TiledMapTileLayer layer) {
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
	 *         tile coordinates), or null if the layer is empty. x is right, y
	 *         is down.
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

		Gdx.app.debug("IS_VALID_LEVEL", "Checking " + mapName + ".");

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
				+ " dx-layers,\n" + dyLayers + " dy-layers,\n" + minOpacityLayers + " fadable-layers,\nfor a total of "
				+ propLayers + " layers which can be changed.");

		return retVal;
	}

	public void reset() {
		levelNumber = -1;
		if (levels != null) {
			levels.clear();
			levels = null;
		}
	}

	@Override
	public void dispose() {
		reset();

		world = null;
		batch = null;
	}
}
