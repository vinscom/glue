package in.erail.glue.factory;

import in.erail.glue.Glue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author vinay
 */
public class ParameterConstructorInstanceFactoryTest {
  

  @Test
  public void testCreateInstance() {
    TestComponent2 inst = Glue.instance().resolve("/in/erail/glue/test/factory/TestComponentPC1");
    String[] expected = new String[]{"ninja","5","true","a","b"};
    assertArrayEquals(expected,inst.getResult().toArray());
  }

}
