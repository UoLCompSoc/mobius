package com.sgtcodfish.mobiusListing;

import com.artemis.Entity;
import com.artemis.managers.GroupManager;

/**
 * <p>
 * {@link GroupManager} that is aware of links between entities and the need to
 * destroy a child if a parents is destroyed.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusGroupManager extends GroupManager {
	// private ComponentMapper<Linked> linkedComponentMapper = null;

	public MobiusGroupManager() {
		super();
	}

	@Override
	public void initialize() {
		// linkedComponentMapper = world.getMapper(Linked.class);
	}

	@Override
	public void deleted(Entity e) {
		super.deleted(e);
	}

}
