package com.sgtcodfish.mobiusListing.systems;

import java.util.ArrayList;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.sgtcodfish.mobiusListing.WorldConstants;
import com.sgtcodfish.mobiusListing.components.Collectable;
import com.sgtcodfish.mobiusListing.components.Interactable;
import com.sgtcodfish.mobiusListing.components.Inventory;
import com.sgtcodfish.mobiusListing.components.Opacity;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.player.PlayerConstants;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class SolidProcessingSystem extends EntityProcessingSystem {
	private ComponentMapper<Position>		positionMapper		= null;
	private ComponentMapper<Velocity>		velocityMapper		= null;

	private ComponentMapper<Solid>			solidMapper			= null;

	private ComponentMapper<Inventory>		inventoryMapper		= null;
	private ComponentMapper<Collectable>	collectableMapper	= null;

	private ComponentMapper<Interactable>	interactableMapper	= null;

	private ComponentMapper<Opacity>		opacityMapper		= null;

	private ArrayList<Entity>				movingSolids		= null;
	private ArrayList<Entity>				staticSolids		= null;

	private LinkingSystem					linkingSystem		= null;

	@SuppressWarnings("unchecked")
	public SolidProcessingSystem(LinkingSystem linkingSystem) {
		this(Filter.allComponents(Position.class, Solid.class), linkingSystem);
	}

	protected SolidProcessingSystem(Filter filter, LinkingSystem linkingSystem) {
		super(filter);
		this.linkingSystem = linkingSystem;
	}

	@Override
	public void initialize() {
		positionMapper = world.getMapper(Position.class);
		velocityMapper = world.getMapper(Velocity.class);

		solidMapper = world.getMapper(Solid.class);

		inventoryMapper = world.getMapper(Inventory.class);
		collectableMapper = world.getMapper(Collectable.class);

		interactableMapper = world.getMapper(Interactable.class);

		opacityMapper = world.getMapper(Opacity.class);

		movingSolids = new ArrayList<Entity>(world.getEntityManager().getActiveEntityCount());
		staticSolids = new ArrayList<Entity>(world.getEntityManager().getActiveEntityCount());
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
			Opacity opacity = opacityMapper.get(e);
			if (opacity != null) {
				if (opacity.opacity < WorldConstants.GLOBAL_SOLID_OPACITY_THRESHOLD) {
					return;
				}
			}

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

				Solid other = solidMapper.get(otherEntity);

				if (s.boundingBox.overlaps(other.boundingBox)) {
					handleCollision(e, movingSolids.get(j));
				}
			}

			for (Entity staticSolid : staticSolids) {
				Solid other = solidMapper.get(staticSolid);

				if (s.boundingBox.overlaps(other.boundingBox)) {
					handleCollision(e, staticSolid);
				}
			}
		}
	}

	protected void handleCollision(Entity one, Entity other) {
		if (inventoryMapper.get(one) != null && collectableMapper.get(other) != null) {
			collect(one, other);
			return;
		} else if (inventoryMapper.get(other) != null && collectableMapper.get(one) != null) {
			collect(other, one);
			return;
		} else if (solidMapper.get(other).weight < solidMapper.get(one).weight && interactableMapper.get(other) != null
				&& PlayerConstants.interacting) {
			// TODO: Fix dirty hack with weights
			if (inventoryMapper.get(one) != null && inventoryMapper.get(one).inventoryList.size() > 0) {
				world.getSystem(LevelAdvanceSystem.class).setPassive(false);
			}
		} else if ((solidMapper.get(one).weight < solidMapper.get(other).weight && interactableMapper.get(one) != null && PlayerConstants.interacting)) {
			// TODO: Fix dirty hack with weights
			if (inventoryMapper.get(other) != null && inventoryMapper.get(other).inventoryList.size() > 0) {
				world.getSystem(LevelAdvanceSystem.class).setPassive(false);
			}
		}
	}

	protected void collect(Entity one, Entity item) {
		Inventory inventory = inventoryMapper.get(one);
		Collectable collectable = collectableMapper.get(item);

		inventory.inventoryList.add(collectable.item);
		Gdx.app.debug("PICKUP", collectable.item.name + " was picked up!");

		linkingSystem.scheduleForRemoval(item);
	}
}
