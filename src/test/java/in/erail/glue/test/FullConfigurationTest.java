package in.erail.glue.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import in.erail.glue.Glue;
import in.erail.glue.PropertiesRepository;
import in.erail.glue.common.TestConstant;
import in.erail.glue.component.EnumTestValues;
import in.erail.glue.component.Initial;
import in.erail.glue.component.PropertiesComponent;
import io.vertx.core.json.JsonObject;

public class FullConfigurationTest {

  @Test
  public void loadGlobalComonentSetInPropertyFile() {
    Object inst = Glue.instance().<Object>resolve("/in/erail/glue/test/component/GlobalObjectSetSpecifically");
    assertNotNull("Load global scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = Glue.instance().<Object>resolve("/in/erail/glue/test/component/GlobalObjectSetSpecifically");
    assertSame(inst, inst2);
  }

  @Test
  public void loadGlobalComonentNotSetInPropertyFile() {
    Object inst = Glue.instance().<Object>resolve("/in/erail/glue/test/component/GlobalObjectByDefault");
    assertNotNull("Load global scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = Glue.instance().<Object>resolve("/in/erail/glue/test/component/GlobalObjectByDefault");
    assertSame(inst, inst2);
  }

  @Test
  public void loadLocalScopeComponent() {
    Object inst = Glue.instance().<Object>resolve("/in/erail/glue/test/component/LocalObject");
    assertNotNull("Load local scope component when set in property file", inst);
    assertEquals(inst.getClass(), Object.class);

    Object inst2 = Glue.instance().<Object>resolve("/in/erail/glue/test/component/LocalObject");
    assertNotSame(inst, inst2);
  }

  @Test
  public void stringProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals("TestString", inst.getPropString());
    inst.setPropString("Wrong String");
    assertEquals("Wrong String", inst.getPropString());
    inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals("TestString", inst.getPropString());
  }

  @Test
  public void booleanProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertTrue(inst.isPropBoolean());
    assertTrue(inst.isPropBoolean2());
  }

