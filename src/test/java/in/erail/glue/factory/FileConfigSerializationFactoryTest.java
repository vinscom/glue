package in.erail.glue.factory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import in.erail.glue.ConfigSerializationFactory;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.enumeration.PropertyValueModifier;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vinay
 */
public class FileConfigSerializationFactoryTest {

  Map<String, ListMultimap<String, ValueWithModifier>> pConfig = new HashMap<>();
  ConfigSerializationFactory factory;
  
  @Before
  public void setUp() {
    ListMultimap<String, ValueWithModifier> multiMap = ArrayListMultimap.create();
    multiMap.put("p1", new ValueWithModifier("test", PropertyValueModifier.MINUS));
    multiMap.put("p1", new ValueWithModifier("test2", PropertyValueModifier.MINUS));
    multiMap.put("p2", new ValueWithModifier("test3", PropertyValueModifier.MINUS));
    multiMap.put("p3", new ValueWithModifier("test4", PropertyValueModifier.MINUS));
    pConfig.put("a/b/c", multiMap);
    
    factory = new FileConfigSerializationFactory();
  }

  @Test
  public void testSaveLoad() {
    Map<String, ListMultimap<String, ValueWithModifier>> result = factory.save(pConfig).andThen(factory.load()).blockingGet();
    assertEquals(1, result.size());
    assertEquals("test3", result.get("a/b/c").get("p2").get(0).value1);
  }

}
