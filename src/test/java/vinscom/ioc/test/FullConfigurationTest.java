package vinscom.ioc.test;

import vinscom.ioc.ComponentManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import vinscom.ioc.test.component.PropertiesComponent;

public class FullConfigurationTest {

  private List<String> layers = new ArrayList<>(2);
  ComponentManager iocc;

  public FullConfigurationTest() {
    layers.add("/Users/vinay/Desktop/IoCContainer/testdata/layer1");
    layers.add("/Users/vinay/Desktop/IoCContainer/testdata/layer2");
  }

  @Before
  public void setUp() {
    iocc = ComponentManager.create(layers);
  }

  @Test
  public void loadGlobalComonentSetInPropertyFile() {
    Object inst = iocc.<Object>resolve("/vinscom/ioc/test/component/GlobalObjectSetSpecifically", Object.class);
    assertNotNull("Load global scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = iocc.<Object>resolve("/vinscom/ioc/test/component/GlobalObjectSetSpecifically", Object.class);
    assertSame(inst, inst2);
  }

  @Test
  public void loadGlobalComonentNotSetInPropertyFile() {
    Object inst = iocc.<Object>resolve("/vinscom/ioc/test/component/GlobalObjectByDefault", Object.class);
    assertNotNull("Load global scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = iocc.<Object>resolve("/vinscom/ioc/test/component/GlobalObjectByDefault", Object.class);
    assertSame(inst, inst2);
  }

  @Test
  public void loadLocalScopeComponent() {
    Object inst = iocc.<Object>resolve("/vinscom/ioc/test/component/LocalObject", Object.class);
    assertNotNull("Load local scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = iocc.<Object>resolve("/vinscom/ioc/test/component/LocalObject", Object.class);
    assertNotSame(inst, inst2);
  }

  @Test
  public void stringProperty() {
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertEquals("TestString", inst.getPropString());
    inst.setPropString("Wrong String");
    assertEquals("Wrong String", inst.getPropString());
    inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertEquals("TestString", inst.getPropString());
  }

  @Test
  public void stringArrayProperty() {
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertArrayEquals(new String[]{"a", "b", "c"}, inst.getPropArray());
    inst.setPropArray(new String[]{});
    assertArrayEquals(new String[]{}, inst.getPropArray());
    inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertArrayEquals(new String[]{"a", "b", "c"}, inst.getPropArray());
  }

  @Test
  public void stringListProperty() {
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    List<String> expected = new ArrayList<>();
    expected.add("a");
    expected.add("b");
    expected.add("c");
    expected.removeAll(inst.getPropList());
    assertEquals(expected.size(), 0);
  }

  @Test
  public void mapProperty() {
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    Map<String, String> result = inst.getPropMap();
    assertEquals("b", result.remove("a"));
    assertEquals("d", result.remove("c"));
    assertEquals("f", result.remove("e"));
  }

  @Test
  public void anotherComponentProperty() {
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertSame(Object.class, inst.getPropComponent().getClass());
  }

  @Test
  public void mergeProperties(){
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/MergedComponent", PropertiesComponent.class);
    assertEquals("TestString2", inst.getPropString());
    assertArrayEquals(new String[]{"a"}, inst.getPropArray());
    assertEquals(1, inst.getPropList().size());
    assertEquals(1, inst.getPropMap().size());
    
    Object refInst = iocc.<Object>resolve("/vinscom/ioc/test/component/GlobalObjectSetSpecifically", Object.class);
    assertSame(refInst, inst.getPropComponent());
  }

  @Test
  public void componentStartUp() {
    PropertiesComponent inst = iocc.<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertTrue(inst.isStartup());
  }
}
