package vinscom.ioc.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import vinscom.ioc.enumeration.MethodArgumentType;

public class Util {

  private static Logger logger = LogManager.getLogger(Util.class.getCanonicalName());
  
  public static String sanatizePropertyName(String pProperty) {
    if (pProperty.endsWith("^")) {
      return pProperty.substring(0, pProperty.length() - 1);
    }
    return pProperty;
  }

  public static String buildSetPropertyName(String pProperty) {
    String propName = sanatizePropertyName(pProperty);
    return "set" + propName.substring(0, 1).toUpperCase() + propName.substring(1, propName.length());
  }

  @SuppressWarnings("rawtypes")
  public static Method getMethod(Class pClass, String pMethodName) {

    logger.debug(()-> "Trying to find Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
    
    Method[] methods = pClass.getMethods();

    for (Method method : methods) {
      if (method.getName().equals(pMethodName)) {
        logger.debug(()-> "Found Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
        return method;
      }
    }

    logger.debug(()-> "Not Found Method:" + pMethodName + " in class:" + pClass.getCanonicalName());
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
  public static MethodArgumentType findMethodArgumentType(Method pMethod) {

    if(pMethod == null) {
      Thread.currentThread().dumpStack();
      return MethodArgumentType.NONE;
    }
    
    logger.debug(()-> "findMethodArgumentType:" + pMethod.getName() + ",Args:" + pMethod.getParameterCount());
    
    Class params = pMethod.getParameterTypes()[0];

    if (Map.class.isAssignableFrom(params)) {
      return MethodArgumentType.MAP;
    } else if (List.class.isAssignableFrom(params)) {
      return MethodArgumentType.LIST;
    } else if (String.class.isAssignableFrom(params)) {
      return MethodArgumentType.STRING;
    } else if (params.isPrimitive() && boolean.class.equals(params)) {
      return MethodArgumentType.BOOLEAN;
    } else if (params.isArray()) {
      return MethodArgumentType.ARRAY;
    } else if (JsonObject.class.isAssignableFrom(params)) {
      return MethodArgumentType.JSON;
    }

    return MethodArgumentType.COMPONENT;
  }

  public static boolean doesPropertyReferToComponent(String pProperty) {
    return pProperty.endsWith("^");
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

    Map<String, String> result = new HashMap<>();

    for (String s : pValue.split(",")) {
      String[] keyvalue = s.split("=");
      result.put(keyvalue[0], keyvalue[1]);
    }

    return result;
  }

  public static List<String> getSystemLayers() {

    String layer = System.getProperty(Constant.SystemProperties.LAYERS);

    if (layer == null) {
      return Collections.emptyList();
    }

    return Arrays.asList(layer.split(Constant.SystemProperties.SEPERATOR));
  }
}
