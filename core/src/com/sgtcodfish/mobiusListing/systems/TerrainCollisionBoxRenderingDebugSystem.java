package com.sgtcodfish.mobiusListing.systems;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.sgtcodfish.mobiusListing.TerrainCollisionMap;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class TerrainCollisionBoxRenderingDebugSystem extends VoidEntitySystem {
	private ShapeRenderer			renderer				= null;
	private Camera					camera					= null;

	private TerrainCollisionSystem	terrainCollisionSystem	= null;
	private TerrainCollisionMap		map						= null;

	public TerrainCollisionBoxRenderingDebugSystem(Camera camera, TerrainCollisionSystem terrainCollisionSystem) {
		super();

		this.renderer = new ShapeRenderer();
		this.terrainCollisionSystem = terrainCollisionSystem;
		this.camera = camera;
	}

	@Override
	public void begin() {
		this.map = terrainCollisionSystem.getCollisionMap();

		renderer.begin(ShapeType.Line);
		renderer.setProjectionMatrix(camera.combined);
		renderer.setColor(Color.YELLOW);

		super.begin();
	}

	@Override
	protected void processSystem() {
		for (int y = 0; y < map.actualHeightInTiles(); y++) {
			for (int x = 0; x < map.actualWidthInTiles(); x++) {
				int solidity = map.mapRetrieve(x, y);

				if (solidity >= 1) {
					if (solidity >= 2) {
						renderer.setColor(Color.RED);
					} else {
						renderer.setColor(Color.YELLOW);
					}

					renderer.rect(map.tileToWorldCoordinatesX(x), map.tileToWorldCoordinatesY(y), 32, 32);
				}
			}
		}
	}

	@Override
	public void end() {
		renderer.end();
		super.end();
	}
}
