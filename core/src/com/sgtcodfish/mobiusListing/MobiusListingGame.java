package com.sgtcodfish.mobiusListing;

import com.artemis.Entity;
import com.artemis.World;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sgtcodfish.mobiusListing.components.Renderable;
import com.sgtcodfish.mobiusListing.components.Position;
import com.sgtcodfish.mobiusListing.components.Velocity;
import com.sgtcodfish.mobiusListing.systems.MovementSystem;
import com.sgtcodfish.mobiusListing.systems.SpriteRenderingSystem;

/**
 * Contains all the main logic; most should be in classes that extend
 * EntityProcessingSystem.
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class MobiusListingGame extends ApplicationAdapter {
	private SpriteBatch	batch					= null;
	private Camera		camera					= null;
	private Texture		img						= null;

	public World		world					= null;

	public Entity		exampleTextureEntity	= null;

	@Override
	public void create() {
		world = new World();

		batch = new SpriteBatch();
		// camera = new PerspectiveCamera(60.0f, 320.0f, 240.0f);
		camera = new OrthographicCamera(320.0f, 240.0f);
		img = new Texture("badlogic.jpg");

		world.setSystem(new MovementSystem());
		world.setSystem(new SpriteRenderingSystem(batch, camera));

		world.initialize();

		exampleTextureEntity = world.createEntity();
		exampleTextureEntity.addComponent(world.createComponent(Position.class));

		Velocity v = world.createComponent(Velocity.class);
		v.velocity.y = 1.0f;
		exampleTextureEntity.addComponent(v);

		Renderable d = world.createComponent(Renderable.class);
		d.texture = img;

		exampleTextureEntity.addComponent(d);

		exampleTextureEntity.addToWorld();
	}

	@Override
	public void render() {
		world.setDelta(Gdx.graphics.getDeltaTime());

		camera.update();

		Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		world.process();
	}
}
