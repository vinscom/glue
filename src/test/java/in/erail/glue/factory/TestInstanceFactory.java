package in.erail.glue.factory;

import com.google.common.base.Strings;
import in.erail.glue.component.PropertiesComponent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author vinay
 */
public class TestInstanceFactory {

  public List<String> create(String pFirst,int pSecond){
    List<String> result = new ArrayList<>();
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    return result;
  }
  
  public List<String> createWithParamType(String pFirst,int pSecond){
    List<String> result = new ArrayList<>();
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    return result;
  }
  
  public List<String> createWithComponentParamType(String pFirst,int pSecond, PropertiesComponent pComponent){
    List<String> result = new ArrayList<>();
    result.add(pFirst);
    result.add(Integer.toString(pSecond));
    result.add(Boolean.toString(!Strings.isNullOrEmpty(pComponent.getPropString())));
    return result;
  }
}
