package in.erail.glue;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import io.reactivex.Observable;
import in.erail.glue.common.Tuple;
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

import in.erail.glue.common.Constant;
import in.erail.glue.common.Util;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.enumeration.PropertyValueModifier;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.subjects.UnicastSubject;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PropertiesRepository {

  protected Logger logger = LogManager.getLogger(PropertiesRepository.class.getCanonicalName());

  public static List<String> layers;
  private static final String PROPERTY_EXTENSION = ".properties";
  private static final int PROPERTY_EXTENSION_LENGTH = PROPERTY_EXTENSION.length();

  private Map<String, ListMultimap<String, ValueWithModifier>> mPropertiesRepository;
  private final AtomicLong mInstanceFactoryCounter;

  private boolean mInitialized = false;

  public PropertiesRepository() {
    this.mInstanceFactoryCounter = new AtomicLong();
    this.mPropertiesRepository = Collections.EMPTY_MAP;
  }

  public void init() {

    ConfigSerializationFactory factory = Util.getConfigSerializationFactory();

    mPropertiesRepository = factory
            .load()
            .switchIfEmpty(loadConfig())
            .flatMap((t) -> {
              setInitialized(true);
              return factory
                      .save(t)
                      .andThen(Single.just(t));
            })
            .blockingGet();
  }

  private Single<Map<String, ListMultimap<String, ValueWithModifier>>> loadConfig() {

    Map<String, ListMultimap<String, ValueWithModifier>> propertiesRepository = new HashMap<>();

    UnicastSubject<Map.Entry<String, ListMultimap<String, ValueWithModifier>>> instanceOfFactoryProperties
            = UnicastSubject.<Map.Entry<String, ListMultimap<String, ValueWithModifier>>>create();

    Observable<Tuple<Path, Path>> paths = Observable
            .fromIterable(getLayers())
            .map(path -> Paths.get(path))
            .flatMap(this::findAllPropertiesFile);

    Observable<ListMultimap<String, ValueWithModifier>> cachedProperties = loadCachedProperties(paths, propertiesRepository);
    Observable<Properties> newProperties = loadUncachedProperties(paths);

    return cachedProperties
            .zipWith(newProperties, this::mergeUncachedIntoCachedProperties)
            .flatMapCompletable((t) -> t)
            .andThen(Observable.fromIterable(propertiesRepository.entrySet()))
            .doOnNext((t) -> {
              String factoryPath = Util.getLastValue(t.getValue(), Constant.Component.INSTANCE_FACTORY);
              if (Strings.isNullOrEmpty(factoryPath)) {
                return;
              }
              Map.Entry<String, ListMultimap<String, ValueWithModifier>> newFactoryProperties
                      = createInstanceFactoryProperties(factoryPath, t.getKey(), mInstanceFactoryCounter);
              instanceOfFactoryProperties.onNext(newFactoryProperties);
              //Add new factory componet path.
              t.getValue()
                      .put(Constant.Component.INSTANCE_FACTORY,
                              new ValueWithModifier(newFactoryProperties.getKey(), PropertyValueModifier.NONE));
            })
            .doOnComplete(() -> instanceOfFactoryProperties.onComplete())
            .concatWith(instanceOfFactoryProperties.doOnNext((p) -> propertiesRepository.put(p.getKey(), p.getValue())))
            .doOnNext((t) -> updateBasedOnProperties(t.getKey(), t.getValue(), propertiesRepository))
            .doOnNext((t) -> updateBasedOnSpecialProperties(t.getKey(), t.getValue(), propertiesRepository))
            .ignoreElements()
            .toSingleDefault(propertiesRepository);
  }

  private Map.Entry<String, ListMultimap<String, ValueWithModifier>> createInstanceFactoryProperties(
          String pInstFactoryComponentPath,
          String pSourceComponentPath,
          AtomicLong pCounter) {

    String componentPath = pInstFactoryComponentPath + ":" + pCounter.incrementAndGet();
    ListMultimap<String, ValueWithModifier> properties = ArrayListMultimap.create();

    ValueWithModifier basedOn = new ValueWithModifier(pInstFactoryComponentPath, PropertyValueModifier.NONE);
    ValueWithModifier basedOnSpecialProperties = new ValueWithModifier(pSourceComponentPath, PropertyValueModifier.NONE);

    properties.put(Constant.Component.BASED_ON, basedOn);
    properties.put(Constant.Component.BASED_ON_SPECIAL_PROPERTIES, basedOnSpecialProperties);
    return new AbstractMap.SimpleEntry<>(componentPath, properties);
  }

  /**
   *
   * @param pComponentPath
   * @param pProperties
   */
  private void updateBasedOnSpecialProperties(
          String pComponentPath,
          ListMultimap<String, ValueWithModifier> pProperties,
          Map<String, ListMultimap<String, ValueWithModifier>> pPropertiesRepository) {

    String baseOnSpecialComponentPath = Util.getLastValue(pProperties, Constant.Component.BASED_ON_SPECIAL_PROPERTIES);

    if (Strings.isNullOrEmpty(baseOnSpecialComponentPath)) {
      return;
    }

    ListMultimap<String, ValueWithModifier> baseOnSpecialProperties = pPropertiesRepository.get(baseOnSpecialComponentPath);

    ListMultimap<String, ValueWithModifier> newProperties = ArrayListMultimap.create();

    baseOnSpecialProperties
            .entries()
            .stream()
            .filter((t) -> t.getKey().startsWith(Constant.Component.SPECIAL_PROPERTY))
            .filter((t) -> !Constant.Component.BASED_ON.equals(t.getKey()))
            .filter((t) -> !Constant.Component.SCOPE.equals(t.getKey()))
            .filter((t) -> !Constant.Component.INSTANCE_FACTORY.equals(t.getKey()))
            .filter((t) -> !Constant.Component.BASED_ON_SPECIAL_PROPERTIES.equals(t.getKey()))
            .forEachOrdered((e) -> {
              String key = e.getKey();
              if (Constant.Component.CLASS.equals(key)) {
                key = "baseClass";
              } else {
                key = key.substring(1);
              }
              String k = Util.convertDotToCamelCase(key);
              newProperties.put(k, e.getValue());
            });

    pProperties
            .entries()
            .stream()
            .filter((p) -> !Constant.Component.BASED_ON_SPECIAL_PROPERTIES.equals(p.getKey()))
            .forEachOrdered((e) -> {
              newProperties.put(e.getKey(), e.getValue());
            });

    pProperties.clear();
    pProperties.putAll(newProperties);
  }

  /**
   *
   * @param pComponentPath
   * @param pProperties
   */
  private void updateBasedOnProperties(
          String pComponentPath,
          ListMultimap<String, ValueWithModifier> pProperties,
          Map<String, ListMultimap<String, ValueWithModifier>> pPropertiesRepository) {

    String baseOnComponentPath = Util.getLastValue(pProperties, Constant.Component.BASED_ON);

    if (Strings.isNullOrEmpty(baseOnComponentPath)) {
      return;
    }

    ListMultimap<String, ValueWithModifier> baseOnProperties = pPropertiesRepository.get(baseOnComponentPath);

    if (Util.getLastValue(baseOnProperties, Constant.Component.BASED_ON) != null) {
      updateBasedOnProperties(baseOnComponentPath, baseOnProperties, pPropertiesRepository);
    }

    ListMultimap<String, ValueWithModifier> newProperties = ArrayListMultimap.create();

    baseOnProperties
            .entries()
            .stream()
            .forEachOrdered((e) -> {
              newProperties.put(e.getKey(), e.getValue());
            });

    pProperties
            .entries()
            .stream()
            .filter((p) -> !Constant.Component.BASED_ON.equals(p.getKey()))
            .forEachOrdered((e) -> {
              newProperties.put(e.getKey(), e.getValue());
            });

    pProperties.clear();
    pProperties.putAll(newProperties);
  }

  /**
   * Merge properties file loaded from system into one previously loaded.
   *
   * @param pCached Already loaded properties map
   * @param pUncached Newly loaded properties file
   * @return
   */
  private Completable mergeUncachedIntoCachedProperties(ListMultimap<String, ValueWithModifier> pCached, Properties pUncached) {

    Observable<Map.Entry<String, String>> property = Observable
            .fromIterable(pUncached.entrySet())
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
            })
            .ignoreElements();

  }

  /**
   * Remove modifier from value
   *
   * @param pEntry Value with modifier
   * @return Value without modifier
   */
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

  /**
   * Return value modifier
   *
   * @param pEntry Value with modifier
   * @return Modifier of value
   */
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

  /**
   * Load properties file from system.
   *
   * @param pPath Search path and full path of properties file
   * @return Loaded Properties object
   */
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

  /**
   * Return already loaded properties file map. If file is not loaded then empty map is returned
   *
   * @param pPath Search path and full path of properties file
   * @return Properties map
   */
  private Observable<ListMultimap<String, ValueWithModifier>> loadCachedProperties(
          Observable<Tuple<Path, Path>> pPath,
          Map<String, ListMultimap<String, ValueWithModifier>> pPropertiesRepository) {

    return pPath
            .map((path) -> {
              String dir = path.value1.toString();
              String fullPath = path.value2.toString();

              //Key Without Property
              String key = fullPath.substring(dir.length());
              key = key.substring(0, key.length() - PROPERTY_EXTENSION_LENGTH);

              if (pPropertiesRepository.containsKey(key)) {
                return pPropertiesRepository.get(key);
              }

              ListMultimap<String, ValueWithModifier> properties = ArrayListMultimap.create();
              properties.put(Constant.Component.COMPONENT_PATH_SPECIAL_PROPERTIES, new ValueWithModifier(key, PropertyValueModifier.NONE));
              pPropertiesRepository.put(key, properties);
              return properties;
            });

  }

  /**
   * Find all properties file under given path
   *
   * @param pPath Search path
   * @return Search Path and Full File Path
   */
  private Observable<Tuple<Path, Path>> findAllPropertiesFile(Path pPath) {

    return Observable
            .fromIterable((Iterable<Path>) () -> {
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
    return mPropertiesRepository;
  }

  public boolean isInitialized() {
    return mInitialized;
  }

  public void setInitialized(boolean initialized) {
    this.mInitialized = initialized;
  }

  public static List<String> getLayers() {
    return layers;
  }

  public static void setLayers(List<String> pLayer) {
    layers = pLayer
            .stream()
            .map((path) -> {
              if (path.endsWith(".jar") || path.endsWith(".zip")) {
                try {
                  String dir = Util.unzip(Paths.get(path), Files.createTempDirectory("layer"));
                  Object[] configFolder = Files
                          .walk(Paths.get(dir))
                          .filter((p) -> p.endsWith("config"))
                          .limit(1)
                          .toArray();
                  if (configFolder.length > 0) {
                    return ((Path) configFolder[0]).toString();
                  }
                  return null;
                } catch (IOException ex) {
                  LogManager.getLogger(PropertiesRepository.class.getCanonicalName()).error(path);
                }

              }
              return path;
            })
            .filter((path) -> !Strings.isNullOrEmpty(path))
            .collect(Collectors.toList());
  }
}
