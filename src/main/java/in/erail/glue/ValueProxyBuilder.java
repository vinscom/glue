package in.erail.glue;

import in.erail.glue.common.ValueWithModifier;
import java.util.Collection;

/**
 *
 * @author vinay
 */
public class ValueProxyBuilder {

  private Class mValueProxyClass = ValueProxy.class;
  private Class mTargetClass;
  private Collection<ValueWithModifier> mPropertyValue;
  private String mComponentPath;

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

  public Class getValueProxyClass() {
    return mValueProxyClass;
  }

  public ValueProxyBuilder setValueProxyClass(Class pValueProxyClass) {
    this.mValueProxyClass = pValueProxyClass;
    return this;
  }

  public static ValueProxyBuilder newBuilder() {
    return new ValueProxyBuilder();
  }

  public ValueProxy build() {
    ValueProxy vp = new DefaultValueProxy();
    vp.setTargetClass(getTargetClass());
    vp.setPropertyValue(getPropertyValue());
    vp.setComponentPath(getComponentPath());
    vp.init();
    return vp;
  }

}
