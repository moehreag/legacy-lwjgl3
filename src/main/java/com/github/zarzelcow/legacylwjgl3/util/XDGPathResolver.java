package com.github.zarzelcow.legacylwjgl3.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;

public class XDGPathResolver {

	private static Path getHome(){
		String home = System.getenv().getOrDefault("$HOME", System.getProperty("user.home"));
		if (home == null || home.isEmpty()) {
			throw new IllegalStateException("could not resolve user home");
		}
		return Paths.get(home);
	}

	public static Path getUserDataLocation() {
		String xdgDataHome = System.getenv("$XDG_DATA_HOME");
		if (xdgDataHome == null || xdgDataHome.isEmpty()) {
			return getHome().resolve(".local/share/");
		}
		return Paths.get(xdgDataHome);
	}


	public static List<Path> getIconThemeLocations(){
		Path userShare = getUserDataLocation().resolve("icons");
		Path homeIcons = getHome().resolve(".icons");
		Path systemIcons = Paths.get("/usr/share/icons");
		return ImmutableList.of(userShare, homeIcons, systemIcons);
	}

	public static Path getIconTheme(){

		String themeName;

		ProcessBuilder builder = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "cursor-theme");

		try {
			Process p = builder.start();
			themeName = IOUtils.toString(p.getInputStream()).split("'")[1];
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			themeName = "default";
		}

		for (Path p : getIconThemeLocations()){
			Path theme = p.resolve(themeName);
			if (Files.exists(theme)){
				return theme;
			}
		}

		return null;
	}

	public static int getCursorSize(){
		int size;
		ProcessBuilder builder = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "cursor-size");

		try {
			Process p = builder.start();
			size = Integer.parseInt(IOUtils.toString(p.getInputStream()).split("\n")[0]);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			size = 24;
		}

		return size;
	}
}
