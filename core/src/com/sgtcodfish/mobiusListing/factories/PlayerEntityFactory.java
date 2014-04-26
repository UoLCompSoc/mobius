package com.sgtcodfish.mobiusListing.factories;

import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Renderable;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.player.PlayerAnimationState;
import com.sgtcodfish.mobiusListing.player.PlayerRenderHandler;

/**
 * <p>
 * Creates Player {@link Entity}s and handles related resources. To make a
 * player entity with a different sprite sheet, make a new PlayerEntityFactory
 * for each sheet.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerEntityFactory {
	private World										world			= null;
	private HashMap<PlayerAnimationState, Animation>	animationMap	= null;

	public PlayerEntityFactory(World world) {
		this(world, loadDefaultAnimationMap());
	}

	public PlayerEntityFactory(World world, HashMap<PlayerAnimationState, Animation> animationMap) {
		this.world = world;
		this.animationMap = animationMap;
	}

	public Entity createEntity(float x, float y) {
		Entity e = world.createEntity();

		Position p = world.createComponent(Position.class);
		p.position.x = x;
		p.position.y = y;

		e.addComponent(p);

		e.addComponent(world.createComponent(Velocity.class));

		e.addComponent(world.createComponent(PlayerState.class));

		Renderable d = world.createComponent(Renderable.class);
		d.renderHandler = new PlayerRenderHandler(animationMap);
		e.addComponent(d);

		return e;
	}

	private static HashMap<PlayerAnimationState, Animation> loadDefaultAnimationMap() {
		final float DEFAULT_FRAME_DURATION = 0.1f;

		final float STANDING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float RUNNING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float JUMPING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float USING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float MANIPULATING_FRAME_DURATION = DEFAULT_FRAME_DURATION;

		Animation standing = null, running = null, jumping = null, using = null, manipulating = null;

		Texture defaultPlayerTexture = new Texture("player/sprites.png");
		defaultPlayerTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		// TODO: More graceful loading than hard coding what regions to use
		TextureRegion[][] regions = TextureRegion.split(defaultPlayerTexture, 32, 32);
		standing = new Animation(STANDING_FRAME_DURATION, regions[0][0], regions[0][1], regions[0][2], regions[0][3],
				regions[0][4], regions[0][5], regions[0][6], regions[0][7]);
		standing.setPlayMode(PlayMode.LOOP);

		running = new Animation(RUNNING_FRAME_DURATION, regions[1][0], regions[1][1]);
		running.setPlayMode(PlayMode.LOOP);

		jumping = new Animation(JUMPING_FRAME_DURATION, regions[2][0], regions[2][1]);
		jumping.setPlayMode(PlayMode.LOOP);

		using = new Animation(USING_FRAME_DURATION, regions[3][0], regions[3][1], regions[3][2], regions[3][3]);
		using.setPlayMode(PlayMode.LOOP);

		manipulating = new Animation(MANIPULATING_FRAME_DURATION, regions[4][0], regions[4][1], regions[4][2],
				regions[4][3]);
		manipulating.setPlayMode(PlayMode.LOOP);

		return PlayerAnimationState.makeAnimationMapFromAnimations(standing, running, jumping, using, manipulating);
	}
}
