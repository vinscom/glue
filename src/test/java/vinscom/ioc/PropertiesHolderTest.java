package vinscom.ioc;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import vinscom.ioc.common.Constant;

/**
 *
 * @author vinay
 */
public class PropertiesHolderTest {

  @Test
  public void testInit() {
    System.out.println("init");
    PropertiesHolder instance = new PropertiesHolder();
    instance.setLayers(Arrays.asList(System.getProperty(Constant.SystemProperties.LAYERS).split(Constant.SystemProperties.SEPERATOR)));
    
    instance
            .init()
            .await();
    
    assertTrue(instance.isInitialized());
  }

}
