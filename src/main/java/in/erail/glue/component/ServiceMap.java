package in.erail.glue.component;

import java.util.Map;
import java.util.TreeMap;
import in.erail.glue.Glue;
import java.util.Collections;

/**
 *
 * @author vinay
 */
public class ServiceMap {

  private final Map<String, String> mServices;
  private Map<String, Object> mResolvedServices;

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

  public synchronized Map<String, Object> getServices() {
    if (mResolvedServices == null) {
      mResolvedServices = new TreeMap<>();
      mServices
              .entrySet()
              .stream()
              .forEach((kv) -> {
                Object component = Glue.instance().resolve(kv.getValue());
                mResolvedServices.put(kv.getKey(), component);
              });
    }
    return mResolvedServices;
  }

}
