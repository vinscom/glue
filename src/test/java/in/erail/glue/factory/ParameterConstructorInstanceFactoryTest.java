package in.erail.glue.factory;

import in.erail.glue.Glue;
import org.junit.Test;
import static org.junit.Assert.*;

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
