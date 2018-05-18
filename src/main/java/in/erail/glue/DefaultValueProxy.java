package in.erail.glue;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.erail.glue.common.Util;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.enumeration.PropertyValueModifier;

public class DefaultValueProxy implements ValueProxy {

  protected Logger logger = LogManager.getLogger(DefaultValueProxy.class.getCanonicalName());
  private static final Pattern DEFERRED_PROPERTY_VALUE_PATTER = Pattern.compile("(?<component>^[^.]+)($|(\\.(?<property>.*)$))");
  @SuppressWarnings("rawtypes")
  private Class targetClass;
  private Collection<ValueWithModifier> propertyValue;
  private String componentPath;

  private Object value;
  private String deferredComponentPath;
  private String deferredComponentProperty;
  private Object deferredComponent;
  private boolean deferredComponentProcessed = false;
  private boolean deferredValue = false;
  private boolean processed = false;

  @Override
  public void init() {
    ValueWithModifier propValue = getLastValueWithModifier();

    if (PropertyValueModifier.FROM.equals(propValue.getPropertyValueModifier())) {
      deferredValue = true;
    }

    if (!(targetClass.isArray()
            || targetClass.isEnum()
            || ProxyValueResolverRegistry.getClassToValue().containsKey(targetClass)
            || Strings.isNullOrEmpty(propValue.getValue()))) {
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

  @Override
  public void process() {

    setProcessed(true);

    if (isDeferredValue()) {
      return;
    }

    if (getTargetClass().isArray()) {
      setValue(ProxyValueResolverRegistry.createArray(targetClass).apply(getPropertyValue(), this));
    } else if (getTargetClass().isEnum()) {
      setValue(ProxyValueResolverRegistry.getClassToValue().get(Enum.class).apply(getPropertyValue(), this));
    } else if (ProxyValueResolverRegistry.getClassToValue().containsKey(targetClass)) {
      setValue(ProxyValueResolverRegistry.getClassToValue().get(targetClass).apply(getPropertyValue(), this));
    } else if (Strings.isNullOrEmpty(getValueAsString())) {
      setValue(null);
    }

  }

  protected String getValueAsString() {
    String v = getLastValueWithModifier().getValue();
    if (Strings.isNullOrEmpty(v)) {
      return null;
    }
    return v;
  }

  protected ValueWithModifier getLastValueWithModifier() {
    return Util.getLastValueWithModifier(getPropertyValue());
  }

  @Override
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

  protected void setValue(Object pValue) {
    this.value = pValue;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Class getTargetClass() {
    return targetClass;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public void setTargetClass(Class pTargetClass) {
    this.targetClass = pTargetClass;
  }

  @Override
  public String getDeferredComponentPath() {
    return deferredComponentPath;
  }

  protected Collection<ValueWithModifier> getPropertyValue() {
    return propertyValue;
  }

  @Override
  public void setPropertyValue(Collection<ValueWithModifier> pPropertyValue) {
    this.propertyValue = pPropertyValue;
  }

  protected void setDeferredComponentPath(String pDeferredComponentPath) {
    this.deferredComponentPath = pDeferredComponentPath;
  }

  protected String getDeferredComponentProperty() {
    return deferredComponentProperty;
  }

  protected void setDeferredComponentProperty(String pDeferredComponentProperty) {
    this.deferredComponentProperty = pDeferredComponentProperty;
  }

  protected Object getDeferredComponent() {
    return deferredComponent;
  }

  @Override
  public void setDeferredComponent(Object pDeferredComponent) {
    this.deferredComponent = pDeferredComponent;
  }

  protected String getComponentPath() {
    return componentPath;
  }

  @Override
  public void setComponentPath(String pComponentPath) {
    this.componentPath = pComponentPath;
  }

  @Override
  public boolean isDeferredValue() {
    return deferredValue;
  }

  protected void setDeferredValue(boolean pDeferredValue) {
    this.deferredValue = pDeferredValue;
  }

  @Override
  public String toString() {

    return MoreObjects
            .toStringHelper(this)
            .omitNullValues()
            .add("TargetClass", getTargetClass().getCanonicalName())
            .add("ComponentPath", getComponentPath())
            .add("PropertyValue", Joiner.on(",").join(getPropertyValue()))
            .toString();

  }

  protected boolean isProcessed() {
    return processed;
  }

  protected void setProcessed(boolean pProcessed) {
    this.processed = pProcessed;
  }

  @Override
  public boolean isDeferredComponentProcessed() {
    return deferredComponentProcessed;
  }

  @Override
  public void setDeferredComponentProcessed(boolean pDeferredComponentProcessed) {
    this.deferredComponentProcessed = pDeferredComponentProcessed;
  }

}
