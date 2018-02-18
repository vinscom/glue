package in.erail.glue.factory;

import com.google.common.base.Strings;
import in.erail.glue.component.PropertiesComponent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vinay
 */
public class TestComponent {

  private List<String> result = new ArrayList<>();

  public List<String> getResult() {
    return result;
  }

  public void setResult(List<String> pResult) {
    this.result = pResult;
  }

}
