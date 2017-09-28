package vinscom.ioc.common;

import io.vertx.core.json.JsonObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonLoaderTest {
  
  public JsonLoaderTest() {
  }
  
  @Test
  public void testLoad() {
    System.out.println("load");
    String pComponentPath = "/vinscom/ioc/test/component/GlobalObjectByDefault";
    String pFile = "testconfig.json";
    JsonObject expResult = new JsonObject(TestConstant.TEST_JSON);
    JsonObject result = JsonLoader.load(pComponentPath, pFile);
    assertEquals(expResult.toString(), result.toString());
  }
  
}
