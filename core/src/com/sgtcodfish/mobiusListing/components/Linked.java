package com.sgtcodfish.mobiusListing.components;

import com.artemis.Component;
import com.artemis.ComponentMapper;
import com.artemis.Entity;
import com.artemis.World;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class Linked implements Component {
	/**
	 * <p>
	 * Holds a performer which can be used to apply linking attributes to linked
	 * child entities.
	 * </p>
	 * 
	 * @author Ashley Davis (SgtCoDFish)
	 */
	public interface LinkPerformer {
		/**
		 * <p>
		 * Performs some function that links the child entity to the parent. An
		 * example might be moving the child to the same position as the parent,
		 * or to a position with some function applied to it.
		 * </p>
		 * 
		 * @param parent
		 *        The parent entity of linkedEntity.
		 */
		public void perform(Entity parent);
	}

	public Entity			child		= null;
	public LinkPerformer	performer	= null;

	public Linked() {
	}

	@Override
	public void reset() {
		child = null;
		performer = null;
	}

	/**
	 * <p>
	 * Creates a new {@link PositionOpacityLinkPerformer} between the parent and
	 * child entities.
	 * </p>
	 * 
	 * @param world
	 *        The world in which the entities reside.
	 * @param parent
	 *        The parent entity.
	 * @param child
	 *        The child entity; see {@link PositionOpacityLinkPerformer} for
	 *        changes which performer.perform(parent) will carry out.
	 * @param xOffset
	 *        How far in the x direction the child should be offset.
	 * @param yFlip
	 *        The way to "flip" the position of the child entity when mirrored.
	 */
	public static void makePositionOpacityLink(World world, Entity parent, Entity child, float xOffset, float yFlip) {
		Linked link = world.createComponent(Linked.class);
		link.child = child;
		link.performer = link.new PositionOpacityLinkPerformer(world, xOffset, yFlip);

		parent.addComponent(link);
	}

	/**
	 * <p>
	 * Does nothing to the child entity. Useful for linking a child's lifecycle
	 * to its parent's (i.e. the child is destroyed when the parent is).
	 * </p>
	 * 
	 * @author Ashley Davis (SgtCoDFish)
	 */
	public static class PassLink implements LinkPerformer {
		@Override
		public void perform(Entity parent) {
		}
	}

	/**
	 * <p>
	 * Contains a {@link LinkPerformer} which maps a child's position to the
	 * parent's mirrored position, and maps a child's {@link Opacity} to match
	 * its parent's opacity.
	 * </p>
	 * 
	 * @author Ashley Davis (SgtCoDFish)
	 */
	public class PositionOpacityLinkPerformer implements LinkPerformer {
		private ComponentMapper<Position>	positionMapper	= null;
		private ComponentMapper<Opacity>	opacityMapper	= null;

		private float						xOffset			= 0.0f;
		private float						yFlip			= 0.0f;

		public PositionOpacityLinkPerformer(World world, float xOffset, float yFlip) {
			this.positionMapper = world.getMapper(Position.class);
			this.opacityMapper = world.getMapper(Opacity.class);

			this.xOffset = xOffset;
			this.yFlip = yFlip;
		}

		@Override
		public void perform(Entity parent) {
			if (parent == null || child == null) {
				return;
			}

			Position thisPos = positionMapper.get(parent);
			Position otherPos = positionMapper.get(child);

			if (thisPos != null && otherPos != null) {
				otherPos.position.y = yFlip - thisPos.position.y;
				otherPos.position.x = xOffset + thisPos.position.x;
			}

			Opacity thisOpacity = opacityMapper.get(parent);
			Opacity otherOpacity = opacityMapper.get(child);

			if (thisOpacity != null && otherOpacity != null) {
				otherOpacity.opacity = thisOpacity.opacity;
			}
		}
	}
}
