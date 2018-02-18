package in.erail.glue.factory;

import in.erail.glue.Glue;
import in.erail.glue.InstanceFactory;
import in.erail.glue.ValueProxy;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.enumeration.PropertyValueModifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author vinay
 */
public class ParameterConstructorInstanceFactory implements InstanceFactory {

  private String mBaseClass;
  private String[] mConstructorParamValues;
  private String[] mConstructorParamType;
  private Class[] mParamType;
  private Logger mLog;
  private final Map<String, Class> mPrimitiveType = new HashMap<>();

  public ParameterConstructorInstanceFactory() {
    mPrimitiveType.put("byte.class", byte.class);
    mPrimitiveType.put("short.class", short.class);
    mPrimitiveType.put("int.class", int.class);
    mPrimitiveType.put("long.class", long.class);
    mPrimitiveType.put("float.class", float.class);
    mPrimitiveType.put("double.class", double.class);
    mPrimitiveType.put("boolean.class", boolean.class);
    mPrimitiveType.put("char.class", char.class);
  }

  @Override
  public Optional<Object> createInstance() {

    Object instance = null;

    try {
      Class clazz = Class.forName(getBaseClass());
      Constructor constructor = null;
      if (getParamType()!=null && getParamType().length == 0) {
        for (Constructor c : clazz.getConstructors()) {
          constructor = c;
          break;
        }
      } else {
        if(clazz.getConstructors().length > 0){
          constructor = clazz.getConstructors()[0];
        }
      }

      if(Optional.ofNullable(constructor).isPresent()){
        instance = constructor.newInstance(getConstructorMethodParams(getConstructorParamValues(), constructor));
      }      
      
    } catch (ClassNotFoundException
            | SecurityException
            | InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException ex) {
      getLog().error(ex);
    }

    return Optional.ofNullable(instance);
  }

  protected Object[] getConstructorMethodParams(String[] pParams, Constructor pConstructor) {

    Class[] paramsType = pConstructor.getParameterTypes();
    Object[] params = new Object[paramsType.length];

    for (int i = 0; i < paramsType.length; i++) {
      ValueWithModifier vm = new ValueWithModifier(pParams[i], PropertyValueModifier.NONE);
      List<ValueWithModifier> vmc = new ArrayList<>(1);
      vmc.add(vm);
      ValueProxy vp = new ValueProxy(paramsType[i], vmc, pParams[i]);
      vp.process();
      if (vp.isDeferredValue()) {
        params[i] = Glue.instance().resolve(vp.getDeferredComponentPath());
      } else {
        params[i] = vp.getValue();
      }
    }

    return params;
  }

  protected Class[] getConstructorParamType(String[] pParamsType) {

    Class[] result;

    if (pParamsType != null) {
      try {
        result = new Class[pParamsType.length];
        for (int i = 0; i < pParamsType.length; i++) {
          String clazz = pParamsType[i];
          if (mPrimitiveType.containsKey(clazz)) {
            result[i] = mPrimitiveType.get(clazz);
          } else {
            result[i] = Class.forName(clazz);
          }
        }
      } catch (ClassNotFoundException ex) {
        result = new Class[]{};
        getLog().error(ex);
      }
    } else {
      result = new Class[]{};
    }

    return result;
  }

  public String getBaseClass() {
    return mBaseClass;
  }

  public void setBaseClass(String pBaseClass) {
    this.mBaseClass = pBaseClass;
  }

  public String[] getConstructorParamValues() {
    return mConstructorParamValues;
  }

  public void setConstructorParamValues(String[] pConstructorParamValues) {
    this.mConstructorParamValues = pConstructorParamValues;
  }

  public String[] getConstructorParamType() {
    return mConstructorParamType;
  }

  public void setConstructorParamType(String[] pConstructorParamType) {
    this.mConstructorParamType = pConstructorParamType;
    this.mParamType = getConstructorParamType(pConstructorParamType);
  }

  public Logger getLog() {
    return mLog;
  }

  public void setLog(Logger pLog) {
    this.mLog = pLog;
  }

  public Class[] getParamType() {
    return mParamType;
  }

  public void setParamType(Class[] pParamType) {
    this.mParamType = pParamType;
  }

}
