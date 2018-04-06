package in.erail.glue.factory;

import in.erail.glue.Glue;
import in.erail.glue.InstanceFactory;
import in.erail.glue.ValueProxy;
import in.erail.glue.annotation.StartService;
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
  private String mComponentPath;
  private boolean mConstructorEnable = true;
  private Constructor mConstructor = null;
  private Object[] mConstructorParam;

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

  @StartService
  public void start() {

    if (!isConstructorEnable()) {
      return;
    }

    try {
      Class clazz = Class.forName(getBaseClass());

      Constructor defaultConstructor = null;

      all_constructors:
      for (Constructor c : clazz.getConstructors()) {

        if (defaultConstructor == null) {
          defaultConstructor = c;
        }

        Class[] pType = c.getParameterTypes();

        if (getParamType().length == pType.length) {
          for (int i = 0; i < pType.length; i++) {
            if (!pType[i].equals(getParamType()[i])) {
              continue all_constructors;
            }
          }
          mConstructor = c;
          break;
        }
      }

      if (mConstructor == null) {
        mConstructor = defaultConstructor;
      }

      if (mConstructor == null) {
        mConstructorParam = new Object[0];
      } else {
        mConstructorParam = getConstructorMethodParams(getConstructorParamValues(), mConstructor);
      }

    } catch (ClassNotFoundException | SecurityException ex) {
      getLog().error(ex);
    }

  }

  @Override
  public Optional<Object> createInstance() {

    if (!isConstructorEnable() || mConstructor == null) {
      return Optional.empty();
    }

    Object instance = null;

    try {
      instance = mConstructor.newInstance(mConstructorParam);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
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
      ValueProxy vp = new ValueProxy(paramsType[i], vmc, getComponentPath());
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
    if (mParamType == null) {
      mParamType = new Class[0];
    }
    return mParamType;
  }

  public void setParamType(Class[] pParamType) {
    this.mParamType = pParamType;
  }

  public String getComponentPath() {
    return mComponentPath;
  }

  public void setComponentPath(String pComponentPath) {
    this.mComponentPath = pComponentPath;
  }

  public boolean isConstructorEnable() {
    return mConstructorEnable;
  }

  public void setConstructorEnable(boolean pConstructorEnable) {
    this.mConstructorEnable = pConstructorEnable;
  }

}
