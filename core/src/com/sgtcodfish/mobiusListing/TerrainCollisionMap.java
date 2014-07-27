package com.sgtcodfish.mobiusListing;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TerrainCollisionMap {
	private TiledMapTileLayer	layer			= null;
	private Array<Boolean>		collisionMap	= null;

	public TerrainCollisionMap(TiledMapTileLayer layer, Array<Boolean> collisionMap) {
		this.layer = layer;
		this.collisionMap = collisionMap;
	}

	/**
	 * 
	 * @param p
	 * @param v
	 * @return true if worldY in world coordinates will cause the entity to
	 *         collide with this map.
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

	public boolean willCollideX(Vector2 p, Vector2 v) {
		// TODO: fix magic numbers
		int ty = worldToTileCoordinatesY(p.y);

		int tx = worldToTileCoordinatesX(p.x + v.x);

		if (tx == -1 || ty == -1) {
			return false;
		} else {
			return mapRetrieve(tx, ty);
		}
	}

	protected boolean mapRetrieve(int tileX, int tileY) {
		return collisionMap.get((int) (tileY * (actualWidthInTiles()) + tileX)).booleanValue();
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
}
