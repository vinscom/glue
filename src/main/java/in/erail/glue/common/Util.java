package in.erail.glue.common;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Parameter;
import java.util.Collection;
import in.erail.glue.enumeration.PropertyValueModifier;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Util {

  private static final Logger logger = LogManager.getLogger(Util.class.getCanonicalName());
  private static final MetricRegistry metricRegistry;

  static {
    String metricRegistryName = System.getenv(Constant.EnvVar.METRIC_REGISTRY_NAME);

    if (Strings.isNullOrEmpty(metricRegistryName)) {
      metricRegistryName = System.getProperty(Constant.EnvVar.Java.METRIC_REGISTRY_NAME);
    }

    if (metricRegistryName == null) {
      metricRegistryName = "vertx-registry";
    }

    metricRegistry = SharedMetricRegistries.getOrCreate(metricRegistryName);
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

    logger.debug(() -> "Trying to find Method:" + pMethodName + " in class:" + pClass.getCanonicalName());

    Method[] methods = pClass.getMethods();

    for (Method method : methods) {
      if (method.getName().equals(pMethodName)) {
        logger.debug(() -> "Found Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
        return method;
      }
    }

    logger.debug(() -> "Not Found Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
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

  @SuppressWarnings("rawtypes")
  public static Object createInstance(String pClass) {

    Object inst = null;

    try {
      Class clzz = Class.forName(pClass);
      inst = clzz.newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
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
    return metricRegistry;
  }

  public static List<String> getSystemLayers() {

    String layer = System.getenv(Constant.EnvVar.LAYERS);

    if (Strings.isNullOrEmpty(layer)) {
      layer = System.getProperty(Constant.EnvVar.Java.LAYERS);
    }

    if (layer == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(layer.split(Constant.EnvVar.SEPERATOR));
  }

  public static ValueWithModifier getLastValueWithModifier(Collection<ValueWithModifier> pList) {

    if (pList.isEmpty()) {
      return new ValueWithModifier(null, PropertyValueModifier.NONE);
    }

    List<ValueWithModifier> v = (List<ValueWithModifier>) pList;
    return v.get(v.size() - 1);
  }

  public static String getLastValue(ListMultimap<String, ValueWithModifier> pMap, String pPropertyName) {
    return getLastValueWithModifier(pMap.get(pPropertyName)).getValue();
  }

  public static String getLastValue(ListMultimap<String, ValueWithModifier> pMap, String pPropertyName, String pDefault) {
    String result = getLastValue(pMap, pPropertyName);
    if (result == null) {
      return pDefault;
    }
    return result;
  }

  public static String unzip(String pZipFilePath) {
    return unzip(pZipFilePath, null);
  }

  public static String unzip(String pZipFilePath, String pOnlyExtractPath) {

    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(pZipFilePath))) {
      //Create Temp Folder
      Path configLayer = Files.createTempDirectory("layer");

      ZipEntry entry = zipIn.getNextEntry();
      // iterates over entries in the zip file
      while (entry != null) {
        String filePath = configLayer + File.separator + entry.getName();
        if (entry.isDirectory()) {
          // if the entry is a directory, make the directory
          File dir = new File(filePath);
          dir.mkdir();
        } else {
          // if the entry is a file, extracts it
          Files.copy(zipIn, Paths.get(filePath));
        }
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }

      return configLayer.toString();
    } catch (IOException ex) {
      logger.error(ex);
    }

    return null;
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
      logger.error(ex);
    }
    return new String[0];
  }

}