  @Test
  public void booleanPattern() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertTrue(inst.getPropPattern().matcher("test").find());
  }

  @Test
  public void longProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals(inst.getPropLong(), 2l);
    assertTrue(inst.getPropLong2().equals(2l));
  }

  @Test
  public void fileProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals(inst.getPropFile().getName(), "testconfig.json");
  }

  @Test
  public void stringArrayProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertArrayEquals(new String[]{"a", "b", "c", "e,f"}, inst.getPropArray());
    inst.setPropArray(new String[]{});
    assertArrayEquals(new String[]{}, inst.getPropArray());
    inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertArrayEquals(new String[]{"a", "b", "c", "e,f"}, inst.getPropArray());
  }

  @Test
  public void stringListProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    List<String> expected = new ArrayList<>();
    expected.add("a");
    expected.add("b");
    expected.add("c");
    expected.add("e,f");
    expected.removeAll(inst.getPropList());
    assertEquals(expected.size(), 0);
  }

  @Test
  public void stringSetProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    Set<String> expected = new HashSet<>();
    expected.add("a");
    expected.add("b");
    expected.add("c");
    expected.add("e,f");
    expected.removeAll(inst.getPropSet());
    assertEquals(expected.size(), 0);
  }

  @Test
  public void mapProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    Map<String, String> result = inst.getPropMap();
    assertEquals("b", result.remove("a"));
    assertEquals("d", result.remove("c"));
    assertEquals("f", result.remove("e"));
    assertEquals("h,i", result.remove("g"));
  }

  @Test
  public void integerProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals(inst.getPropInt(), 2);
    assertTrue(inst.getPropInteger().equals(2));
  }

  @Test
  public void loggerAsProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    Logger logger = inst.getPropLogger();
    assertEquals(logger.getName(), "in.erail.glue.test.component.PropertiesComponent");
  }

  @Test
  public void anotherComponentProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertSame(Object.class, inst.getPropComponent().getClass());
  }

  @Test
  public void mergeProperties() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/MergedComponent");
    assertEquals("TestString2", inst.getPropString());
    assertArrayEquals(new String[]{"a", "b", "c", "x"}, inst.getPropArray());
    assertEquals(5, inst.getPropList().size());
    assertEquals(3, inst.getPropMap().size());
    assertEquals(3, inst.getPropSet().size());

    PropertiesComponent refInst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent2");
    assertEquals(refInst.getPropString(), "TestString3");
  }

  @Test
  public void componentStartUp() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertTrue(inst.isStartup());
  }

  @Test
  public void jsonProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/MergedComponent");
    assertEquals(new JsonObject(TestConstant.TEST_JSON).toString(), inst.getPropJson().toString());
  }

  @Test
  public void enumProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals(EnumTestValues.TWO, inst.getPropEnum());
  }

  @Test
  public void serviceMapProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    Object inst2 = Glue.instance().<Object>resolve("/in/erail/glue/test/component/GlobalObjectByDefault");
    assertEquals(3, inst.getPropServiceMap().getServices().size());
    assertSame(inst2, inst.getPropServiceMap().get("a"));
    assertSame(inst2, inst.getPropServiceMap().get("b"));
    assertSame(inst2, inst.getPropServiceMap().get("c"));
  }

  @Test
  public void componentInitial() {
    Initial inst = Glue.instance().<Initial>resolve("/in/erail/glue/test/component/Initial");
    List<Object> comps = inst.getComponents();
    assertEquals(comps.size(), 4);
    comps.forEach((comp) -> {
      assertEquals(comp.getClass(), PropertiesComponent.class);
    });
  }

  @Test
  public void refComponentTest() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    PropertiesComponent inst2 = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/RefPropertiesComponent");
    PropertiesComponent inst3 = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/MergedComponent");
    Object inst4 = Glue.instance().<Object>resolve("/in/erail/glue/test/component/GlobalObjectByDefault");
    assertEquals(inst.getPropString(), inst2.getPropString());
    assertArrayEquals(inst.getPropArray(), inst2.getPropArray());
    assertTrue(inst.getPropList().equals(inst2.getPropList()));
    assertTrue(inst.getPropMap().keySet().equals(inst2.getPropMap().keySet()));
    assertSame(inst.getPropComponent(), inst2.getPropComponent());
    assertEquals(inst.isPropBoolean(), inst2.isPropBoolean());
    assertEquals(inst.getPropEnum(), inst2.getPropEnum());
    assertEquals(inst3.getPropJson(), inst2.getPropJson());

    assertEquals(3, inst2.getPropServiceMap().getServices().size());
    assertSame(inst4, inst2.getPropServiceMap().get("a"));
    assertSame(inst4, inst2.getPropServiceMap().get("b"));
    assertSame(inst4, inst2.getPropServiceMap().get("c"));
  }

  @Test
  public void basedOnComponentTest() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/BasedOnPropertiesComponent");
    assertEquals("TestString2", inst.getPropString());
  }

  @Test
  public void nullProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals(0, inst.getPropNullArray().length);
    assertEquals(0, inst.getPropNullList().size());
    assertEquals(0, inst.getPropNullMap().size());
    assertNull(inst.getPropNullComponent());
    assertFalse(inst.isPropNullBoolean());
    assertNull(inst.getPropNullEnum());
    assertEquals(0, inst.getPropNullSet().size());
    assertNull(inst.getPropNullBoolean2());
    assertEquals(0, inst.getPropNullServiceMap().getServices().size());
    assertEquals(0, inst.getPropNullInt());
    assertNull(inst.getPropNullInteger());
    assertNull(inst.getPropNullFile());
    assertEquals(0l, inst.getPropNullLong());
    assertNull(inst.getPropNullLong2());
    assertNull(inst.getPropNullPattern());
  }

  @Test
  public void componetFromZipFile() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent3");
    assertEquals("ZipString", inst.getPropString());
  }

  @Test
  public void metricProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertNotNull(inst.getPropTimer());
    assertNotNull(inst.getPropCounter());
    assertNotNull(inst.getPropHistogram());
    assertNotNull(inst.getPropMeter());
  }
  
  @Test
  public void componentArrayProperty() {
    PropertiesComponent inst2 = Glue.instance().resolve("/in/erail/glue/test/component/RefPropertiesComponent");
    Object[] objs = inst2.getPropComponentArray();
    assertEquals(objs[0].getClass().getCanonicalName(),"in.erail.glue.component.ArrayComponent1");
    assertEquals(objs[1].getClass().getCanonicalName(),"in.erail.glue.component.ArrayComponent2");
    assertEquals(objs[2].getClass().getCanonicalName(),"in.erail.glue.component.ArrayComponent3");
  }
  
  @Test
  public void varArgComponentArrayProperty() {
    PropertiesComponent inst2 = Glue.instance().resolve("/in/erail/glue/test/component/RefPropertiesComponent");
    Object[] objs = inst2.getPropVarArgComponentArray();
    assertEquals(objs[0].getClass().getCanonicalName(),"in.erail.glue.component.ArrayComponent1");
    assertEquals(objs[1].getClass().getCanonicalName(),"in.erail.glue.component.ArrayComponent2");
    assertEquals(objs[2].getClass().getCanonicalName(),"in.erail.glue.component.ArrayComponent3");
  }
  
  @Test
  public void classProperty() {
    PropertiesComponent inst = Glue.instance().<PropertiesComponent>resolve("/in/erail/glue/test/component/PropertiesComponent");
    assertEquals(inst.getPropClass(),PropertiesRepository.class);
  }
}
