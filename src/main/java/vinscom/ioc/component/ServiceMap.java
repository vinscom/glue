package vinscom.ioc.component;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import vinscom.ioc.Glue;

/**
 *
 * @author vinay
 */
public class ServiceMap {

  private final Map<String, String> mServices;
  private Map<String, Object> mResolvedServices;

  public ServiceMap(Map<String, String> pServices) {
    if (pServices == null) {
      throw new RuntimeException("Services Map can't be null");
    }
    this.mServices = pServices;
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
