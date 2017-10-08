package vinscom.ioc;

import io.vertx.core.json.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vinscom.ioc.common.JsonLoader;
import vinscom.ioc.common.Util;
import vinscom.ioc.common.ValueWithModifier;
import vinscom.ioc.enumeration.PropertyValueModifier;

public class ValueProxy {

  private static final Pattern DEFERRED_PROPERTY_VALUE_PATTER = Pattern.compile("(?<component>^[^.]+)($|(\\.(?<property>.*)$))");
  private Class targetClass;
  private Collection<ValueWithModifier> propertyValue;
  private String componentPath;

  private Object value;
  private String deferredComponentPath;
  private String deferredComponentProperty;
  private Object deferredComponent;
  private boolean deferredValue = false;
  private boolean processed = false;

  public ValueProxy() {
  }

  public ValueProxy(Class pTargetClass, Collection<ValueWithModifier> pPropertyValue, String pComponentPath) {
    this.targetClass = pTargetClass;
    this.propertyValue = pPropertyValue;
    this.componentPath = pComponentPath;

    ValueWithModifier propValue = getLastValueWithModifier();

    if (PropertyValueModifier.FROM.equals(propValue.getPropertyValueModifier())) {
      deferredValue = true;
    }

    if (!(targetClass.isArray()
            || String.class.isAssignableFrom(targetClass)
            || List.class.isAssignableFrom(targetClass)
            || Map.class.isAssignableFrom(targetClass)
            || Enum.class.isAssignableFrom(targetClass)
            || boolean.class.isAssignableFrom(targetClass)
            || Boolean.class.isAssignableFrom(targetClass)
            || JsonObject.class.isAssignableFrom(targetClass)
            || Set.class.isAssignableFrom(targetClass))) {
      deferredValue = true;
    }

    if (deferredValue) {
      Matcher m = DEFERRED_PROPERTY_VALUE_PATTER.matcher(propValue.getValue());
      if (m.find()) {
        deferredComponentPath = m.group("component");
        deferredComponentProperty = m.group("property");
      }
    }
  }

  public void process() {

    setProcessed(true);

    if (isDeferredValue()) {
      return;
    }

    if (getTargetClass().isArray()) {
      setValue(getValueAsArray());
    } else if (String.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsString());
    } else if (List.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsList());
    } else if (Map.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsMap());
    } else if (Enum.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsEnum());
    } else if (boolean.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsboolean());
    } else if (Boolean.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsBoolean());
    } else if (JsonObject.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsJson());
    } else if (Set.class.isAssignableFrom(getTargetClass())) {
      setValue(getValueAsSet());
    }

  }

  private String[] getValueAsArray() {
    return getValueAsString().split(",");
  }

  private String getValueAsString() {
    return getLastValueWithModifier().getValue();
  }

  private Set<String> getValueAsSet() {

    final Set<String> result = new HashSet<>();

    List<ValueWithModifier> v = (List) getPropertyValue();
    v.stream().forEach((vwm) -> {
      List<String> l = Arrays.asList(vwm.getValue().split(","));
      switch (vwm.getPropertyValueModifier()) {
        case PLUS:
          result.addAll(l);
          break;
        case MINUS:
          result.removeAll(l);
          break;
        default:
          result.clear();
          result.addAll(l);
      }
    });

    return result;

  }

  private List<String> getValueAsList() {

    final List<String> result = new ArrayList<>();

    List<ValueWithModifier> v = (List) getPropertyValue();
    v.stream().forEach((vwm) -> {
      List<String> l = Arrays.asList(vwm.getValue().split(","));
      switch (vwm.getPropertyValueModifier()) {
        case PLUS:
          result.addAll(l);
          break;
        case MINUS:
          result.removeAll(l);
          break;
        default:
          result.clear();
          result.addAll(l);
      }
    });

    return result;

  }

  private HashMap<String, String> getValueAsMap() {

    final HashMap<String, String> result = new HashMap<>();

    List<ValueWithModifier> v = (List) getPropertyValue();
    v.stream().forEach((vwm) -> {
      Map m = Util.getMapFromValue(vwm.getValue());
      switch (vwm.getPropertyValueModifier()) {
        case PLUS:
          result.putAll(m);
          break;
        case MINUS:
          result.keySet().removeAll(m.keySet());
          break;
        default:
          result.clear();
          result.putAll(m);
      }
    });

    return result;
  }

  private Enum getValueAsEnum() {
    return Enum.valueOf(getTargetClass(), getValueAsString());
  }

  private Boolean getValueAsBoolean() {
    return Boolean.valueOf(getValueAsString());
  }

  private boolean getValueAsboolean() {
    return Boolean.parseBoolean(getValueAsString());
  }

  private JsonObject getValueAsJson() {
    return JsonLoader.load(getComponentPath(), getValueAsString());
  }

  private ValueWithModifier getLastValueWithModifier() {
    return Util.getLastValueWithModifier(getPropertyValue());
  }

  public Object getValue() {
    if (isDeferredValue()) {
      if (getDeferredComponentProperty() == null) {
        setValue(getDeferredComponent());
      } else {
        String getValueMethodName = Util.buildGetPropertyName(getDeferredComponentProperty(),
                boolean.class.isAssignableFrom(getTargetClass()) || Boolean.class.isAssignableFrom(getTargetClass()));
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

  public Class getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(Class pTargetClass) {
    this.targetClass = pTargetClass;
  }

  public String getDeferredComponentPath() {
    return deferredComponentPath;
  }

  public Collection<ValueWithModifier> getPropertyValue() {
    return propertyValue;
  }

  public void setPropertyValue(Collection<ValueWithModifier> pPropertyValue) {
    this.propertyValue = pPropertyValue;
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

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean pProcessed) {
    this.processed = pProcessed;
  }

}
