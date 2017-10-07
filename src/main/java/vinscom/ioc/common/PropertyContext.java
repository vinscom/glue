package vinscom.ioc.common;

import java.lang.reflect.Method;
import vinscom.ioc.ValueProxy;

public class PropertyContext {

  private String componentPath;
  private Object instance;
  private Method method;
  private ValueProxy value;

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

  public ValueProxy getValue() {
    return value;
  }

  public void setValue(ValueProxy pValue) {
    this.value = pValue;
  }

  public String getComponentPath() {
    return componentPath;
  }

  public void setComponentPath(String pComponentPath) {
    this.componentPath = pComponentPath;
  }
  
}
