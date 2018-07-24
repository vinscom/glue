package in.erail.glue;

import com.google.common.base.Strings;
import in.erail.glue.common.Constant;
import in.erail.glue.common.Util;
import in.erail.glue.common.ValueWithModifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author vinay
 */
public class ValueProxyBuilder {

  private static Class valueProxyClass;
  private Class mTargetClass;
  private Collection<ValueWithModifier> mPropertyValue;
  private String mComponentPath;

  static {
    String envValueProxyClassName = Util.getEnvironmentValue(Constant.SystemProperties.VALUE_PROXY_CLASS);
    if (Strings.isNullOrEmpty(envValueProxyClassName)) {
      valueProxyClass = DefaultValueProxy.class;
    } else {
      try {
        valueProxyClass = Class.forName(envValueProxyClassName);
      } catch (ClassNotFoundException ex) {
        throw new RuntimeException(ex);
      }
    }
  }

  public Class getTargetClass() {
    return mTargetClass;
  }

  public ValueProxyBuilder setTargetClass(Class pTargetClass) {
    this.mTargetClass = pTargetClass;
    return this;
  }

  public Collection<ValueWithModifier> getPropertyValue() {
    return mPropertyValue;
  }

  public ValueProxyBuilder setPropertyValue(Collection<ValueWithModifier> pPropertyValue) {
    this.mPropertyValue = pPropertyValue;
    return this;
  }

  public String getComponentPath() {
    return mComponentPath;
  }

  public ValueProxyBuilder setComponentPath(String pComponentPath) {
    this.mComponentPath = pComponentPath;
    return this;
  }

  public static ValueProxyBuilder newBuilder() {
    return new ValueProxyBuilder();
  }

  public ValueProxy build() {
    try {
      ValueProxy vp = (ValueProxy) valueProxyClass.getDeclaredConstructor().newInstance();
      vp.setTargetClass(getTargetClass());
      vp.setPropertyValue(getPropertyValue());
      vp.setComponentPath(getComponentPath());
      vp.init();
      return vp;
    } catch (InstantiationException
            | IllegalAccessException
            | NoSuchMethodException
            | SecurityException
            | IllegalArgumentException
            | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
  }

}
