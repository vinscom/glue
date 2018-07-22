package in.erail.glue.common;

import in.erail.glue.PropertiesRepository;
import java.util.List;

import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonLoader {

  public static List<String> layers;
  private static final Pattern COMPONENT_FOLDER_REGEX = Pattern.compile("^(?<folder>.*)/.*$");

  static {
    layers = PropertiesRepository.layers;
  }
  
  public static JsonObject load(String pComponentPath, String pFile) {

    Matcher m = COMPONENT_FOLDER_REGEX.matcher(pComponentPath);

    String componentFolder = "/";

    if (m.find()) {
      componentFolder = m.group("folder") + "/";
    }

    if (Util.isOSWindows()) {
      componentFolder = componentFolder.replace("/", "\\");
    }

    final String cf = componentFolder;

    
    return layers
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
            .map((p) -> {
              String result = "";
              try {
                result = new String(Files.readAllBytes(p));
              } catch (IOException ex) {
              }
              return result;
            })
            .map((s) -> {
              return new JsonObject(s);
            })
            .collect(JsonObject::new, JsonLoader::accumulator, JsonLoader::combine);
  }

  public static JsonObject accumulator(JsonObject acc, JsonObject json) {
    return acc.mergeIn(json, true);
  }

  public static JsonObject combine(JsonObject json1, JsonObject json2) {
    return json1.mergeIn(json2, true);
  }
}
