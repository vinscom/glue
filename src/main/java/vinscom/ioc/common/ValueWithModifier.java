package vinscom.ioc.common;

import vinscom.ioc.enumeration.PropertyValueModifier;

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
}
