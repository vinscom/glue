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
    PropertiesRepository.setLayers(Arrays.asList(System.getProperty(Constant.EnvVar.Java.LAYERS).split(Constant.EnvVar.SEPERATOR)));
    
    instance.init();
    
    assertTrue(instance.isInitialized());
  }

}
