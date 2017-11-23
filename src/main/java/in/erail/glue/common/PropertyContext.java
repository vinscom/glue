package in.erail.glue.common;

import com.google.common.base.MoreObjects;
import java.lang.reflect.Method;
import in.erail.glue.ValueProxy;

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

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper(this)
            .omitNullValues()
            .add("Instance", getInstance()!=null?getInstance().getClass().getCanonicalName():null)
            .add("Method", getMethod()!=null?getMethod().getName():null)
            .add("Value", getValue()!=null?getValue().toString():null)
            .add("ComponentPath", getComponentPath()!=null?getComponentPath():null)
            .toString();
  }
  
}
