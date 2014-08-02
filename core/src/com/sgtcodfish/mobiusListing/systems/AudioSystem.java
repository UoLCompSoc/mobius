package com.sgtcodfish.mobiusListing.systems;

import java.util.ArrayList;

import com.artemis.systems.VoidEntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/**
 * <p>
 * Handles playing all audio.
 * </p>
 * 
 * @author Ashley Davis (SgtCoDFish)
 */
public class AudioSystem extends VoidEntitySystem implements Disposable {
	public static final String		AUDIO_PREFIX	= "audio/";

	private Music					backgroundMusic	= null;
	private Sound					bloop			= null;

	private ArrayList<MobiusSounds>	soundQueue		= null;

	public enum MobiusSounds {
		BLOOP;
	}

	public AudioSystem() {
	}

	@Override
	public void initialize() {
		soundQueue = new ArrayList<AudioSystem.MobiusSounds>();

		backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(AUDIO_PREFIX + "soundtrack.mp3"));
		backgroundMusic.setLooping(true);
		backgroundMusic.setVolume(0.5f);

		bloop = Gdx.audio.newSound(Gdx.files.internal(AUDIO_PREFIX + "bloop.wav"));
	}

	@Override
	protected void processSystem() {
		if (soundQueue.size() > 0) {
			Gdx.app.debug("AUDIO_SYSTEM_PROCESS", "Processing " + soundQueue.size() + " queued sounds.");
			for (MobiusSounds sound : soundQueue) {
				switch (sound) {
				case BLOOP: {
					bloop.play();
					break;
				}

				default: {
					Gdx.app.debug("AUDIO_SYSTEM_PROCESS", "Invalid sound in sound queue, size is " + soundQueue.size());
				}
				}
			}

			soundQueue.clear();
		}
	}

	public void start() {
		backgroundMusic.play();
	}

	public void stop() {
		backgroundMusic.pause();
	}

	public void enqueue(MobiusSounds sound) {
		soundQueue.add(sound);
	}

	public void dispose() {
		backgroundMusic.dispose();
		bloop.dispose();
	}
}
