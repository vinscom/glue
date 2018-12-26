package in.erail.glue;

import java.util.Map;

import com.google.common.collect.ListMultimap;

import in.erail.glue.common.ValueWithModifier;
import io.reactivex.Completable;
import io.reactivex.Maybe;

/**
 *
 * @author vinay
 */
public interface ConfigSerializationFactory {

  public static final String DEFAULT_IDENTIFIER = ".default";

  /**
   * Save configuration. Each call to this function will override 
   * last saved configuration.
   * @param pConfig Configuration Map
   * @return Map saved successfully
   */
  Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig);
  
  /**
   * Save configuration. Each call to this function will override 
   * last saved configuration.
   *
   * @param pConfig Configuration Map. 
   * @param pIdentifier Unique Identifier.
   * @return Map saved successfully
   */
  Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig, String pIdentifier);

  /**
   * Load configuration
   * @return Loaded map
   */
  Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load();
  
  /**
   * Load configuration
   * @param pIdentifier Unique Identifier
   * @return Loaded map
   */
  Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load(String pIdentifier);
}
