package vinscom.ioc.enumeration;

/**
 *
 * @author vinay
 */
public enum PropertyValueModifier {
  PLUS,
  MINUS,
  FROM,
  NONE;
  
  public static PropertyValueModifier parse(String pModifer){
    switch(pModifer){
      case "+":
        return PLUS;
      case "-":
        return MINUS;
      case "^":
        return FROM;
    }
    return NONE;
  }
}
