package in.erail.glue.component;

import java.util.ArrayList;
import java.util.List;
import in.erail.glue.Glue;
import java.util.Collections;

/**
 *
 * @author vinay
 */
public class ServiceArray {

  private final List<String> mServices;
  private List<Object> mResolvedServices;

  public ServiceArray(List<String> pServices) {
    if (pServices == null) {
      mServices = Collections.emptyList();
    } else {
      this.mServices = pServices;
    }
  }

  public Object get(int pKey) {
    return getServices().get(pKey);
  }

  public synchronized List<Object> getServices() {
    if (mResolvedServices == null) {
      mResolvedServices = new ArrayList<>(mServices.size());
      mServices
              .stream()
              .forEach((v) -> {
                Object component = Glue.instance().resolve(v);
                mResolvedServices.add(component);
              });
    }
    return mResolvedServices;
  }

}
