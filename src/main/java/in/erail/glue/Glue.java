package in.erail.glue;

public interface Glue {

  <T> T resolve(String pPath);

  static Glue instance() {
    return ComponentRepository.instance();
  }
}
