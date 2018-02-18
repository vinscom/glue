package in.erail.glue.factory;

import in.erail.glue.Glue;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vinay
 */
public class MethodInstanceFactoryTest {
  
  @Test
  public void testCreateInstanceFromClass() {
    MethodInstanceFactory mif = Glue.instance().resolve("/in/erail/glue/test/factory/MethodInstanceFactoryClassOnly");
    Optional instance = mif.createInstance();
    assertTrue(instance.isPresent());
    List<String> result = (List<String>) instance.get();
    String[] expected = new String[]{"ninja","5"};
    assertArrayEquals(expected,result.toArray());
  }

  @Test
  public void testCreateInstanceFromComponent() {
    MethodInstanceFactory mif = Glue.instance().resolve("/in/erail/glue/test/factory/MethodInstanceFactoryInstanceOnly");
    Optional instance = mif.createInstance();
    assertTrue(instance.isPresent());
    List<String> result = (List<String>) instance.get();
    String[] expected = new String[]{"ninja","5"};
    assertArrayEquals(expected,result.toArray());
  }
  
  @Test
  public void testCreateInstanceWithParamType() {
    MethodInstanceFactory mif = Glue.instance().resolve("/in/erail/glue/test/factory/MethodInstanceFactoryParamType");
    Optional instance = mif.createInstance();
    assertTrue(instance.isPresent());
    List<String> result = (List<String>) instance.get();
    String[] expected = new String[]{"ninja","5"};
    assertArrayEquals(expected,result.toArray());
  }
  
  @Test
  public void testCreateInstanceWithComponentParamType() {
    MethodInstanceFactory mif = Glue.instance().resolve("/in/erail/glue/test/factory/MethodInstanceFactoryWithComponentParamType");
    Optional instance = mif.createInstance();
    assertTrue(instance.isPresent());
    List<String> result = (List<String>) instance.get();
    String[] expected = new String[]{"ninja","5","true"};
    assertArrayEquals(expected,result.toArray());
  }
}
