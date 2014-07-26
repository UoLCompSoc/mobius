package com.sgtcodfish.mobiusListing.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sgtcodfish.mobiusListing.MobiusListingGame;
import com.sgtcodfish.mobiusListing.levels.LevelEntityFactory;

public class DesktopLauncher {
	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		config.resizable = false;
		config.title = "Möbius";
		config.addIcon("icons/icon128.png", FileType.Internal);
		config.addIcon("icons/icon32.png", FileType.Internal);
		config.addIcon("icons/icon16.png", FileType.Internal);

		boolean debug = false;

		for (String s : args) {
			if ("--debug".equals(s)) {
				debug = true;
			} else if ("--check-levels".equals(s)) {
				LevelEntityFactory.VERBOSE_LOAD = true;
			}
		}

		new LwjglApplication(new MobiusListingGame(debug), config);
	}
}
