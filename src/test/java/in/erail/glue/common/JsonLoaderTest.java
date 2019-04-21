package in.erail.glue.common;

import io.vertx.core.json.JsonObject;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class JsonLoaderTest {
  
  public JsonLoaderTest() {
  }
  
  @Test
  public void testLoad() {
    System.out.println("load");
    String pComponentPath = "/in/erail/glue/test/component/GlobalObjectByDefault";
    String pFile = "testconfig.json";
    JsonObject expResult = new JsonObject(TestConstant.TEST_JSON);
    JsonObject result = JsonLoader.load(pComponentPath, pFile);
    assertEquals(expResult.toString(), result.toString());
  }
  
}
