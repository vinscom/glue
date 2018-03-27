package in.erail.glue;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import in.erail.glue.common.Constant;
import in.erail.glue.common.Util;

/**
 *
 * @author vinay
 */
public class PropertiesHolderTest {

  @Test
  public void testInit() {
    System.out.println("init");
    PropertiesRepository instance = new PropertiesRepository();
    PropertiesRepository.setLayers(Arrays.asList(Util.getEnvironmentValue(Constant.SystemProperties.LAYERS).split(Constant.SystemProperties.SEPERATOR)));
    
    instance.init();
    
    assertTrue(instance.isInitialized());
  }

}
