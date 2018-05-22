package com.rocasolida.scrap.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

public class DriverFinder {

	public static String DRIVERS_BINARIES = "./drivers/binaries";

	public static String findChromeDriver(String os) {
		return findDriver("chromedriver", os);
	}

	public static String findFirefoxDriver(String os) {
		if (os.toLowerCase().contains("windows")) {
			return findDriver("marionette", os);
		} else if (os.toLowerCase().contains("mac")) {
			return findDriver("geckodriver", "osx");
		} else {
			return findDriver("geckodriver", os);
		}
	}

	private static String findDriver(final String name, String os) {
		Path start = Paths.get(DRIVERS_BINARIES);
		int maxDepth = 10;
		try (Stream<Path> stream = Files.find(start, maxDepth, (path, attr) -> String.valueOf(path).contains(name) && String.valueOf(path).contains(os.toLowerCase()))) {
			Iterator<Path> it = stream.iterator();
			while (it.hasNext()) {
				Path path = it.next();
				if (Files.isRegularFile(path)) {
					return path.toString();
				}
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}