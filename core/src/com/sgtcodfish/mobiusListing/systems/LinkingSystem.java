package com.sgtcodfish.mobiusListing.systems;

import java.util.ArrayList;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.badlogic.gdx.Gdx;
import com.sgtcodfish.mobiusListing.components.Linked;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class LinkingSystem extends EntityProcessingSystem {
	private ComponentMapper<Linked>	linkedComponentMapper	= null;

	private ArrayList<Entity>		scheduledForRemoval		= null;

	@SuppressWarnings("unchecked")
	public LinkingSystem() {
		this(Filter.allComponents(Linked.class));
	}

	protected LinkingSystem(Filter filter) {
		super(filter);

		scheduledForRemoval = new ArrayList<Entity>(10);
	}

	@Override
	public void initialize() {
		linkedComponentMapper = world.getMapper(Linked.class);
	}

	@Override
	protected void process(Entity e) {
		Linked link = linkedComponentMapper.get(e);

		link.performer.perform(e);
	}

	@Override
	protected void end() {
		int childCount = 0;

		for (int i = 0; i < scheduledForRemoval.size(); i++) {
			Entity root = scheduledForRemoval.get(i);

			while (linkedComponentMapper.get(root) != null) {
				root = linkedComponentMapper.get(root).child;

				if (!scheduledForRemoval.contains(root)) {
					scheduledForRemoval.add(root);
					childCount++;
				}
			}
		}

		if (scheduledForRemoval.size() > 0) {
			Gdx.app.debug("LINKING_SYSTEM", "Removing " + scheduledForRemoval.size() + " elements, of which "
					+ childCount + " were child entities.");
		}

		for (Entity e : scheduledForRemoval) {
			e.deleteFromWorld();
		}

		scheduledForRemoval.clear();

		super.end();
	}

	public void scheduleForRemoval(Entity e) {
		scheduledForRemoval.add(e);
	}
}
