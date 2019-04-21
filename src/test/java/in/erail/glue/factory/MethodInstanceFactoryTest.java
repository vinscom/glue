package in.erail.glue.factory;

import in.erail.glue.Glue;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author vinay
 */
public class MethodInstanceFactoryTest {
  
  @Test
  public void testCreateInstanceFromClass() {
    TestComponent inst = Glue.instance().resolve("/in/erail/glue/test/factory/TestComponentPC2");
    String[] expected = new String[]{"ninja","5"};
    assertArrayEquals(expected,inst.getResult().toArray());
  }

  @Test
  public void testCreateInstanceFromComponent() {
    TestComponent inst = Glue.instance().resolve("/in/erail/glue/test/factory/TestComponentPC3");
    String[] expected = new String[]{"ninja","5"};
    assertArrayEquals(expected,inst.getResult().toArray());
  }
  
  @Test
  public void testCreateInstanceWithParamType() {
    TestComponent inst = Glue.instance().resolve("/in/erail/glue/test/factory/TestComponentPC4");
    String[] expected = new String[]{"ninja","5"};
    assertArrayEquals(expected,inst.getResult().toArray());
  }
  
  @Test
  public void testCreateInstanceWithComponentParamType() {
    TestComponent inst = Glue.instance().resolve("/in/erail/glue/test/factory/TestComponentPC5");
    String[] expected = new String[]{"ninja","5","true"};
    assertArrayEquals(expected,inst.getResult().toArray());
  }
}
