package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class Linked implements Component {
	public interface LinkPerformer {
		public void perform(Entity other);
	}

	public Entity			linkedEntity	= null;
	public LinkPerformer	performer		= null;

	public Linked() {
	}

	@Override
	public void reset() {
		linkedEntity = null;
		performer = null;
	}

	public static void makePositionLink(World world, Entity parent, Entity child, float yFlip) {
		Linked link = world.createComponent(Linked.class);
		link.linkedEntity = child;
		link.performer = link.new PositionLinkPerformer(world, yFlip);

		parent.addComponent(link);
	}

	/**
	 * Does nothing
	 * 
	 * @author Ashley Davis (SgtCoDFish)
	 */
	public static class PassLink implements LinkPerformer {
		@Override
		public void perform(Entity other) {
		}
	}

	public class PositionLinkPerformer implements LinkPerformer {
		private ComponentMapper<Position>	positionMapper	= null;
		private float						yFlip			= 0.0f;

		public PositionLinkPerformer(World world, float yFlip) {
			this.positionMapper = world.getMapper(Position.class);
			this.yFlip = yFlip;
		}

		@Override
		public void perform(Entity parent) {
			Position thisPos = positionMapper.get(parent);
			if (linkedEntity == null || thisPos == null)
				return;
			Position otherPos = positionMapper.get(linkedEntity);
			if (otherPos == null)
				return;

			otherPos.position.y = yFlip - thisPos.position.y;
		}
	}
}
