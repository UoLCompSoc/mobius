package com.sgtcodfish.mobiusListing;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.sgtcodfish.mobiusListing.WorldConstants.InteractableLayerTypes;
import com.sgtcodfish.mobiusListing.levels.LevelEntityFactory;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TerrainCollisionMap implements Poolable {
	private TiledMapTileLayer										layer			= null;
	private HashMap<TiledMapTileLayer, PlatformManipulationHandler>	platformLayers	= null;
	private ArrayList<Integer>										collisionMap	= null;

	protected TerrainCollisionMap(TiledMapTileLayer layer,
			HashMap<TiledMapTileLayer, PlatformManipulationHandler> platformLayers, ArrayList<Integer> collisionMap) {
		this.layer = layer;
		this.platformLayers = platformLayers;
		this.collisionMap = collisionMap;
	}

	/**
	 * @param p
	 *        The position vector to use for checking (in world coordinates).
	 * @param v
	 *        The velocity vector to use for checking (in world
	 *        coordinates/frame).
	 * @return true if the object travelling in the given tile (converted from
	 *         world coordinates in variable p) with velocity v will collide in
	 *         either y direction.
	 */
	public boolean willCollideY(Vector2 p, Vector2 v) {
		int tx = worldToTileCoordinatesX(p.x + v.x);

		int ty = worldToTileCoordinatesY(p.y + v.y);

		if (tx == -1 || ty == -1) {
			return false;
		} else {
			return isSolidTile(tx, ty);
		}
	}

	/**
	 * @param p
	 *        The position vector (in world coordinates) to use for checking.
	 * @param v
	 *        The velocity vector (in world coordinates/frame) to use for
	 *        checking. Note that the y velocity may be ignored.
	 * @return true if the object travelling in the given tile(converted from
	 *         world coordinates in argument p) with velocity v will collide in
	 *         either x direction.
	 */
	public boolean willCollideX(Vector2 p, Vector2 v) {
		int ty = worldToTileCoordinatesY(p.y);

		int tx = worldToTileCoordinatesX(p.x + v.x);

		if (tx == -1 || ty == -1) {
			return false;
		} else {
			return isSolidTile(tx, ty);
		}
	}

	public void layerInteracted(TiledMapTileLayer interacted, int degree) {
		PlatformManipulationHandler handler = platformLayers.get(interacted);

		if (handler == null) {
			Gdx.app.debug("LAYER_INTERACTED", "Call to layerInteracted with improperly initialised platform layer.");
		} else {
			handler.update(degree);
		}
	}

	/**
	 * <p>
	 * Checks to see if the tile specified by (tileX, tileY) is solid.
	 * </p>
	 * <p>
	 * <em>Note:</em> this function doesn't perform checking on its arguments
	 * and invalid coordinates lead to undefined behaviour. Ensure coordinates
	 * are correct before passing them.
	 * </p>
	 * 
	 * @param tileX
	 *        The x coordinate of the tile, assumed to be inside the map.
	 * @param tileY
	 *        The y coordinate of the tile, assumed to be inside the map.
	 * @return true if the tile is solid, false if it is not.
	 */
	public boolean isSolidTile(int tileX, int tileY) {
		return mapRetrieve(tileX, tileY) >= 1;
	}

	/**
	 * <p>
	 * Retrieves the cell specified by <em>(tileX, tileY)</em> from the map.
	 * Values of 1 or greater indiciate a solid tile.
	 * </p>
	 * 
	 * @param tileX
	 *        The x coordinate, in tile coordinates of the cell to retrieve.
	 * @param tileY
	 *        The y coordinate, in tile coordinates of the cell to retrieve.
	 * @return An integer where 0 or less indicates non-collidable terrain and 1
	 *         or greater indicates solid terrain.
	 */
	public int mapRetrieve(int tileX, int tileY) {
		return collisionMap.get((int) (tileY * (actualWidthInTiles()) + tileX)).intValue();
	}

	/**
	 * <p>
	 * Converts x coordinates in world space to tile coordinates; this probably
	 * involves dividing by the tile width.
	 * <p>
	 * 
	 * @param x
	 *        The coordinate, in world coordinates to convert.
	 * @return An x coordinate in tile coordinates.
	 */
	public int worldToTileCoordinatesX(float x) {
		int tx = (int) (x / layer.getTileWidth());

		if (tx < 0 || tx >= actualWidthInTiles()) {
			tx = tx % actualWidthInTiles();
		}

		return tx;
	}

	/**
	 * <p>
	 * Converts y coordinates in world space to tile coordinates; this probably
	 * involves dividing by the tile height.
	 * <p>
	 * 
	 * @param y
	 *        The y coordinate, in world coordinates to convert.
	 * @return An y coordinate in tile coordinates.
	 */
	public int worldToTileCoordinatesY(float y) {
		int ty = (int) (y / layer.getTileHeight());

		if (ty < 0 || ty >= layer.getHeight()) {
			ty = ty % actualHeightInTiles();
		}

		return ty;
	}

	public float tileToWorldCoordinatesX(int x) {
		return x * layer.getTileWidth();
	}

	public float tileToWorldCoordinatesY(int y) {
		return y * layer.getTileHeight();
	}

	public float worldToGridWorldCoordinatesX(float x) {
		return x - (x % layer.getTileWidth());
	}

	public float worldToGridWorldCoordinatesY(float y) {
		return y - (y % layer.getTileHeight());
	}

	public int actualWidthInTiles() {
		return layer.getWidth() * 2;
	}

	public float actualWidthInWorld() {
		return layer.getWidth() * layer.getTileWidth() * 2;
	}

	public int actualHeightInTiles() {
		return layer.getHeight();
	}

	/**
	 * <p>
	 * Debug only, returns the collision map
	 * </p>
	 * 
	 * @return the integer-array collision map as stored in this
	 *         {@link TerrainCollisionMap}
	 */
	public ArrayList<Integer> getMap() {
		return collisionMap;
	}

	@Override
	public void reset() {
		layer = null;
		platformLayers.clear();
		collisionMap.clear();
	}

	/**
	 * <p>
	 * Scans every cell in the layer and indiscriminately marks it as solid in a
	 * collision map, and returns the map.
	 * </p>
	 * 
	 * @param layer
	 *        The layer to use to generate the map.
	 * @param mirrorLayer
	 *        The mirror layer corresponding to the layer.
	 * @return A boolean array where map[layer.getWidth() * y + x] is a cell,
	 *         and true means "solid".
	 */
	public static ArrayList<Integer> generateCollisionArray(TiledMapTileLayer layer, TiledMapTileLayer mirrorLayer) {
		ArrayList<Integer> collisionMap = new ArrayList<Integer>();

		collisionMap.ensureCapacity((layer.getWidth() + mirrorLayer.getWidth()) * layer.getHeight());

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < (layer.getWidth() + mirrorLayer.getWidth()); x++) {
				if (x < layer.getWidth()) {
					collisionMap.add((layer.getCell(x, y) != null ? 1 : 0));
				} else {
					collisionMap.add((mirrorLayer.getCell(x - layer.getWidth(), y) != null ? 1 : 0));
				}
			}
		}

		return collisionMap;
	}

	/**
	 * <p>
	 * Takes an already partially populated collision array for the map
	 * containing the layer and mirror layer given, adds to it the contents of
	 * the layers supplied and returns the modified array.
	 * </p>
	 * <p>
	 * Behaviour is undefined if <em>collisionMap</em> does not have similar
	 * dimensions to the layers given.
	 * </p>
	 * 
	 * @param collisionMap
	 *        A pre-populated list of integers to be modified.
	 * @param layer
	 *        A layer to add to the map.
	 * @param mirrorLayer
	 *        The mirror of <em>layer</em>
	 * @return collisionMap with the contents of the layers added.
	 */
	public static ArrayList<Integer> accumulateCollisionArray(ArrayList<Integer> collisionMap, TiledMapTileLayer layer,
			TiledMapTileLayer mirrorLayer) {
		// first check the array is sensibly dimensioned for the input layers
		int expectedSize = layer.getWidth() * 2 * layer.getHeight();
		int totalWidth = layer.getWidth() + mirrorLayer.getWidth();
		// widths should be the same but let's be careful

		if (collisionMap.size() != expectedSize) {
			Gdx.app.debug("ACCUMULATE_COLLISION_ARRAY", "Expected array of size " + expectedSize
					+ " for accumulation but only got size of " + collisionMap.size() + ".");
		}

		for (int y = 0; y < layer.getHeight(); y++) {
			// split into two loops to avoid an if statement and possible branch
			// prediction fails
			for (int x = 0; x < layer.getWidth(); x++) {
				int offset = y * totalWidth + x; // location of cell in array

				if (layer.getCell(x, y) != null) {
					collisionMap.set(offset, collisionMap.get(offset).intValue() + 1);
				}
			}

			for (int x = 0; x < mirrorLayer.getWidth(); x++) {
				int offset = y * totalWidth + x + layer.getWidth();

				if (mirrorLayer.getCell(x, y) != null) {
					collisionMap.set(offset, collisionMap.get(offset).intValue() + 1);
				}
			}
		}

		return collisionMap;
	}

	/**
	 * <p>
	 * Creates and returns a new {@link TerrainCollisionMap} from the given map.
	 * If the map is not a valid Möbius map, the behaviour is undefined.
	 * </p>
	 * 
	 * @param map
	 *        The map from which to create a TerrainCollisionMap.
	 * @return
	 */
	public static TerrainCollisionMap generateCollisionMap(TiledMap map, TiledMap mirrorMap) {
		TerrainCollisionMap retVal = null;

		MapLayers layers = map.getLayers();
		MapLayers mirrorLayers = mirrorMap.getLayers();

		TiledMapTileLayer egLayer = (TiledMapTileLayer) layers.get(0);
		HashMap<TiledMapTileLayer, PlatformManipulationHandler> platformLayers = new HashMap<TiledMapTileLayer, PlatformManipulationHandler>();

		ArrayList<Integer> collisionArray = null;

		for (int i = 0; i < layers.getCount(); i++) {
			TiledMapTileLayer origLayer = (TiledMapTileLayer) layers.get(i);
			TiledMapTileLayer mirrorLayer = (TiledMapTileLayer) mirrorLayers.get(i);

			if ((LevelEntityFactory.isSolidLayer(origLayer) && LevelEntityFactory.isSolidLayer(mirrorLayer))
					|| (LevelEntityFactory.isInteractableLayer(origLayer) && LevelEntityFactory
							.isInteractableLayer(mirrorLayer))) {
				if (collisionArray == null) {
					collisionArray = generateCollisionArray(origLayer, mirrorLayer);
				} else {
					if (origLayer.getOpacity() >= WorldConstants.GLOBAL_SOLID_OPACITY_THRESHOLD) {
						collisionArray = accumulateCollisionArray(collisionArray, origLayer, mirrorLayer);
					}
				}

				if (LevelEntityFactory.isInteractableLayer(origLayer)) {
					PlatformManipulationHandler handler = null;
					ArrayList<TerrainMapTileCoordinate> tileCoords = generatePlatformTileCoordinateArray(origLayer,
							mirrorLayer);

					InteractableLayerTypes layerType = LevelEntityFactory.getInteractableLayerType(origLayer);

					if (layerType.equals(InteractableLayerTypes.DX) || layerType.equals(InteractableLayerTypes.DY)) {
						handler = new MovingPlatformManipulationHandler(collisionArray, tileCoords,
								layerType.equals(InteractableLayerTypes.DX),
								layerType.equals(InteractableLayerTypes.DY));
					} else if (layerType.equals(InteractableLayerTypes.FADABLE)) {
						float opacity = origLayer.getOpacity();
						handler = new FadableLayerManipulationHandler(collisionArray, tileCoords,
								(opacity >= WorldConstants.GLOBAL_SOLID_OPACITY_THRESHOLD));
					}

					if (handler == null) {
						throw new GdxRuntimeException("Trying to create handler for invalid platform type.");
					} else {
						platformLayers.put(origLayer, handler);
						platformLayers.put(mirrorLayer, handler);
					}
				}
			}
		}

		retVal = new TerrainCollisionMap(egLayer, platformLayers, collisionArray);

		return retVal;
	}

	/**
	 * <p>
	 * Creates and populates an array with the tile coordinates of every
	 * non-null cell in the given layer and mirrorLayer.
	 * </p>
	 * <p>
	 * Useful for platform layers which need to be manipulated.
	 * </p>
	 * 
	 * @param layer
	 *        The layer from which to find cells.
	 * @param mirrorLayer
	 *        The layer, a mirror of <em>layer</em> from which to find cells.
	 * @return A populated array of every non-null cell in the layers.
	 */
	public static ArrayList<TerrainMapTileCoordinate> generatePlatformTileCoordinateArray(TiledMapTileLayer layer,
			TiledMapTileLayer mirrorLayer) {
		ArrayList<TerrainMapTileCoordinate> retVal = new ArrayList<TerrainCollisionMap.TerrainMapTileCoordinate>();
		int width = layer.getWidth() + mirrorLayer.getWidth();

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < layer.getWidth(); x++) {
				if (layer.getCell(x, y) != null) {
					retVal.add(new TerrainMapTileCoordinate(width, x, y));
				}
			}

			for (int x = 0; x < mirrorLayer.getWidth(); x++) {
				if (mirrorLayer.getCell(x, y) != null) {
					retVal.add(new TerrainMapTileCoordinate(width, x + layer.getWidth(), y, true));
				}
			}
		}

		// Gdx.app.debug("GEN_PLATFORM_TILE_ARRAY",
		// "Generated " + retVal.size() + " tile coordinates for layer " +
		// layer.getName() + " and mirror "
		// + mirrorLayer.getName());

		return retVal;
	}

	/**
	 * <p>
	 * A super simple integer 2D vector to hold tile coords internally.
	 * </p>
	 * 
	 * @author Ashley Davis (SgtCoDFish)
	 */
	public static class TerrainMapTileCoordinate {
		public int		width		= -1;
		public int		x			= -1;
		public int		y			= -1;
		public boolean	isMirror	= false;

		public TerrainMapTileCoordinate(int width) {
			this(width, -1, -1, false);
		}

		public TerrainMapTileCoordinate(int width, int x, int y) {
			this(width, x, y, false);
		}

		public TerrainMapTileCoordinate(int width, int x, int y, boolean isMirror) {
			set(x, y);
			this.width = width;
			this.isMirror = isMirror;
		}

		public void set(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int to1DOffset() {
			return width * y + x;
		}
	}

	public static abstract class PlatformManipulationHandler {
		protected ArrayList<Integer>				map			= null;
		public ArrayList<TerrainMapTileCoordinate>	tileCoords	= null;

		public PlatformManipulationHandler(ArrayList<Integer> map, ArrayList<TerrainMapTileCoordinate> tileCoords) {
			this.map = map;
			this.tileCoords = tileCoords;
		}

		public void update(int degree) {
			begin();

			for (TerrainMapTileCoordinate tc : tileCoords) {
				manipulate(tc, degree);
			}

			end();
		}

		public void begin() {

		}

		public void end() {

		}

		protected abstract void manipulate(TerrainMapTileCoordinate tileCoord, int degree);
	}

	public static class MovingPlatformManipulationHandler extends PlatformManipulationHandler {
		protected boolean	moveInX	= false;
		protected boolean	moveInY	= false;

		public MovingPlatformManipulationHandler(ArrayList<Integer> map,
				ArrayList<TerrainMapTileCoordinate> tileCoords, boolean moveInX, boolean moveInY) {
			super(map, tileCoords);
			this.moveInX = moveInX;
			this.moveInY = moveInY;

			if (!moveInX && !moveInY) {
				// if we're not moving in either direction, what's the point?
				throw new GdxRuntimeException("MovingPlatformManipulationHandler created with no movement direction.");
			}
		}

		@Override
		protected void manipulate(TerrainMapTileCoordinate tileCoord, int degree) {
			/*
			 * Note this is not very optimised as a move by 1 simplifies to
			 * changing the beginning and end of a platform (but only if we can
			 * guarantee it's a joined platform).
			 * 
			 * Still, the game isn't exactly intensive and this optimisation is
			 * probably premature until problems present themselves.
			 */

			map.set(tileCoord.to1DOffset(), map.get(tileCoord.to1DOffset()) - 1);

			tileCoord.x += (moveInX ? degree : 0);
			tileCoord.y -= (moveInY ? degree * (tileCoord.isMirror ? -1 : 1) : 0);

			map.set(tileCoord.to1DOffset(), map.get(tileCoord.to1DOffset()) + 1);
		}
	}

	public static class FadableLayerManipulationHandler extends PlatformManipulationHandler {
		public boolean	isOpaque	= false;

		public FadableLayerManipulationHandler(ArrayList<Integer> map, ArrayList<TerrainMapTileCoordinate> tileCoords,
				boolean startsOpaque) {
			super(map, tileCoords);
			this.isOpaque = startsOpaque;
		}

		@Override
		protected void manipulate(TerrainMapTileCoordinate tileCoord, int degree) {
			int newVal = map.get(tileCoord.to1DOffset()) + degree * (isOpaque ? -1 : 1);
			map.set(tileCoord.to1DOffset(), newVal);
		}

		@Override
		public void end() {
			isOpaque = !isOpaque;
		}

	}
}
