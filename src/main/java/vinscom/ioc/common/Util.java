package vinscom.ioc.common;

import java.lang.annotation.Annotation;
import vinscom.ioc.enumeration.MethodArgumentType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {

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

    Method[] methods = pClass.getMethods();

    for (Method method : methods) {
      if (method.getName().equals(pMethodName)) {
        return method;
      }
    }

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
    Class params = pMethod.getParameterTypes()[0];

    if (Map.class.isAssignableFrom(params)) {
      return MethodArgumentType.MAP;
    } else if (List.class.isAssignableFrom(params)) {
      return MethodArgumentType.LIST;
    } else if (String.class.isAssignableFrom(params)) {
      return MethodArgumentType.STRING;
    } else if (params.isArray()) {
      return MethodArgumentType.ARRAY;
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

}
