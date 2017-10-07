package vinscom.ioc;

import io.vertx.core.json.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vinscom.ioc.common.JsonLoader;
import vinscom.ioc.common.Util;

public class ValueProxy {

  private static final Pattern DEFERRED_PROPERTY_VALUE_PATTER = Pattern.compile("(?<component>^[^.]+)($|(\\.(?<property>.*)$))");
  private Class targetClass;
  private String propertyValue;
  private String componentPath;

  private Object value;
  private String deferredComponentPath;
  private String deferredComponentProperty;
  private Object deferredComponent;
  private boolean deferredValue = false;
  private boolean stage1ProcessingRequired = true;
  private boolean stage2ProcessingRequired = true;

  public ValueProxy() {
  }

  public ValueProxy(Class pTargetClass, String pPropertyValue, String pComponentPath) {
    this.targetClass = pTargetClass;
    this.propertyValue = pPropertyValue;
    this.componentPath = pComponentPath;
  }

  public void processStage2() {

    if(isDeferredValue()){
      return;
    }
    
    if (getTargetClass().isArray()) {
      setValue(getPropertyValue().split(","));
    } else if (String.class.isAssignableFrom(getTargetClass())) {
      setValue(getPropertyValue());
    } else if (List.class.isAssignableFrom(getTargetClass())) {
      setValue(Arrays.asList(getPropertyValue().split(",")));
    } else if (Map.class.isAssignableFrom(getTargetClass())) {
      setValue(Util.getMapFromValue(getPropertyValue()));
    } else if (Enum.class.isAssignableFrom(getTargetClass())) {
      setValue(Enum.valueOf(getTargetClass(), getPropertyValue()));
    } else if (boolean.class.isAssignableFrom(getTargetClass())) {
      setValue(Boolean.parseBoolean(getPropertyValue()));
    } else if (Boolean.class.isAssignableFrom(getTargetClass())) {
      setValue(Boolean.valueOf(getPropertyValue()));
    } else if (JsonObject.class.isAssignableFrom(getTargetClass())) {
      setValue(JsonLoader.load(getComponentPath(), getPropertyValue()));
    }

    setStage2ProcessingRequired(false);
  }

  public void processStage1() {
    Matcher m = DEFERRED_PROPERTY_VALUE_PATTER.matcher(getPropertyValue());
    if (!m.find()) {
      return;
    }
    setDeferredComponentPath(m.group("component"));
    setDeferredComponentProperty(m.group("property"));
    setStage1ProcessingRequired(false);
  }

  public Object getValue() {
    if (isDeferredValue()) {
      if (getDeferredComponentProperty() == null) {
        setValue(getDeferredComponent());
      } else {
        String getValueMethodName = Util.buildGetPropertyName(getDeferredComponentProperty(),
                boolean.class.isAssignableFrom(getTargetClass()) || Boolean.parseBoolean(getPropertyValue()));
        Method getValueMethod = Util.getMethod(getDeferredComponent().getClass(), getValueMethodName);
        try {
          Object getValue = getValueMethod.invoke(getDeferredComponent());
          setValue(getValue);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
          throw new RuntimeException(ex);
        }
      }
    }
    return value;
  }

  public void setValue(Object pValue) {
    this.value = pValue;
  }

  public String getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(String pPropertyValue) {
    this.propertyValue = pPropertyValue;
  }

  public Class getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(Class pTargetClass) {
    this.targetClass = pTargetClass;
  }

  public String getDeferredComponentPath() {
    return deferredComponentPath;
  }

  public void setDeferredComponentPath(String pDeferredComponentPath) {
    this.deferredComponentPath = pDeferredComponentPath;
  }

  public String getDeferredComponentProperty() {
    return deferredComponentProperty;
  }

  public void setDeferredComponentProperty(String pDeferredComponentProperty) {
    this.deferredComponentProperty = pDeferredComponentProperty;
  }

  public Object getDeferredComponent() {
    return deferredComponent;
  }

  public void setDeferredComponent(Object pDeferredComponent) {
    this.deferredComponent = pDeferredComponent;
  }

  public String getComponentPath() {
    return componentPath;
  }

  public void setComponentPath(String pComponentPath) {
    this.componentPath = pComponentPath;
  }

  public boolean isDeferredValue() {
    return deferredValue;
  }

  public void setDeferredValue(boolean pDeferredValue) {
    this.deferredValue = pDeferredValue;
  }

  @Override
  public String toString() {
    return getComponentPath() + ":" + getClass().getCanonicalName() + ":" + getPropertyValue();
  }

  public boolean isStage1ProcessingRequired() {
    return stage1ProcessingRequired;
  }

  public void setStage1ProcessingRequired(boolean pStage1ProcessingRequired) {
    this.stage1ProcessingRequired = pStage1ProcessingRequired;
  }

  public boolean isStage2ProcessingRequired() {
    return stage2ProcessingRequired;
  }

  public void setStage2ProcessingRequired(boolean pStage2ProcessingRequired) {
    this.stage2ProcessingRequired = pStage2ProcessingRequired;
  }

}
