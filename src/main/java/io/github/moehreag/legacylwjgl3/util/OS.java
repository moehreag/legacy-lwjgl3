package io.github.moehreag.legacylwjgl3.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import io.github.moehreag.legacylwjgl3.LegacyLWJGL3;

public enum OS {
	LINUX(),
	OSX() {
		@Override
		protected String[] getProcessArguments(URI uri) {
			return new String[]{"open", uri.toString()};
		}
	},
	SOLARIS(),
	UNKNOWN(),
	WINDOWS() {
		@Override
		protected String[] getProcessArguments(URI uri) {
			return new String[]{"rundll32", "url.dll,FileProtocolHandler", uri.toString()};
		}
	};

	public void openUri(URI uri) {
		try {
			Process process = Runtime.getRuntime().exec(this.getProcessArguments(uri));
			process.getInputStream().close();
			process.getErrorStream().close();
			process.getOutputStream().close();
		} catch (IOException e) {
			LegacyLWJGL3.LOGGER.error("Couldn't open location '{}'", uri, e);
		}
	}

	protected String[] getProcessArguments(URI uri) {
		String string = uri.toString();
		if ("file".equals(uri.getScheme())) {
			string = string.replace("file:", "file://");
		}

		return new String[]{"xdg-open", string};
	}

	public void open(String uri) {
		try {
			this.openUri(new URI(uri));
		} catch (IllegalArgumentException | URISyntaxException var3) {
			LegacyLWJGL3.LOGGER.error("Couldn't open uri '{}'", uri, var3);
		}
	}

	private static final OS CURRENT = getPlatform();

	public static OS current() {
		return CURRENT;
	}

	private static OS getPlatform() {
		String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		if (string.contains("win")) {
			return OS.WINDOWS;
		} else if (string.contains("mac")) {
			return OS.OSX;
		} else if (string.contains("solaris") || string.contains("sunos")) {
			return OS.SOLARIS;
		} else if (string.contains("linux") || string.contains("unix")) {
			return OS.LINUX;
		}
		return OS.UNKNOWN;
	}
}
