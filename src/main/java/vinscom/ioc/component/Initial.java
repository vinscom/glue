package vinscom.ioc.component;

import java.util.ArrayList;
import java.util.List;
import vinscom.ioc.ComponentManager;
import vinscom.ioc.annotation.StartService;

public class Initial {

  private String[] initial;
  private final List<Object> components = new ArrayList<>();

  @StartService
  public void startup() {
    for (String path : initial) {
      components.add(ComponentManager.instance().resolve(path));
    }
  }

  public String[] getInitial() {
    return initial;
  }

  public void setInitial(String[] pInitial) {
    this.initial = pInitial;
  }

  public List<Object> getComponents() {
    return components;
  }

}
