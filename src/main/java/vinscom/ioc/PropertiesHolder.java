package vinscom.ioc;

import vinscom.ioc.common.Tuple;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import rx.Completable;
import rx.Observable;

public class PropertiesHolder {

  protected Logger logger = LogManager.getLogger(PropertiesHolder.class.getCanonicalName());
  private static final String PROPERTY_EXTENSION = ".properties";
  private static final int PROPERTY_EXTENSION_LENGTH = PROPERTY_EXTENSION.length();

  private final Map<String, Properties> propertiesCache;
  
  private boolean initialized = false;
  private List<String> layers;

  public PropertiesHolder() {
    this.propertiesCache = new HashMap<>();
  }
  
  protected Completable init() {

    Observable<Tuple<Path, Path>> paths = Observable
            .from(getLayers())
            .map(path -> Paths.get(path))
            .flatMap(this::findAllPropertiesFile);

    Observable<Properties> cachedProperties = loadCachedProperties(paths);
    Observable<Properties> newProperties = loadUncachedProperties(paths);

    return cachedProperties
            .zipWith(newProperties, this::mergeUncachedIntoCachedProperties)
            .doOnCompleted(() -> setInitialized(true))
            .toCompletable();

  }

  private Properties mergeUncachedIntoCachedProperties(Properties pCached, Properties pUncached) {
    pCached.putAll(pUncached);
    return pCached;
  }

  private Observable<Properties> loadUncachedProperties(Observable<Tuple<Path, Path>> pPath) {

    return pPath
            .map((p) -> {
              Properties item = new Properties();
              logger.debug(()-> "Loading Property File:" + p.value2);
              try {
                item.load(Files.newBufferedReader(p.value2));
              } catch (IOException ex) {
                ex.printStackTrace();
              }
              return item;
            });

  }

  private Observable<Properties> loadCachedProperties(Observable<Tuple<Path, Path>> pPath) {

    return pPath
            .map((path) -> {
              String dir = path.value1.toString();
              String fullPath = path.value2.toString();

              //Key Without Property
              String key = fullPath.substring(dir.length());
              key = key.substring(0, key.length() - PROPERTY_EXTENSION_LENGTH);

              if (propertiesCache.containsKey(key)) {
                return propertiesCache.get(key);
              }

              Properties newProp = new Properties();
              propertiesCache.put(key, newProp);
              return newProp;
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

  protected Map<String, Properties> getPropertiesCache() {
    return propertiesCache;
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
