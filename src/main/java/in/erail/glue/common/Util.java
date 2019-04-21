package in.erail.glue.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;

import in.erail.glue.ConfigSerializationFactory;
import in.erail.glue.enumeration.PropertyValueModifier;
import in.erail.glue.factory.DefaultConfigSerializationFactory;

public class Util {

  private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

  private static final Logger LOGGER = LogManager.getLogger(Util.class.getCanonicalName());
  private static final MetricRegistry METRIC_REGISTRY;
  private static final Properties GLUE_CONFIG = new Properties();

  static {
    String metricRegistryName = getEnvironmentValue(Constant.SystemProperties.METRIC_REGISTRY_NAME);

    if (metricRegistryName == null) {
      metricRegistryName = "vertx-registry";
    }

    METRIC_REGISTRY = SharedMetricRegistries.getOrCreate(metricRegistryName);

    Path glueConfig = Paths.get(getEnvironmentValue(Constant.SystemProperties.GLUE_CONFIG, "glue.config"));

    if (Files.exists(glueConfig)) {
      try {
        GLUE_CONFIG.load(Files.newInputStream(glueConfig));
      } catch (IOException ex) {
        LOGGER.error("Not able to load glue.config from " + glueConfig.toString(), ex);
      }
    } else {
      LOGGER.info(glueConfig.toAbsolutePath().toString() + " not found. User Dir:" + System.getProperty("user.dir"));
    }

  }

  public static boolean isOSWindows() {
    return IS_WINDOWS;
  }

  public static String buildSetPropertyName(String pProperty) {
    return "set" + pProperty.substring(0, 1).toUpperCase() + pProperty.substring(1, pProperty.length());
  }

  public static String buildGetPropertyName(String pProperty, boolean pIsBoolean) {
    if (pIsBoolean) {
      return "is" + pProperty.substring(0, 1).toUpperCase() + pProperty.substring(1, pProperty.length());
    }
    return "get" + pProperty.substring(0, 1).toUpperCase() + pProperty.substring(1, pProperty.length());
  }

