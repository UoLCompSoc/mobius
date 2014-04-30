package com.sgtcodfish.mobiusListing;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.managers.GroupManager;
import com.sgtcodfish.mobiusListing.components.Linked;

/**
 * <p>
 * {@link GroupManager} that is aware of links between entities and the need to
 * destroy a child if a parents is destroyed.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusGroupManager extends GroupManager {
	private ComponentMapper<Linked>	linkedComponentMapper	= null;

	public MobiusGroupManager() {
		super();
	}

	@Override
	public void initialize() {
		linkedComponentMapper = world.getMapper(Linked.class);
	}

	@Override
	public void deleted(Entity e) {
		Linked link = linkedComponentMapper.get(e);

		if (link != null) {
			link.child.deleteFromWorld();
		}

		super.deleted(e);
	}

}
