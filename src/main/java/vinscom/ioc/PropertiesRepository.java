package vinscom.ioc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import vinscom.ioc.common.Tuple;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Completable;
import rx.Observable;
import vinscom.ioc.common.Constant;
import vinscom.ioc.common.ValueWithModifier;
import vinscom.ioc.enumeration.PropertyValueModifier;

public class PropertiesRepository {

  protected Logger logger = LogManager.getLogger(PropertiesRepository.class.getCanonicalName());
  private static final String PROPERTY_EXTENSION = ".properties";
  private static final int PROPERTY_EXTENSION_LENGTH = PROPERTY_EXTENSION.length();

  private final Map<String, ListMultimap<String, ValueWithModifier>> propertiesRepository;

  private boolean initialized = false;
  private List<String> layers;

  public PropertiesRepository() {
    this.propertiesRepository = new HashMap<>();
  }

  protected Completable init() {

    Observable<Tuple<Path, Path>> paths = Observable
            .from(getLayers())
            .map(path -> Paths.get(path))
            .flatMap(this::findAllPropertiesFile);

    Observable<ListMultimap<String, ValueWithModifier>> cachedProperties = loadCachedProperties(paths);
    Observable<Properties> newProperties = loadUncachedProperties(paths);

    return cachedProperties
            .zipWith(newProperties, this::mergeUncachedIntoCachedProperties)
            .flatMap((t) -> t)
            .doOnCompleted(() -> setInitialized(true))
            .toCompletable();

  }

  private Observable<Boolean> mergeUncachedIntoCachedProperties(ListMultimap<String, ValueWithModifier> pCached, Properties pUncached) {

    Observable<Map.Entry<String, String>> property = Observable
            .from(pUncached.entrySet())
            .map(e -> (Map.Entry<String, String>) new AbstractMap.SimpleEntry<>(e.getKey().toString(), e.getValue().toString()));

    Observable<String> key = property.map(e -> e.getKey()).map(this::extractKey);
    Observable<PropertyValueModifier> modifier = property.map(e -> e.getKey()).map(this::extractPropertyValueModifier);
    Observable<ValueWithModifier> value = property
            .map(e -> e.getValue())
            .zipWith(modifier, (v, m) -> {
              return new ValueWithModifier(v, m);
            });

    return key
            .zipWith(value, (k, v) -> {
              return pCached.put(k, v);
            });

  }

  private String extractKey(String pEntry) {

    String modifier = pEntry.substring(pEntry.length() - 1);

    switch (modifier) {
      case Constant.Component.Modifier.PLUS:
      case Constant.Component.Modifier.MINU:
      case Constant.Component.Modifier.FROM:
        return pEntry.substring(0, pEntry.length() - 1);
      default:
        return pEntry;
    }

  }

  private PropertyValueModifier extractPropertyValueModifier(String pEntry) {

    String modifier = pEntry.substring(pEntry.length() - 1);

    switch (modifier) {
      case Constant.Component.Modifier.PLUS:
        return PropertyValueModifier.PLUS;
      case Constant.Component.Modifier.MINU:
        return PropertyValueModifier.MINUS;
      case Constant.Component.Modifier.FROM:
        return PropertyValueModifier.FROM;
      default:
        return PropertyValueModifier.NONE;
    }

  }

  private Observable<Properties> loadUncachedProperties(Observable<Tuple<Path, Path>> pPath) {

    return pPath
            .map((p) -> {
              Properties item = new Properties();
              logger.debug(() -> "Loading Property File:" + p.value2);
              try {
                item.load(Files.newBufferedReader(p.value2));
              } catch (IOException ex) {
                ex.printStackTrace();
              }
              return item;
            });

  }

  private Observable<ListMultimap<String, ValueWithModifier>> loadCachedProperties(Observable<Tuple<Path, Path>> pPath) {

    return pPath
            .map((path) -> {
              String dir = path.value1.toString();
              String fullPath = path.value2.toString();

              //Key Without Property
              String key = fullPath.substring(dir.length());
              key = key.substring(0, key.length() - PROPERTY_EXTENSION_LENGTH);

              if (propertiesRepository.containsKey(key)) {
                return propertiesRepository.get(key);
              }

              ListMultimap<String, ValueWithModifier> properties = ArrayListMultimap.create();
              propertiesRepository.put(key, properties);
              return properties;
            });

  }

  private Observable<Tuple<Path, Path>> findAllPropertiesFile(Path pPath) {

    return Observable
            .from((Iterable<Path>) () -> {
              try {
                return Files.walk(pPath).filter((t) -> {
                  return t.toString().endsWith(PROPERTY_EXTENSION);
                }).iterator();
              } catch (IOException ex) {
                return Collections.<Path>emptyIterator();
              }
            })
            .map((fullPath) -> new Tuple<>(pPath, fullPath));

  }

  protected Map<String, ListMultimap<String, ValueWithModifier>> getPropertiesCache() {
    return propertiesRepository;
  }

  public boolean isInitialized() {
    return initialized;
  }

  public void setInitialized(boolean initialized) {
    this.initialized = initialized;
  }

  public List<String> getLayers() {
    return layers;
  }

  public void setLayers(List<String> layers) {
    this.layers = layers;
  }
}
