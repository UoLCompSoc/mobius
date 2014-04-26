package com.sgtcodfish.mobiusListing;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sgtcodfish.mobiusListing.components.Drawable;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.systems.MovementSystem;
import com.sgtcodfish.mobiusListing.systems.RenderingSystem;

/**
 * Contains all the main logic; most should be in classes that extend
 * EntityProcessingSystem.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusListingGame extends ApplicationAdapter {
	private SpriteBatch			batch					= null;
	private PerspectiveCamera	camera					= null;
	private Texture				img						= null;

	public World				world					= null;

	public Entity				exampleTextureEntity	= null;

	public MobiusListingGame() {
		world = new World();
	}

	@Override
	public void create() {
		batch = new SpriteBatch();
		camera = new PerspectiveCamera(60.0f, 320.0f, 240.0f);
		img = new Texture("badlogic.jpg");

		world.setSystem(new MovementSystem());
		world.setSystem(new RenderingSystem(batch, camera));

		exampleTextureEntity = world.createEntity();
		exampleTextureEntity.addComponent(world.createComponent(Position.class));
		exampleTextureEntity.addComponent(world.createComponent(Velocity.class));
		exampleTextureEntity.getComponent(Position.class).position.x = 5.0f;

		Drawable d = world.createComponent(Drawable.class);
		d.texture = img;

		exampleTextureEntity.addComponent(d);

		exampleTextureEntity.addToWorld();

		world.initialize();
	}

	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());

		Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		world.process();

		batch.begin();
		batch.draw(img, 0, 0);
		batch.end();
	}
}
