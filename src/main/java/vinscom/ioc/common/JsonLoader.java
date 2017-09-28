package vinscom.ioc.common;

import java.util.List;

import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonLoader {

  public static List<String> layers;

  static {
    layers = Util.getSystemLayers();
  }
  
  public static JsonObject load(String pComponentPath, String pFile) {

    return layers
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
