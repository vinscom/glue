package in.erail.glue.factory;

import com.google.common.base.Strings;
import in.erail.glue.component.PropertiesComponent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vinay
 */
public class TestFactory {

  public TestComponent create(String pFirst, int pSecond) {
    List<String> result = new ArrayList<>();
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    TestComponent inst = new TestComponent();
    inst.setResult(result);
    return inst;
  }

  public TestComponent createWithParamType(String pFirst, int pSecond) {
    List<String> result = new ArrayList<>();
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    TestComponent inst = new TestComponent();
    inst.setResult(result);
    return inst;
  }

  public TestComponent createWithComponentParamType(String pFirst, int pSecond, PropertiesComponent pComponent) {
    List<String> result = new ArrayList<>();
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    result.add(Boolean.toString(!Strings.isNullOrEmpty(pComponent.getPropString())));
    TestComponent inst = new TestComponent();
    inst.setResult(result);
    return inst;
  }
}
