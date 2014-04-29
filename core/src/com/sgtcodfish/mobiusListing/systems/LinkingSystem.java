package com.sgtcodfish.mobiusListing.systems;

import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.sgtcodfish.mobiusListing.components.Linked;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class LinkingSystem extends EntityProcessingSystem {
	private ComponentMapper<Linked>	linkedComponentMapper	= null;

	@SuppressWarnings("unchecked")
	public LinkingSystem() {
		this(Filter.allComponents(Linked.class));
	}

	protected LinkingSystem(Filter filter) {
		super(filter);
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
}
