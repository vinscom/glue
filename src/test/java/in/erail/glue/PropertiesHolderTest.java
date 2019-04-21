package in.erail.glue;

import java.util.Arrays;
import in.erail.glue.common.Constant;
import in.erail.glue.common.Util;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

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
