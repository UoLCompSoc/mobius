package com.sgtcodfish.mobiusListing.levels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sgtcodfish.mobiusListing.Item;
import com.sgtcodfish.mobiusListing.Item.ItemType;
import com.sgtcodfish.mobiusListing.TerrainCollisionMap;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.WorldConstants.InteractableLayerTypes;
import com.sgtcodfish.mobiusListing.components.ChildLinked;
import com.sgtcodfish.mobiusListing.components.Collectable;
import com.sgtcodfish.mobiusListing.components.DxLayer;
import com.sgtcodfish.mobiusListing.components.DyLayer;
import com.sgtcodfish.mobiusListing.components.FadableLayer;
import com.sgtcodfish.mobiusListing.components.Interactable;
import com.sgtcodfish.mobiusListing.components.Linked;
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
	public static final String					DEFAULT_LEVEL_FOLDER	= "levels/";

	// set to true to output detailed info about each level during loading
	public static boolean						VERBOSE_LOAD			= false;

	// note: level5, level7, level9, level11, level12, level15, level27 removed.
	// level11: thin corridors
	// level12: over-edge fade platform
	public static final String[]				MAP_NAMES				= { "level0.tmx", "level1.tmx", "level2.tmx",
			"level3.tmx", "level4.tmx", "level6.tmx", "level8.tmx", "level10.tmx", "level13.tmx", "level14.tmx",
			"level16.tmx", "level17.tmx", "level18.tmx", "level19.tmx", "level20.tmx", "level21.tmx", "level22.tmx",
			"level23.tmx", "level24.tmx", "level25.tmx", "level26.tmx", "level28.tmx", "level29.tmx", "level30.tmx",
			"level31.tmx", "level32.tmx"								};

	public static final String[]				VITAL_LAYERS			= { "floor", "key", "exit", "playerspawn" };

	public static final String					MIRROR_GROUP_EXTENSION	= "_mirror";

	private static final int					PLATFORM_TILE_WIDTH		= 32;
	private static final int					PLATFORM_TILE_HEIGHT	= 32;

	public ArrayList<String>					levels					= null;
	public HashMap<String, Vector2>				levelSpawns				= null;
	public HashMap<String, TerrainCollisionMap>	collisionMaps			= null;
	private int									levelNumber				= -1;

	private World								world					= null;

	private Batch								batch					= null;

	public LevelEntityFactory(World world, Batch batch) {
		this(world, batch, DEFAULT_LEVEL_FOLDER);
	}

	public LevelEntityFactory(World world, Batch batch, String folderName) {
		this.world = world;
		this.batch = batch;

		if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
			String[] MAP_NAMES_DEBUG = new String[MAP_NAMES.length + 1];
			MAP_NAMES_DEBUG[0] = new String("debug.tmx");
			System.arraycopy(MAP_NAMES, 0, MAP_NAMES_DEBUG, 1, MAP_NAMES.length);

			loadLevelsFromList(folderName, MAP_NAMES_DEBUG);
		} else {
			loadLevelsFromList(folderName, MAP_NAMES);
		}
	}

	/**
	 * <p>
	 * Generates an array of entities for the next level, and returns it.
	 * Returns false if there are no more levels.
	 * </p>
	 * 
	 * @return true if a level was loaded, false if there are no more levels.
	 */
	public boolean loadNextLevel() {
		nextLevel();

		if (levelNumber >= levels.size()) {
			return false;
		} else {
			if (Gdx.app.getLogLevel() == Application.LOG_DEBUG) {
				Gdx.app.debug("NEXT_LEVEL", "Level " + getCurrentLevelGroupName() + "'s manager contains "
						+ world.getManager(GroupManager.class).getEntities(levels.get(levelNumber)).size + " entities.");
			}

			return true;
		}
	}

	protected void nextLevel() {
		if (levelNumber >= 0) {
			int deletedCount = 0;

			for (Entity e : getCurrentLevelEntities()) {
				e.deleteFromWorld();
				deletedCount++;
			}

			for (Entity e : getCurrentMirroredLevelEntities()) {
				e.deleteFromWorld();
				deletedCount++;
			}

			Gdx.app.debug("NEXT_LEVEL", "Deleted " + deletedCount + " entities from old level.");
		}

		levelNumber++;

		if (levelNumber < levels.size()) {
			Gdx.app.debug("NEXT_LEVEL", "On level " + (levelNumber + 1) + " of " + (levels.size() + 1) + ".");

			int enabledCount = 0;

			for (Entity e : getCurrentLevelEntities()) {
				e.enable();
				enabledCount++;
			}

			for (Entity e : getCurrentMirroredLevelEntities()) {
				e.enable();
				enabledCount++;
			}

			Gdx.app.debug("NEXT_LEVEL", "Enabled " + enabledCount + " entities for new level.");
		}
	}

	public String getCurrentLevelGroupName() {
		return levels.get(levelNumber);
	}

	public String getCurrentLevelMirrorGroupName() {
		return getCurrentLevelGroupName() + MIRROR_GROUP_EXTENSION;
	}

	public Vector2 getCurrentLevelSpawn() {
		return levelSpawns.get(getCurrentLevelGroupName());
	}

	public TerrainCollisionMap getCurrentLevelCollisionMap() {
		return collisionMaps.get(getCurrentLevelGroupName());
	}

	protected Array<Entity> getCurrentLevelEntities() {
		return world.getManager(GroupManager.class).getEntities(getCurrentLevelGroupName());
	}

	protected Array<Entity> getCurrentMirroredLevelEntities() {
		return world.getManager(GroupManager.class).getEntities(getCurrentLevelMirrorGroupName());
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
		levels = new ArrayList<String>(nameList.length);
		levelSpawns = new HashMap<String, Vector2>(nameList.length);
		collisionMaps = new HashMap<String, TerrainCollisionMap>(nameList.length);

		for (String s : nameList) {
			if (prefix != null) {
				s = prefix + s;
			}

			FileHandle handle = Gdx.files.internal(s);

			if (!handle.exists()) {
				Gdx.app.debug("LOAD_LEVELS", "Level does not exist: " + s + " when loading from list. Skipping.");
				continue;
			} else {
				loadLevel(loader, handle);
			}
		}
	}

	protected void loadLevel(TmxMapLoader loader, FileHandle handle) {
		TiledMap map = loader.load(handle.path());

		String groupName = handle.name();

		if (VERBOSE_LOAD)
			Gdx.app.debug("LOAD_LEVELS", "-----");
		if (VERBOSE_LOAD)
			Gdx.app.debug("LOAD_LEVELS", "Loading " + handle.name() + ".");

		if (!isValidLevel(map, handle.name())) {
			Gdx.app.debug("LOAD_LEVELS", handle.name() + " is an invalid level format. Skipping.");
			return;
		}

		TiledMap invertedMap = generateInvertedMap(map);

		TiledMapTileLayer floorLayer = ((TiledMapTileLayer) (map.getLayers().get("floor")));
		float mapWidth = floorLayer.getTileWidth() * floorLayer.getWidth();
		float mapHeight = floorLayer.getTileHeight() * floorLayer.getHeight();
		float yFlip = mapHeight;

		if (VERBOSE_LOAD)
			Gdx.app.debug("LOAD_LEVELS", "Level details:\nWidth in tiles (w, h): (" + floorLayer.getWidth() + ", "
					+ floorLayer.getHeight() + ")\nTile dimensions in pixels (w, h): (" + floorLayer.getTileWidth()
					+ ", " + floorLayer.getTileHeight() + ")\nMap dimensions in pixels (w, h): (" + mapWidth + ", "
					+ mapHeight + ").");

		Entity level = generateLevelEntity(map, 0.0f, 0.0f);
		Entity mirrorLevel = generateLevelEntity(invertedMap, mapWidth, 0.0f);

		Linked levelLink = world.createComponent(Linked.class);
		levelLink.child = mirrorLevel;
		levelLink.performer = new Linked.PassLink();
		level.addComponent(levelLink);

		ChildLinked.makeChild(world, level, mirrorLevel);

		world.getManager(GroupManager.class).add(level, groupName);
		world.getManager(GroupManager.class).add(mirrorLevel, groupName + MIRROR_GROUP_EXTENSION);

		if (VERBOSE_LOAD)
			Gdx.app.debug("LOAD_LEVELS", "Generated regular and mirror level entities for " + handle.name()
					+ " - id reg = " + level.id + ", mirror = " + mirrorLevel.id);

		for (int i = 0; i < map.getLayers().getCount(); i++) {
			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
			TiledMapTileLayer mirrorLayer = (TiledMapTileLayer) invertedMap.getLayers().get(i);

			if (isInteractableLayer(layer)) {
				if (VERBOSE_LOAD)
					Gdx.app.debug("LOAD_LEVELS", "Loading interactible layer: " + layer.getName());
				Entity platformEntity = generateInteractablePlatformEntity(layer, false, 0.0f, 0.0f);
				Entity mirroredPlatformEntity = generateInteractablePlatformEntity(mirrorLayer, true, mapWidth, 0.0f);

				positionLinkAndCommitToGroup(platformEntity, mirroredPlatformEntity, mapWidth, yFlip, groupName);

			} else if (isKeyLayer(layer)) {
				if (VERBOSE_LOAD)
					Gdx.app.debug("LOAD_LEVELS", "Loading key layer.");
				String firstChar = ("" + handle.nameWithoutExtension().charAt(0)).toUpperCase();
				String keyName = firstChar + handle.nameWithoutExtension().substring(1) + " Key";
				String mirrorKeyName = keyName + MIRROR_GROUP_EXTENSION;

				Entity keyEntity = generateKeyEntity(layer, keyName, false, 0.0f, 0.0f);
				Entity mirroredKeyEntity = generateKeyEntity(mirrorLayer, mirrorKeyName, true, mapWidth, 0.0f);
				positionLinkAndCommitToGroup(keyEntity, mirroredKeyEntity, mapWidth, yFlip, groupName);

			} else if (isExitLayer(layer)) {
				if (VERBOSE_LOAD)
					Gdx.app.debug("LOAD_LEVELS", "Loading exit layer.");
				Entity exitEntity = generateExitEntity(layer, false, 0.0f, 0.0f);
				Entity mirroredExitEntity = generateExitEntity(mirrorLayer, true, mapWidth, 0.0f);
				positionLinkAndCommitToGroup(exitEntity, mirroredExitEntity, mapWidth, yFlip, groupName);

			} else if (isSpawnLayer(layer)) {
				if (VERBOSE_LOAD)
					Gdx.app.debug("LOAD_LEVELS", "Loading player spawn layer.");

				Vector2 spawn = findFirstCell(layer);

				spawn.x *= layer.getTileWidth();
				spawn.y *= layer.getTileHeight();

				levelSpawns.put(groupName, spawn);
			}
		}

		if (collisionMaps.get(groupName) == null) {
			TerrainCollisionMap tempCollMap = TerrainCollisionMap.generateCollisionMap(map, invertedMap);
			if (tempCollMap == null) {
				Gdx.app.error("LOAD_LEVELS", "Could not create collision map for level " + groupName + ".");
			} else {
				collisionMaps.put(groupName, tempCollMap);
			}
		} else {
			throw new GdxRuntimeException("Modifying collision maps NYI");
		}

		levels.add(groupName);
		if (VERBOSE_LOAD)
			Gdx.app.debug("LOAD_LEVELS", "-----\n");
	}

	protected void positionLinkAndCommitToGroup(Entity parent, Entity child, float xOffset, float yFlip,
			String groupName) {
		if (parent == null || child == null) {
			throw new IllegalArgumentException("Null parent or child passed to posLink+Commit, name = " + groupName);
		}

		Linked.makePositionOpacityLink(world, parent, child, xOffset, yFlip);
		ChildLinked.makeChild(world, parent, child);

		world.getManager(GroupManager.class).add(parent, groupName);
		world.getManager(GroupManager.class).add(child, groupName + MIRROR_GROUP_EXTENSION);
	}

	protected Entity generateLevelEntity(TiledMap map, float x, float y) {
		Entity e = world.createEntity();

		Position p = world.createComponent(Position.class);
		p.position.set(x, y);
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

	protected Entity generateInteractablePlatformEntity(TiledMapTileLayer layer, boolean mirrored, float xOffset,
			float yOffset) {
		Entity e = world.createEntity();

		MapProperties properties = layer.getProperties();

		Rectangle platformRect = calculatePlatformRect(layer);
		if (platformRect == null) {
			Gdx.app.debug("LOAD_LEVEL", "Couldn't create platform rect for layer " + layer);
			return null;
		}

		Texture texture = null;

		int platSize = 0;

		PlatformSpriteOrientation orientation = PlatformSpriteOrientation.NONE;

		if (platformRect.height == 1.0f) { // if height is 1 tile
			texture = generateHorizontalPlatformTexture((int) platformRect.width);
			platSize = (int) platformRect.width;
			orientation = PlatformSpriteOrientation.HORIZONTAL;
		} else if (platformRect.width == 1.0f) {
			texture = generateVerticalPlatformTexture((int) platformRect.height);
			platSize = (int) platformRect.height;
			orientation = PlatformSpriteOrientation.VERTICAL;
		}

		Position position = world.createComponent(Position.class);
		position.position.x = platformRect.x + xOffset;
		position.position.y = platformRect.y + yOffset;

		PlatformSprite platformSprite = world.createComponent(PlatformSprite.class);
		platformSprite.setTexture(texture);
		platformSprite.mirrored = mirrored;
		platformSprite.spriteWidth = texture.getWidth();
		platformSprite.spriteHeight = texture.getHeight();
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

		Solid solid = world.createComponent(Solid.class);
		solid.boundingBox = platformSprite.rectangle;
		solid.invertedGravity = false;
		solid.weight = 100.0f;

		e.addToWorld();
		e.disable();
		return e;
	}

	protected Entity generateKeyEntity(TiledMapTileLayer layer, String keyName, boolean mirrored, float xOffset,
			float yOffset) {
		Vector2 keyLoc = findFirstCell(layer);

		if (keyLoc == null) {
			throw new IllegalArgumentException("Could not find key in layer \"" + layer.getName() + "\".");
		} else {
			Entity e = world.createEntity();

			Position position = world.createComponent(Position.class);
			position.position.x = keyLoc.x * layer.getTileWidth() + xOffset;
			position.position.y = keyLoc.y * layer.getTileHeight() + yOffset;
			e.addComponent(position);

			Collectable collectable = world.createComponent(Collectable.class);
			collectable.item = new Item(keyName, ItemType.KEY);
			e.addComponent(collectable);

			StaticSprite staticSprite = world.createComponent(StaticSprite.class);
			staticSprite.textureRegion = layer.getCell((int) keyLoc.x, (int) keyLoc.y).getTile().getTextureRegion();
			staticSprite.spriteWidth = staticSprite.textureRegion.getRegionWidth();
			staticSprite.spriteHeight = staticSprite.textureRegion.getRegionHeight();
			staticSprite.mirrored = mirrored;
			e.addComponent(staticSprite);

			Solid solid = world.createComponent(Solid.class);
			solid.boundingBox = new Rectangle(position.position.x, position.position.y, staticSprite.spriteWidth,
					staticSprite.spriteHeight);
			e.addComponent(solid);

			e.addToWorld();
			e.disable();
			return e;
		}
	}

	protected Entity generateExitEntity(TiledMapTileLayer layer, boolean mirrored, float xOffset, float yOffset) {
		Vector2 exitLoc = findFirstCell(layer);

		if (exitLoc == null) {
			throw new IllegalArgumentException("Could not find exit in layer \"" + layer.getName() + "\".");
		} else {
			Entity e = world.createEntity();

			Position p = world.createComponent(Position.class);
			p.position.x = exitLoc.x * layer.getTileWidth() + xOffset;
			p.position.y = exitLoc.y * layer.getTileHeight() + xOffset;
			e.addComponent(p);

			Interactable i = world.createComponent(Interactable.class);
			e.addComponent(i);

			StaticSprite s = world.createComponent(StaticSprite.class);
			s.textureRegion = layer.getCell((int) exitLoc.x, (int) exitLoc.y).getTile().getTextureRegion();
			s.spriteWidth = s.textureRegion.getRegionWidth();
			s.spriteHeight = s.textureRegion.getRegionHeight();
			s.mirrored = mirrored;
			e.addComponent(s);

			Solid solid = world.createComponent(Solid.class);
			solid.boundingBox = new Rectangle(p.position.x, p.position.y, s.spriteWidth, s.spriteHeight);
			solid.invertedGravity = false;
			solid.weight = 0.25f;
			e.addComponent(solid);

			e.addToWorld();
			e.disable();
			return e;
		}
	}

	public static TiledMap generateInvertedMap(TiledMap map) {
		TiledMap invertedMap = new TiledMap();

		MapLayers invertedLayers = invertedMap.getLayers();

		for (int i = 0; i < map.getLayers().getCount(); i++) {
			TiledMapTileLayer origLayer = (TiledMapTileLayer) map.getLayers().get(i);

			TiledMapTileLayer tempLayer = invertLayer(origLayer);

			tempLayer.setOpacity(origLayer.getOpacity());
			tempLayer.setName(origLayer.getName());
			tempLayer.setVisible(origLayer.isVisible());
			copyLayerProperties(origLayer, tempLayer);

			invertedLayers.add(tempLayer);
		}

		return invertedMap;
	}

	public static TiledMapTileLayer invertLayer(TiledMapTileLayer layer) {
		TiledMapTileLayer invertedLayer = new TiledMapTileLayer(layer.getWidth(), layer.getHeight(),
				(int) layer.getTileWidth(), (int) layer.getTileHeight());

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				Cell regularCell = layer.getCell(x, y);
				Cell invertedCell = null;

				if (regularCell != null) {
					invertedCell = new Cell();
					invertedCell.setFlipVertically(true);
					invertedCell.setTile(regularCell.getTile());
				}

				invertedLayer.setCell(x, layer.getHeight() - 1 - y, invertedCell);
			}
		}

		return invertedLayer;
	}

	public static void copyLayerProperties(TiledMapTileLayer from, TiledMapTileLayer to) {
		MapProperties fromP = from.getProperties();
		MapProperties toP = to.getProperties();
		Iterator<String> it = fromP.getKeys();

		while (it.hasNext()) {
			String key = it.next();
			toP.put(key, fromP.get(key));
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
	public static Texture generateHorizontalPlatformTexture(int size) {
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
	public static Texture generateVerticalPlatformTexture(int size) {
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

	public static boolean isSolidLayer(TiledMapTileLayer layer) {
		return (layer.getProperties().get("solid", null, String.class) != null);
	}

	public static boolean isExitLayer(TiledMapTileLayer layer) {
		return layer.getName().equals("exit");
	}

	public static boolean isKeyLayer(TiledMapTileLayer layer) {
		return layer.getName().equals("key");
	}

	public static boolean isSpawnLayer(TiledMapTileLayer layer) {
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
	public static boolean isInteractableLayer(TiledMapTileLayer layer) {
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

	public static InteractableLayerTypes getInteractableLayerType(TiledMapTileLayer layer) {
		MapProperties props = layer.getProperties();

		for (String s : WorldConstants.interactableLayersProperties) {
			if (props.get(s) != null) {
				return InteractableLayerTypes.fromProperty(s);
			}
		}

		return null;
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
	public static Vector2 findFirstCell(TiledMapTileLayer layer) {
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
	public static Rectangle calculatePlatformRect(TiledMapTileLayer layer) {
		Rectangle retVal = new Rectangle();

		Vector2 first = findFirstCell(layer);
		if (first == null) {
			Gdx.app.debug("CALC_PLATFORM_RECT", "Couldn't find first cell in platform layer.");
			return null;
		}
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

		HashMap<String, Boolean> found = new HashMap<String, Boolean>();
		for (String s : VITAL_LAYERS) {
			found.put(s, false);
		}

		int otherLayers = 0, dxLayers = 0, dyLayers = 0, minOpacityLayers = 0, propLayers = 0;

		if (VERBOSE_LOAD)
			Gdx.app.debug("IS_VALID_LEVEL", "Checking " + mapName + ".");

		for (int i = 0; i < map.getLayers().getCount(); i++) {
			TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(i);
			String nlower = layer.getName().toLowerCase();

			if (!layer.getName().equals(nlower)) {
				if (VERBOSE_LOAD)
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
					if (VERBOSE_LOAD)
						Gdx.app.debug("IS_VALID_LEVEL", "Warning: layer \"" + layer.getName() + "\":- Has " + propCount
								+ " properties that change behaviour, 1 recommended.");
				}
			}
		}

		for (String key : found.keySet()) {
			if (!found.get(key).booleanValue()) {
				Gdx.app.debug("IS_VALID_LEVEL", "Error: Vital layer not found in map " + mapName + ": " + key);
				retVal = false;
			}
		}

		if (VERBOSE_LOAD)
			Gdx.app.debug("IS_VALID_LEVEL", "Contains " + otherLayers + " non-essential layers, including:\n"
					+ dxLayers + " dx-layers,\n" + dyLayers + " dy-layers,\n" + minOpacityLayers
					+ " fadable-layers,\nfor a total of " + propLayers + " layers which can be changed.");

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
