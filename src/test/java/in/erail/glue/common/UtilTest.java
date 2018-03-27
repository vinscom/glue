package in.erail.glue.common;

import org.junit.Test;
import static org.junit.Assert.*;

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
  
}