package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.artemis.Entity;
import com.artemis.World;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class ChildLinked implements Component {
	public Entity	parentEntity	= null;

	public ChildLinked() {
	}

	@Override
	public void reset() {
		parentEntity = null;
	}

	public static void makeChild(World world, Entity parent, Entity child) {
		ChildLinked childLink = world.createComponent(ChildLinked.class);
		childLink.parentEntity = parent;

		child.addComponent(childLink);
	}
}
