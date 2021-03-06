package com.sgtcodfish.mobiusListing.player;

import java.util.ArrayList;
import java.util.HashMap;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sgtcodfish.mobiusListing.Item;
import com.sgtcodfish.mobiusListing.components.FocusTaker;
import com.sgtcodfish.mobiusListing.components.Inventory;
import com.sgtcodfish.mobiusListing.components.PlayerInputListener;
import com.sgtcodfish.mobiusListing.components.PlayerSprite;
import com.sgtcodfish.mobiusListing.components.PlayerState;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Solid;
import com.sgtcodfish.mobiusListing.components.Velocity;

/**
 * <p>
 * Creates Player {@link Entity}s and handles related resources. To make a
 * player entity with a different sprite sheet, make a new PlayerEntityFactory
 * for each sheet.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class PlayerEntityFactory implements Disposable {
	public static final String							DEFAULT_SPRITE_LOCATION			= "player/sprites.png";
	public static final int								DEFAULT_PLAYER_TEXTURE_WIDTH	= 32;
	public static final int								DEFAULT_PLAYER_TEXTURE_HEIGHT	= 32;

	private World										world							= null;
	private HashMap<HumanoidAnimationState, Animation>	animationMap					= null;

	public Texture										defaultPlayerTexture			= null;

	public PlayerEntityFactory(World world) {
		this(world, null);
	}

	public PlayerEntityFactory(World world, HashMap<HumanoidAnimationState, Animation> animationMap) {
		this.world = world;
		if (animationMap != null) {
			this.animationMap = animationMap;
		} else {
			FileHandle handle = Gdx.files.internal(DEFAULT_SPRITE_LOCATION);

			if (!handle.exists()) {
				throw new GdxRuntimeException("Could not find " + DEFAULT_SPRITE_LOCATION);
			}

			defaultPlayerTexture = new Texture(handle);

			if (defaultPlayerTexture == null) {
				throw new GdxRuntimeException("Could not load default player texture!");
			}

			this.animationMap = loadDefaultAnimationMap(defaultPlayerTexture);
		}
	}

	/**
	 * <p>
	 * Creates and returns an entity at the given (x, y) position. The entity
	 * will not take focus.
	 * </p>
	 * 
	 * @param x
	 *        The x coordinate the entity will be created at.
	 * @param y
	 *        The y coordinate the entity will be created at.
	 * @return The created Entity.
	 */
	public Entity createEntity(float x, float y) {
		return createEntity(x, y, false);
	}

	/**
	 * <p>
	 * Creates and returns an entity at the given (x, y) position.
	 * </p>
	 * <p>
	 * If takesFocus is true, the camera will follow this player. If more than
	 * one Entity takes focus, behaviour is undefined.
	 * </p>
	 * 
	 * @param x
	 *        The x coordinate the entity will be created at.
	 * @param y
	 *        The y coordinate the entity will be created at.
	 * @param takesFocus
	 *        Whether the camera follows this Entity.
	 * @return The created Entity.
	 */
	public Entity createEntity(float x, float y, boolean takesFocus) {
		if (animationMap == null) {
			throw new GdxRuntimeException("Call to createEntity with invalid animation map.");
		}

		Entity e = world.createEntity();

		Position p = world.createComponent(Position.class);
		p.position.x = x;
		p.position.y = y;
		e.addComponent(p);

		Velocity v = world.createComponent(Velocity.class);
		e.addComponent(v);

		PlayerState ps = world.createComponent(PlayerState.class);
		e.addComponent(ps);

		PlayerSprite d = world.createComponent(PlayerSprite.class);
		d.animationMap = animationMap;
		d.spriteWidth = DEFAULT_PLAYER_TEXTURE_WIDTH;
		d.spriteHeight = DEFAULT_PLAYER_TEXTURE_HEIGHT;
		d.mirrored = false;
		e.addComponent(d);

		PlayerInputListener pil = world.createComponent(PlayerInputListener.class);
		e.addComponent(pil);

		Solid s = world.createComponent(Solid.class);
		s.boundingBox = new Rectangle(0.0f, 0.0f, DEFAULT_PLAYER_TEXTURE_WIDTH, DEFAULT_PLAYER_TEXTURE_HEIGHT);
		e.addComponent(s);

		Inventory i = world.createComponent(Inventory.class);
		i.inventoryList = new ArrayList<Item>();
		e.addComponent(i);

		if (takesFocus) {
			FocusTaker ft = world.createComponent(FocusTaker.class);
			e.addComponent(ft);
		}

		world.addEntity(e);
		return e;
	}

	private HashMap<HumanoidAnimationState, Animation> loadDefaultAnimationMap(Texture texture) {
		final float DEFAULT_FRAME_DURATION = 0.1f;

		final float STANDING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float RUNNING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float JUMPING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float USING_FRAME_DURATION = DEFAULT_FRAME_DURATION;
		final float MANIPULATING_FRAME_DURATION = DEFAULT_FRAME_DURATION;

		Animation standing = null, running = null, jumping = null, using = null, manipulating = null;

		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		// TODO: More graceful loading than hard coding what regions to use
		TextureRegion[][] regions = TextureRegion.split(texture, DEFAULT_PLAYER_TEXTURE_WIDTH,
				DEFAULT_PLAYER_TEXTURE_HEIGHT);
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

		return HumanoidAnimationState.makeAnimationMapFromAnimations(standing, running, jumping, using, manipulating);
	}

	@Override
	public void dispose() {
		if (defaultPlayerTexture != null) {
			defaultPlayerTexture.dispose();
			defaultPlayerTexture = null;
		}
	}
}
