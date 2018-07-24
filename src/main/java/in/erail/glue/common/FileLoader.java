package in.erail.glue.common;

import in.erail.glue.PropertiesRepository;
import java.util.List;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileLoader {

  public static List<String> layers;
  private static final Pattern COMPONENT_FOLDER_REGEX = Pattern.compile("^(?<folder>.*)/.*$");

  static {
    if (PropertiesRepository.layers == null) {
      PropertiesRepository.setLayers(Util.getSystemLayers());
    }
    layers = PropertiesRepository.layers;
  }

  public static File load(String pComponentPath, String pFile) {

    Matcher m = COMPONENT_FOLDER_REGEX.matcher(pComponentPath);

    String componentFolder = "/";

    if (m.find()) {
      componentFolder = m.group("folder") + "/";
    }

    if (Util.isOSWindows()) {
      componentFolder = componentFolder.replace("/", "\\");
    }

    final String cf = componentFolder;

    List<Path> filePaths = layers
            .stream()
            .map((p) -> {
              return p + cf + pFile;
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
