package com.rocasolida.scrap.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

public class DriverFinder {

  public static Optional<String> findChromeDriver() {
    return findDriver("chromedriver");
  }

  public static Optional<String> findFirefoxDriver() {
    return findDriver("geckodriver");
  }

  private static Optional<String> findDriver(final String name) {
    Path start = Paths.get("./drivers/binaries");
    int maxDepth = 10;

    try (Stream<Path> stream = Files.find(start, maxDepth, (path, attr) ->
      String.valueOf(path).contains(name))) {
      return stream.findFirst().map(path -> path.toString());
    } catch (final IOException e) {
    		throw new RuntimeException(e);
	}
  }

}