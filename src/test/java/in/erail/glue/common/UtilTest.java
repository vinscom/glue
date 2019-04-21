package in.erail.glue.common;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author vinay
 */
public class UtilTest {
  @Test
  public void testConvertDotToCamelCase() {
    String source = "abc.def.abc";
    String result = "abcDefAbc";
    assertEquals(result, Util.convertDotToCamelCase(source));
  }
  
  @Test
  public void testEnvironmentValue() {
    assertEquals("a", Util.getEnvironmentValue("first"));
    assertEquals("b", Util.getEnvironmentValue("second.dot"));
  }
}
