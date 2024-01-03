package io.github.moehreag.legacylwjgl3.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

	public static Path getIconTheme(String icon){

		String themeName;

		ProcessBuilder builder = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", "cursor-theme");

		try {
			Process p = builder.start();
			themeName = IOUtils.toString(p.getInputStream()).split("'")[1];
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			themeName = "default";
		}

		return findInThemes(themeName, icon);
	}

	private static Path findInThemes(String themeName, String icon){

		Path themePath = getThemePath(themeName);
		Path iconPath = themePath.resolve(icon);
		if (Files.exists(iconPath)){
			return iconPath;
		}

		Path themeIndex = themePath.resolve("index.theme");
		if (Files.exists(themeIndex)){

			try {
				List<String> lines = Files.readAllLines(themeIndex, StandardCharsets.UTF_8);

				boolean iconThemeFound = false;
				for (String s : lines){
					if ("[Icon Theme]".equals(s)){
						iconThemeFound = true;
					}

					if (iconThemeFound && !s.startsWith("#")){
						String[] parts = s.split("=", 2);
						if ("Inherits".equals(parts[0])){
							return findInThemes(parts[1], icon);
						}
					}
				}
			} catch (IOException ignored) {
			}

		}
		return null;
	}

	private static Path getThemePath(String name){
		for (Path p : getIconThemeLocations()){
			Path theme = p.resolve(name);
			if (Files.exists(theme)){
				return theme;
			}
		}
		if ("default".equals(name)){
			return Paths.get("usr", "share", "icons", "default");
		}
		return getThemePath("default");
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
