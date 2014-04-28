package com.sgtcodfish.mobiusListing.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.sgtcodfish.mobiusListing.MobiusListingGame;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1024;
		config.height = 768;
		config.resizable = false;
		config.addIcon("icons/icon128.png", FileType.Internal);
		config.addIcon("icons/icon32.png", FileType.Internal);
		config.addIcon("icons/icon16.png", FileType.Internal);

		new LwjglApplication(new MobiusListingGame(true), config);
	}
}
