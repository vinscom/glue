package in.erail.glue.common;

import com.google.common.base.MoreObjects;

import in.erail.glue.enumeration.PropertyValueModifier;

/**
 *
 * @author vinay
 */
public class ValueWithModifier extends Tuple<String, PropertyValueModifier> {

  public ValueWithModifier(String pValue, PropertyValueModifier pModifier) {
    super(pValue, pModifier);
  }

  public String getValue(){
    return value1;
  }
  
  public PropertyValueModifier getPropertyValueModifier(){
    return value2;
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper(this)
            .add("Value", getValue())
            .add("PropertyValueModifier", getPropertyValueModifier().toString())
            .toString();
  }
  
}
