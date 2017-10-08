package vinscom.ioc;

import java.util.List;

public interface Glue {
  Object resolve(String pPath);

  <T> T resolve(String pPath, Class<T> pClass);

  static Glue instance() {
    return ComponentRepository.instance();
  }

  static Glue instance(List<String> pLayers) {
    return ComponentRepository.instance(pLayers);
  }
}
