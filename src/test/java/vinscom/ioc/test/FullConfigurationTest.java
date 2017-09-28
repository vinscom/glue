package vinscom.ioc.test;

import io.vertx.core.json.JsonObject;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import vinscom.ioc.Glue;
import vinscom.ioc.Initial;
import vinscom.ioc.common.TestConstant;
import vinscom.ioc.test.component.PropertiesComponent;

public class FullConfigurationTest {

  @Test
  public void loadGlobalComonentSetInPropertyFile() {
    Object inst = Glue.instance().<Object>resolve("/vinscom/ioc/test/component/GlobalObjectSetSpecifically", Object.class);
    assertNotNull("Load global scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = Glue.instance().<Object>resolve("/vinscom/ioc/test/component/GlobalObjectSetSpecifically", Object.class);
    assertSame(inst, inst2);
  }

  @Test
  public void loadGlobalComonentNotSetInPropertyFile() {
    Object inst = Glue.instance().<Object>resolve("/vinscom/ioc/test/component/GlobalObjectByDefault", Object.class);
    assertNotNull("Load global scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = Glue.instance().<Object>resolve("/vinscom/ioc/test/component/GlobalObjectByDefault", Object.class);
    assertSame(inst, inst2);
  }

  @Test
  public void loadLocalScopeComponent() {
    Object inst = Glue.instance().<Object>resolve("/vinscom/ioc/test/component/LocalObject", Object.class);
    assertNotNull("Load local scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = Glue.instance().<Object>resolve("/vinscom/ioc/test/component/LocalObject", Object.class);
    assertNotSame(inst, inst2);
  }

  @Test
  public void stringProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertEquals("TestString", inst.getPropString());
    inst.setPropString("Wrong String");
    assertEquals("Wrong String", inst.getPropString());
    inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertEquals("TestString", inst.getPropString());
  }

  @Test
  public void booleanProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertTrue(inst.isPropBoolean());
  }
  
  @Test
  public void stringArrayProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertArrayEquals(new String[]{"a", "b", "c"}, inst.getPropArray());
    inst.setPropArray(new String[]{});
    assertArrayEquals(new String[]{}, inst.getPropArray());
    inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertArrayEquals(new String[]{"a", "b", "c"}, inst.getPropArray());
  }

  @Test
  public void stringListProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    List<String> expected = new ArrayList<>();
    expected.add("a");
    expected.add("b");
    expected.add("c");
    expected.removeAll(inst.getPropList());
    assertEquals(expected.size(), 0);
  }

  @Test
  public void mapProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    Map<String, String> result = inst.getPropMap();
    assertEquals("b", result.remove("a"));
    assertEquals("d", result.remove("c"));
    assertEquals("f", result.remove("e"));
  }

  @Test
  public void anotherComponentProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertSame(Object.class, inst.getPropComponent().getClass());
  }

  @Test
  public void mergeProperties() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/MergedComponent", PropertiesComponent.class);
    assertEquals("TestString2", inst.getPropString());
    assertArrayEquals(new String[]{"a"}, inst.getPropArray());
    assertEquals(1, inst.getPropList().size());
    assertEquals(1, inst.getPropMap().size());

    PropertiesComponent refInst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent2", PropertiesComponent.class);
    assertEquals(refInst.getPropString(), "TestString3");
  }

  @Test
  public void componentStartUp() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/PropertiesComponent", PropertiesComponent.class);
    assertTrue(inst.isStartup());
  }
  
  @Test
  public void jsonProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/vinscom/ioc/test/component/MergedComponent", PropertiesComponent.class);
    assertEquals(new JsonObject(TestConstant.TEST_JSON).toString(), inst.getPropJson().toString());
  }

  @Test
  public void componentInitial() {
    Initial inst = Glue.instance().<Initial>resolve("/vinscom/ioc/test/component/Initial", Initial.class);
    List<Object> comps = inst.getComponents();
    assertEquals(comps.size(), 4);
    comps.forEach((comp) -> {
      assertEquals(comp.getClass(), PropertiesComponent.class);
    });
  }
}
