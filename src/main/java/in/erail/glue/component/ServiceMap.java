package in.erail.glue.component;

import java.util.Map;
import in.erail.glue.Glue;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 *
 * @author vinay
 * @param <T>
 */
public class ServiceMap<T> {

  private final Map<String, String> mServices;
  private Map<String, T> mResolvedServices;

  public ServiceMap(Map<String, String> pServices) {
    if (pServices == null) {
      this.mServices = Collections.emptyMap();
    } else {
      this.mServices = pServices;
    }
  }

  public Object get(String pKey) {
    return getServices().get(pKey);
  }

  public synchronized Map<String, T> getServices() {
    if (mResolvedServices == null) {
      mResolvedServices = new LinkedHashMap<>();
      mServices
              .entrySet()
              .stream()
              .forEach((kv) -> {
                T component = (T) Glue.instance().resolve(kv.getValue());
                mResolvedServices.put(kv.getKey(), component);
              });
    }
    return mResolvedServices;
  }

}
