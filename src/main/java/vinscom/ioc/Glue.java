package vinscom.ioc;

public interface Glue {
  Object resolve(String pPath);

  <T> T resolve(String pPath, Class<T> pClass);

  static Glue instance() {
    return ComponentRepository.instance();
  }
}
