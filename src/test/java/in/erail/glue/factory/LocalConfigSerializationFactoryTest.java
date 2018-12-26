package in.erail.glue.factory;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import in.erail.glue.ConfigSerializationFactory;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.enumeration.PropertyValueModifier;

/**
 *
 * @author vinay
 */
public class LocalConfigSerializationFactoryTest {

  @Test
  public void testSaveLoad() throws IOException {
    ListMultimap<String, ValueWithModifier> multiMap = ArrayListMultimap.create();

    Map<String, ListMultimap<String, ValueWithModifier>> pConfig = new HashMap<>();
    multiMap.put("p1", new ValueWithModifier("test", PropertyValueModifier.MINUS));
    multiMap.put("p1", new ValueWithModifier("test2", PropertyValueModifier.MINUS));
    multiMap.put("p2", new ValueWithModifier("test3", PropertyValueModifier.MINUS));
    multiMap.put("p3", new ValueWithModifier("test4", PropertyValueModifier.MINUS));
    pConfig.put("a/b/c", multiMap);

    ConfigSerializationFactory factory = new LocalConfigSerializationFactory();
    Map<String, ListMultimap<String, ValueWithModifier>> result = factory.save(pConfig, ".saveload.test").andThen(factory.load(".saveload.test")).blockingGet();
    assertEquals(1, result.size());
    assertEquals("test3", result.get("a/b/c").get("p2").get(0).value1);

    Files.deleteIfExists(Paths.get("glue.ser.saveload.test"));
  }

  @Test
  public void testConfigGeneration() throws IOException {
    
    System.setProperty(LocalConfigSerializationFactory.ENV_IDENTIFIER, ".auto.generated.test");
    
    LocalConfigSerializationFactory.main(new String[]{});

    System.clearProperty(LocalConfigSerializationFactory.ENV_IDENTIFIER);

    ConfigSerializationFactory factory = new LocalConfigSerializationFactory();
    Map<String, ListMultimap<String, ValueWithModifier>> result = factory.load(".auto.generated.test").blockingGet();
    assertEquals("java.lang.Object", result.get("/in/erail/glue/test/component/GlobalObjectByDefault").get("$class").get(0).value1);

    Files.deleteIfExists(Paths.get("glue.ser.auto.generated.test"));
  }
}
