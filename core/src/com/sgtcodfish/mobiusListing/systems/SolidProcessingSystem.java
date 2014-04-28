package com.sgtcodfish.mobiusListing.systems;

import java.util.ArrayList;
import java.util.HashMap;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.sgtcodfish.mobiusListing.components.Collectable;
import com.sgtcodfish.mobiusListing.components.Inventory;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;
import com.sgtcodfish.mobiusListing.components.Velocity;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class SolidProcessingSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper		= null;
	private ComponentMapper<Velocity>		velocityMapper		= null;

	private ComponentMapper<Solid>			solidMapper			= null;

	private ComponentMapper<Inventory>		inventoryMapper		= null;
	private ComponentMapper<Collectable>	collectableMapper	= null;

	private ArrayList<Entity>				movingSolids		= null;
	private ArrayList<Entity>				staticSolids		= null;

	private HashMap<Entity, Boolean>		scheduledForRemoval	= null;

	/* *************************************************************************
	 * WARNING: THE BOOLEAN IN THIS HASHMAP DOES NOTHING -ONLY THE PRESENCE OF A
	 * KEY MEANS ANYTHING, DON'T ASK ME WHY
	 * *************************************************************************
	 */

	@SuppressWarnings("unchecked")
	public SolidProcessingSystem() {
		this(Filter.allComponents(Position.class, Solid.class));
	}

	protected SolidProcessingSystem(Filter filter) {
		super(filter);
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);

		solidMapper = world.getMapper(Solid.class);

		inventoryMapper = world.getMapper(Inventory.class);
		collectableMapper = world.getMapper(Collectable.class);

		// TODO: Remove magic number
		movingSolids = new ArrayList<>(100);
		staticSolids = new ArrayList<>(100);

		scheduledForRemoval = new HashMap<>(100);
	}

	@Override
	protected void begin() {
		movingSolids.clear();
		staticSolids.clear();
	}

	@Override
	protected void process(Entity e) {
		Position position = positionMapper.get(e);
		Solid solid = solidMapper.get(e);

		solid.boundingBox.x = position.position.x;
		solid.boundingBox.y = position.position.y;

		if (velocityMapper.get(e) != null) {
			movingSolids.add(e);
		} else {
			staticSolids.add(e);
		}
	}

	@Override
	protected void end() {
		// compare moving against one another, compare each moving against all
		// static.
		for (int i = 0; i < movingSolids.size(); i++) {
			Entity e = movingSolids.get(i);
			Solid s = solidMapper.get(e);

			for (int j = i + 1; j < movingSolids.size(); j++) {
				Entity otherEntity = movingSolids.get(j);

				if (scheduledForRemoval.get(otherEntity) != null) {
					continue;
				}

				Solid other = solidMapper.get(otherEntity);

				if (s.boundingBox.overlaps(other.boundingBox)) {
					handleCollision(e, movingSolids.get(j));
				}
			}

			for (Entity staticSolid : staticSolids) {
				if (scheduledForRemoval.get(staticSolid) != null) {
					continue;
				}

				Solid other = solidMapper.get(staticSolid);

				if (s.boundingBox.overlaps(other.boundingBox)) {
					handleCollision(e, staticSolid);
				}
			}
		}

		for (Entity e : scheduledForRemoval.keySet()) {
			e.deleteFromWorld();
		}

		scheduledForRemoval.clear();
	}

	protected void handleCollision(Entity one, Entity other) {
		if (inventoryMapper.get(one) != null && collectableMapper.get(other) != null) {
			collect(one, other);
		} else if (inventoryMapper.get(other) != null && collectableMapper.get(one) != null) {
			collect(other, one);
		}
	}

	protected void collect(Entity one, Entity item) {
		Inventory inventory = inventoryMapper.get(one);
		Collectable collectable = collectableMapper.get(item);

		inventory.inventoryList.add(collectable.item);

		scheduledForRemoval.put(item, true);
	}
}
