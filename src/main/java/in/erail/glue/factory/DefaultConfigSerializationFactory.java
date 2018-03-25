package in.erail.glue.factory;

import com.google.common.collect.ListMultimap;
import in.erail.glue.ConfigSerializationFactory;
import in.erail.glue.common.ValueWithModifier;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import java.util.Map;

/**
 *
 * @author vinay
 */
public class DefaultConfigSerializationFactory implements ConfigSerializationFactory {

  @Override
  public Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig) {
    return save(pConfig, DEFAULT_IDENTIFIER);
  }

  @Override
  public Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig, String pIdentifier) {
    return Completable.complete();
  }

  @Override
  public Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load() {
    return load(DEFAULT_IDENTIFIER);
  }

  @Override
  public Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load(String pIdentifier) {
    return Maybe.empty();
  }

}
