package in.erail.glue.common;

import java.util.List;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileLoader {

  public static List<String> layers;

  static {
    layers = Util.getSystemLayers();
  }

  public static File load(String pComponentPath, String pFile) {

    List<Path> filePaths = layers
            .stream()
            .map((p) -> {
              return p + pComponentPath.substring(0, pComponentPath.lastIndexOf("/")) + "/" + pFile;
            })
            .map((p) -> {
              return Paths.get(p);
            })
            .filter((p) -> {
              return Files.exists(p);
            })
            .collect(Collectors.toList());

    if (filePaths.size() > 0) {
      return filePaths.get(filePaths.size() - 1).toFile();
    }

    return null;
  }

}
