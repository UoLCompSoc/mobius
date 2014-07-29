package com.sgtcodfish.mobiusListing;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.sgtcodfish.mobiusListing.levels.LevelEntityFactory;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TerrainCollisionMap implements Poolable {
	private TiledMapTileLayer				layer				= null;
	private ArrayList<TiledMapTileLayer>	platformLayers		= null;
	private ArrayList<Boolean>				baseCollisionMap	= null;

	protected TerrainCollisionMap(TiledMapTileLayer layer, ArrayList<TiledMapTileLayer> platformLayers,
			ArrayList<Boolean> baseCollisionMap) {
		this.layer = layer;
		this.platformLayers = platformLayers;
		this.baseCollisionMap = baseCollisionMap;
	}

	/**
	 * <p>
	 * Adds the given layer's platform to the actual collision map. It might not
	 * be immediately collidable if the layer has some property (e.g.
	 * translucency) which makes it non-collidable.
	 * </p>
	 * 
	 * @param layer
	 *        The layer to add. Will be stored until this instance is
	 *        {@link TerrainCollisionMap#reset()}.
	 */
	public void initializePlatform(TiledMapTileLayer layer) {

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
			return mapRetrieve(tx, ty);
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
			return mapRetrieve(tx, ty);
		}
	}

	protected boolean mapRetrieve(int tileX, int tileY) {
		return baseCollisionMap.get((int) (tileY * (actualWidthInTiles()) + tileX)).booleanValue();
	}

	public int worldToTileCoordinatesX(float x) {
		int tx = (int) (x / layer.getTileWidth());

		if (tx < 0 || tx >= actualWidthInTiles()) {
			tx = tx % actualWidthInTiles();
		}

		return tx;
	}

	public int worldToTileCoordinatesY(float y) {
		int ty = (int) (y / layer.getTileHeight());

		if (ty < 0 || ty >= layer.getHeight()) {
			ty = -1;
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

	@Override
	public void reset() {
		layer = null;
		platformLayers.clear();
		baseCollisionMap.clear();
	}

	/**
	 * <p>
	 * Scans every cell in the layer and indiscriminately marks it as solid in a
	 * collision map, and returns this map.
	 * </p>
	 * 
	 * @param layer
	 *        The layer to use to generate the map.
	 * @param mirrorLayer
	 *        The mirror layer corresponding to the layer.
	 * @return A boolean array where map[layer.getWidth() * y + x] is a cell,
	 *         and true means "solid".
	 */
	public static List<Boolean> generateBaseCollisionArray(TiledMapTileLayer layer, TiledMapTileLayer mirrorLayer) {
		ArrayList<Boolean> collisionMap = new ArrayList<Boolean>();
		collisionMap.ensureCapacity((layer.getWidth() + mirrorLayer.getWidth()) * layer.getHeight());

		for (int y = 0; y < layer.getHeight(); y++) {
			for (int x = 0; x < (layer.getWidth() + mirrorLayer.getWidth()); x++) {
				if (x < layer.getWidth()) {
					collisionMap.add((layer.getCell(x, y) != null));
				} else {
					collisionMap.add((mirrorLayer.getCell(x - layer.getWidth(), y) != null));
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
	public static TerrainCollisionMap generateCollisionMap(TiledMap map) {
		TiledMap invertedMap = LevelEntityFactory.generateInvertedMap(map);
		TerrainCollisionMap retVal = null;

		MapLayers layers = map.getLayers();
		MapLayers mirrorLayers = invertedMap.getLayers();

		TiledMapTileLayer egLayer = (TiledMapTileLayer) layers.get(0);
		int arraySize = egLayer.getWidth() * 2 * egLayer.getHeight();

		List<Boolean> baseCollisionArray = new ArrayList<Boolean>(arraySize);

		for (int i = 0; i < layers.getCount(); i++) {
			TiledMapTileLayer origLayer = (TiledMapTileLayer) layers.get(i);
			TiledMapTileLayer mirrorLayer = (TiledMapTileLayer) mirrorLayers.get(i);

			if (LevelEntityFactory.isSolidLayer(origLayer) && LevelEntityFactory.isSolidLayer(mirrorLayer)) {
				List<Boolean> solidArray = generateBaseCollisionArray(origLayer, mirrorLayer);

				// accumulate all solid layers into base array.
				if (baseCollisionArray.isEmpty()) {
					baseCollisionArray = solidArray;
				} else {
					for (int j = 0; j < arraySize; j++) {
						baseCollisionArray.set(j, baseCollisionArray.get(i) || solidArray.get(i));
					}
				}
			} else if (LevelEntityFactory.isInteractableLayer(origLayer)
					|| LevelEntityFactory.isInteractableLayer(mirrorLayer)) {

			}
		}

		return retVal;
	}
}
