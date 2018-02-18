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
    TestInstanceConstructor inst = Glue.instance().resolve("/in/erail/glue/test/factory/TestInstanceConstructor");
    String[] expected = new String[]{"ninja","5","true"};
    assertArrayEquals(expected,inst.getResult().toArray());
  }

}
