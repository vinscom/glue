package in.erail.glue.common;

import in.erail.glue.annotation.StartService;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

  @Test
  void buildSetPropertyName() {
    assertEquals("setTestProperty", Util.buildSetPropertyName("testProperty"));
    assertEquals("setTestProperty", Util.buildSetPropertyName("TestProperty"));
  }

  @Test
  void buildGetPropertyName() {
    assertEquals("getTestProperty", Util.buildGetPropertyName("testProperty", false));
    assertEquals("getTestProperty", Util.buildGetPropertyName("testProperty", false));
    assertEquals("isTestProperty", Util.buildGetPropertyName("testProperty", true));
  }

  @Test
  void getMethod() {
    assertNotNull(Util.getMethod(TestBean.class, "getName"));
    assertNull(Util.getMethod(TestBean.class, "getName1"));
  }

  @Test
  void getMethodWithAnnotation() {
    assertEquals("start", Util.getMethodWithAnnotation(TestBean.class, StartService.class).getName());
    assertEquals("getName", Util.getMethodWithAnnotation(TestBean.class, Nullable.class).getName());
    assertNull(Util.getMethodWithAnnotation(TestBean.class, Nonnull.class));
  }

  @Test
  void getClassOfFirstParameterOfMethod() {
    Method find1 = Util.getMethod(TestBean.class, "find1");
    assertTrue(Util.getClassOfFirstParameterOfMethod(find1).equals(String.class));
    Method find2 = Util.getMethod(TestBean.class, "find2");
    assertTrue(Util.getClassOfFirstParameterOfMethod(find2).equals(int.class));
    assertThrows(RuntimeException.class, () -> {
      Method start = Util.getMethod(TestBean.class, "start");
      Util.getClassOfFirstParameterOfMethod(start);
    });
  }

  @Test
  void createInstance() {
    assertDoesNotThrow(() -> {
      Util.createInstance(String.class.getCanonicalName());
    });
  }

  @Test
  void convertCSVIntoArray() {
    assertEquals(4, Util.convertCSVIntoArray("a,b,\",\",d").length);
  }

  @Test
  void getMapFromValue() throws IOException {
    assertTrue(Util.getMapFromValue("").isEmpty());
    String map = "\"a=,\",d=s";
    assertTrue(Util.getMapFromValue(map).size() == 2);
  }

  @Test
  void getEnvironmentValue() {
    System.setProperty("test.a.b", "testvalue");
    assertEquals("testvalue", Util.getEnvironmentValue("test.a.b"));
    assertEquals("testvalue", Util.getEnvironmentValue("test.a.b", null));
    assertEquals("NullValue", Util.getEnvironmentValue("test.a.c", "NullValue"));
  }

  @Test
  public void convertDotToCamelCase() {
    String source = "abc.def.abc";
    String result = "abcDefAbc";
    assertEquals(result, Util.convertDotToCamelCase(source));
  }

  @Test
  public void resolveFilePath() {
    assertThrows(RuntimeException.class, () -> {
      Util.resolveFilePath("po.xml").toString();
    });
    assertTrue(Util.resolveFilePath("pom*.xml").toAbsolutePath().endsWith("pom.xml"));
    assertTrue(Util.resolveFilePath("po*.xml").toAbsolutePath().endsWith("pom.xml"));
    assertTrue(Util.resolveFilePath("pom.xml").toAbsolutePath().endsWith("pom.xml"));
  }

  public static class TestBean {

    private String name;

    @Nullable
    public String getName() {
      return name;
    }

    public void setName(String pName) {
      this.name = pName;
    }

    @StartService
    public void start() {
    }

    public void find1(String pWhat) {
    }

    public void find2(int pIndex, String pWhat) {
    }
  }
}
