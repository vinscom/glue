package in.erail.glue.factory;

import com.google.common.base.Strings;
import in.erail.glue.component.PropertiesComponent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vinay
 */
public class TestComponent2 {

  private List<String> result = new ArrayList<>();

  public TestComponent2(String pFirst, int pSecond, PropertiesComponent pComponent,List<String> pList) {
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    result.add(Boolean.toString(!Strings.isNullOrEmpty(pComponent.getPropString())));
    result.addAll(pList);
  }

  public List<String> getResult() {
    return result;
  }

  public void setResult(List<String> pResult) {
    this.result = pResult;
  }

}