  @SuppressWarnings("rawtypes")
  public static Method getMethod(Class pClass, String pMethodName) {

    LOGGER.debug(() -> "Trying to find Method:" + pMethodName + " in class:" + pClass.getCanonicalName());

    Method[] methods = pClass.getMethods();

    for (Method method : methods) {
      if (method.getName().equals(pMethodName)) {
        LOGGER.debug(() -> "Found Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
        return method;
      }
    }

    LOGGER.debug(() -> "Not Found Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
    return null;
  }

  @SuppressWarnings("rawtypes")
  public static <T extends Annotation> Method getMethodWithAnnotation(Class pClass, Class<T> pAnnotation) {

    Method[] methods = pClass.getMethods();

    for (Method method : methods) {
      Object anno = method.getAnnotation(pAnnotation);
      if (anno != null) {
        return method;
      }
    }

    return null;
  }

  @SuppressWarnings("rawtypes")
  public static Class getMethodFirstArgumentClass(Method pMethod) {
    Parameter[] param = pMethod.getParameters();
    if (param.length == 0) {
      throw new RuntimeException("No arguments in method:" + pMethod.getName());
    }
    return param[0].getType();
  }

  public static Object createInstance(String pClass) {

    Object inst = null;

    try {
      Class<?> clzz = Class.forName(pClass);
      inst = clzz.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException
            | InstantiationException
            | IllegalAccessException
            | NoSuchMethodException
            | SecurityException
            | IllegalArgumentException
            | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
    return inst;
  }

  public static Object createInstance(Class<?> pClass) {

    Object inst = null;

    try {
      inst = pClass.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException
            | SecurityException
            | InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }

    return inst;
  }

  public static Map<String, String> getMapFromValue(String pValue) {

    if (Strings.isNullOrEmpty(pValue)) {
      return Collections.emptyMap();
    }

    Map<String, String> result = new HashMap<>();

    for (String s : convertCSVIntoArray(pValue)) {
      String[] keyvalue = s.split("=");
      result.put(keyvalue[0], keyvalue[1]);
    }

    return result;
  }

  public static MetricRegistry getMetricRegistry() {
    return METRIC_REGISTRY;
  }

  /**
   * Get property value from Java System Properties, if not found then get it
   * form environment variable.
   *
   * @param pName Name of system property
   * @param pDefault Default value
   * @return Return environment value
   */
  public static String getEnvironmentValue(String pName, String pDefault) {
    String value = System.getProperty(pName);

    if (Strings.isNullOrEmpty(value)) {
      value = System.getenv(pName.toUpperCase().replace(".", "_"));
    }

    if (Strings.isNullOrEmpty(value)) {
      value = GLUE_CONFIG.getProperty(pName);
    }

    if (Strings.isNullOrEmpty(value)) {
      return pDefault;
    }

    return value;
  }

  public static String getEnvironmentValue(String pName) {
    return getEnvironmentValue(pName, null);
  }

  public static ConfigSerializationFactory getConfigSerializationFactory() {

    String factory = getEnvironmentValue(Constant.SystemProperties.GLUE_SERIALIZATION_FACTORY);

    if (Strings.isNullOrEmpty(factory)) {
      return new DefaultConfigSerializationFactory();
    }

    return (ConfigSerializationFactory) createInstance(factory);
  }

  public static List<String> getSystemLayers() {

    String layer = getEnvironmentValue(Constant.SystemProperties.LAYERS);

    if (layer == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(layer.split(Constant.SystemProperties.SEPERATOR));
  }

  public static ValueWithModifier getLastValueWithModifier(Collection<ValueWithModifier> pList) {

    if (pList.isEmpty()) {
      return new ValueWithModifier(null, PropertyValueModifier.NONE);
    }

    List<ValueWithModifier> v = (List<ValueWithModifier>) pList;
    return v.get(v.size() - 1);
  }

  public static String getLastValue(ListMultimap<String, ValueWithModifier> pMap, String pPropertyName) {

    if (pMap == null) {
      LOGGER.error("Value map expected for property:" + pPropertyName);
      return null;
    }

    return getLastValueWithModifier(pMap.get(pPropertyName)).getValue();
  }

  public static String getLastValue(ListMultimap<String, ValueWithModifier> pMap, String pPropertyName, String pDefault) {
    String result = getLastValue(pMap, pPropertyName);
    if (result == null) {
      return pDefault;
    }
    return result;
  }

  public static String unzip(Path pZipFilePath, Path pDestinationPath) throws IOException {
    try ( ZipInputStream zipIn = new ZipInputStream(new FileInputStream(pZipFilePath.toFile()))) {

      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null) {
        String uri = pDestinationPath + File.separator + entry.getName();
        Path path = Paths.get(uri);
        if (entry.isDirectory()) {
          // if the entry is a directory, make the directory
          if (!Files.exists(path)) {
            Files.createDirectories(path);
          }
        } else {

          Path parentPath = path.getParent();

          if (!Files.exists(parentPath)) {
            Files.createDirectories(parentPath);
          }

          // if the entry is a file, extracts it
          Files.copy(zipIn, path);
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }

      return pDestinationPath.toString();
    }
  }

  public static String convertDotToCamelCase(String pValue) {
    StringBuilder sb = new StringBuilder(pValue);
    int dotIndx = 0;
    while (dotIndx != -1) {
      dotIndx = sb.indexOf(".");
      if (dotIndx == -1) {
        break;
      }
      sb.deleteCharAt(dotIndx);
      sb.setCharAt(dotIndx, Character.toUpperCase(sb.charAt(dotIndx)));
    }
    return sb.toString();
  }

  public static String[] convertCSVIntoArray(String pValue) {
    try {
      CSVParser parser = CSVParser.parse(pValue, CSVFormat.DEFAULT);
      Iterator<CSVRecord> itrRecord = parser.iterator();
      if (itrRecord.hasNext()) {  //Only one record is expected
        CSVRecord record = itrRecord.next();
        String[] result = new String[record.size()];
        for (int i = 0; i < result.length; i++) {
          result[i] = record.get(i);
        }
        return result;
      }
    } catch (IOException ex) {
      LOGGER.error(ex);
    }
    return new String[0];
  }

}
