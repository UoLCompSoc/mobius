package com.sgtcodfish.mobiusListing.systems;

import com.artemis.Entity;
import com.artemis.Filter;
import com.artemis.systems.EntityProcessingSystem;
import com.sgtcodfish.mobiusListing.MobiusListingGame;
import com.sgtcodfish.mobiusListing.components.PlayerInputListener;

/**
 * @author Ashley Davis (SgtCoDFish)
 */
public class LevelAdvanceSystem extends EntityProcessingSystem {
	MobiusListingGame	game	= null;

	@SuppressWarnings("unchecked")
	public LevelAdvanceSystem(MobiusListingGame game) {
		this(Filter.allComponents(PlayerInputListener.class), game);
	}

	protected LevelAdvanceSystem(Filter filter, MobiusListingGame game) {
		super(filter);
		this.game = game;
	}

	@Override
	protected void process(Entity e) {
		game.nextLevel();
		this.setPassive(true);
	}

}
