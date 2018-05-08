package in.erail.glue;

import in.erail.glue.common.ValueWithModifier;
import java.util.Collection;

public interface ValueProxy {

  /**
   * Process String based Property Value
   */
  void process();
  
  /**
   * Return resolved value
   * @return Resolve object
   */
  Object getValue();
  
  /**
   * Resolved object expected class
   * @return Resolved object expected class
   */
  Class getTargetClass();
  
  /**
   * Does value required deferred processing. Deferred processing
   * happens in case of one component property pointing to another 
   * component or property.
   * @return 
   */
  boolean isDeferredValue();
  
  /**
   * Check if deferred value is already calculated.
   * @return 
   */
  boolean isDeferredComponentProcessed();
  
  /**
   * Mark deferred processing status
   * @param pDeferredComponentProcessed True is processed or else false
   */
  void setDeferredComponentProcessed(boolean pDeferredComponentProcessed);
  
  /**
   * Deferred component path
   * @return Path to component
   */
  String getDeferredComponentPath();
  
  /**
   * Deferred component instance
   * @param pDeferredComponent component instance
   */
  void setDeferredComponent(Object pDeferredComponent);
 
 /**
  * Expected target value class
  * @param pClass Target value class
  */
 void setTargetClass(Class pClass);
 
 /**
  * Ordered properties value from all layers
  * @param pPropertyValue Ordered collection
  */
 void setPropertyValue(Collection<ValueWithModifier> pPropertyValue);
 
 /**
  * Path of component to which this property/value belongs
  * @param pPath Path
  */
 void setComponentPath(String pPath);
 
 /**
  * Initialise value proxy instance
  */
 void init();
}
