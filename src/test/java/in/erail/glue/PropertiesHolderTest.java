package in.erail.glue;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import in.erail.glue.common.Constant;

/**
 *
 * @author vinay
 */
public class PropertiesHolderTest {

  @Test
  public void testInit() {
    System.out.println("init");
    PropertiesRepository instance = new PropertiesRepository();
    instance.setLayers(Arrays.asList(System.getProperty(Constant.SystemProperties.LAYERS).split(Constant.SystemProperties.SEPERATOR)));
    
    instance.init();
    
    assertTrue(instance.isInitialized());
  }

}
