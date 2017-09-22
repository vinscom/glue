package vinscom.ioc.common;

import vinscom.ioc.enumeration.MethodArgumentType;
import java.lang.reflect.Method;

public class PropertyInfo {
  
  private Object instance;
  private Method method;
  private String value;
  private MethodArgumentType methodArgumentType;

  public Object getInstance() {
    return instance;
  }

  public void setInstance(Object instance) {
    this.instance = instance;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public MethodArgumentType getMethodArgumentType() {
    return methodArgumentType;
  }

  public void setMethodArgumentType(MethodArgumentType methodArgumentType) {
    this.methodArgumentType = methodArgumentType;
  }
  
}
