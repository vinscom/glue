package vinscom.ioc.common;

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
import vinscom.ioc.enumeration.PropertyValueModifier;

public class Util {

  private static Logger logger = LogManager.getLogger(Util.class.getCanonicalName());

  public static String buildSetPropertyName(String pProperty) {
    return "set" + pProperty.substring(0, 1).toUpperCase() + pProperty.substring(1, pProperty.length());
  }

  public static String buildGetPropertyName(String pProperty, boolean pIsBoolean) {
    if(pIsBoolean){
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

  public static Class getMethodFirstArgumentClass(Method pMethod){
    Parameter[] param = pMethod.getParameters();
    if(param.length == 0){
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

  public static ValueWithModifier getLastValueWithModifier(Collection<ValueWithModifier> pList) {
    
    if(pList.isEmpty()){
      return new ValueWithModifier(null, PropertyValueModifier.NONE);
    }
    
    List<ValueWithModifier> v = (List)pList;
    return v.get(v.size() - 1);
  }
  
  public static String getLastValue(ListMultimap<String,ValueWithModifier> pMap, String pPropertyName){
    return getLastValueWithModifier(pMap.get(pPropertyName)).getValue();
  }
  
  public static String getLastValue(ListMultimap<String,ValueWithModifier> pMap, String pPropertyName, String pDefault){
    String result = getLastValue(pMap, pPropertyName);
    if(result == null){
      return pDefault;
    }
    return result;
  }
}
