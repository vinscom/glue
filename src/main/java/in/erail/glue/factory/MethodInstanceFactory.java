package in.erail.glue.factory;

import com.google.common.base.Strings;
import in.erail.glue.Glue;
import in.erail.glue.InstanceFactory;
import in.erail.glue.ValueProxy;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.enumeration.PropertyValueModifier;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
public class MethodInstanceFactory implements InstanceFactory {

  private String mFactoryClass;
  private String mFactoryInstance;
  private String mFactoryMethodName;
  private String[] mFactoryParamValues;
  private String[] mFactoryParamType;
  private Logger mLog;
  private Class[] mParamType;
  private String mComponentPath;
  private boolean mFactoryEnable = true;

  private final Map<String, Class> mPrimitiveType = new HashMap<>();

  public MethodInstanceFactory() {
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
  public Optional createInstance() {

    if(!isFactoryEnable()){
      return Optional.empty();
    }
    
    Object instance = null;
    Class clazz;
    Method method = null;
    Object clzzInstance = null;

    try {
      if (!Strings.isNullOrEmpty(getFactoryClass())) {
        clazz = Class.forName(getFactoryClass());
      } else if (!Strings.isNullOrEmpty(getFactoryInstance())) {
        clzzInstance = Glue.instance().resolve(getFactoryInstance());
        clazz = clzzInstance.getClass();
      } else {
        getLog().error("Not able to create instance");
        return Optional.empty();
      }

      if (getParamType().length == 0) {
        for (Method m : clazz.getMethods()) {
          if (m.getName().equals(getFactoryMethodName())) {
            method = m;
            break;
          }
        }
      } else {
        method = clazz.getMethod(getFactoryMethodName(), getParamType());
      }
      
      if (clzzInstance == null && method != null && !Modifier.isStatic(method.getModifiers())) {
        clzzInstance = clazz.newInstance();
      }

      Object[] params = getFactoryMethodParams(getFactoryParamValues(), method);
      instance = method.invoke(clzzInstance, params);

    } catch (IllegalAccessException
            | IllegalArgumentException
            | ClassNotFoundException
            | InstantiationException
            | NoSuchMethodException
            | InvocationTargetException
            | SecurityException ex) {
      throw new RuntimeException(ex);
    }

    return Optional.ofNullable(instance);
  }

  protected Optional<Method> findFactoryMethod(Class pClazz, String pMethodName, Class[] pParamsType) {

    Method method = null;

    if (pParamsType.length == 0) {
      for (Method m : pClazz.getMethods()) {
        if (m.getName().equals(pMethodName)) {
          method = m;
          break;
        }
      }
    } else {
      try {
        method = pClazz.getMethod(getFactoryMethodName(), pParamsType);
      } catch (NoSuchMethodException | SecurityException ex) {
        getLog().error(ex);
      }
    }

    return Optional.ofNullable(method);
  }

  protected Class[] getFactoryParamType(String[] pParamsType) {

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

  protected Object[] getFactoryMethodParams(String[] pParams, Method pMethod) {

    Class[] paramsType = pMethod.getParameterTypes();
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

  public String getFactoryClass() {
    return mFactoryClass;
  }

  public void setFactoryClass(String pFactoryClass) {
    this.mFactoryClass = pFactoryClass;
  }

  public String getFactoryInstance() {
    return mFactoryInstance;
  }

  public void setFactoryInstance(String pFactoryInstance) {
    this.mFactoryInstance = pFactoryInstance;
  }

  public String getFactoryMethodName() {
    return mFactoryMethodName;
  }

  public void setFactoryMethodName(String pFactoryMethodName) {
    this.mFactoryMethodName = pFactoryMethodName;
  }

  public String[] getFactoryParamValues() {
    return mFactoryParamValues;
  }

  public void setFactoryParamValues(String[] pFactoryParamValues) {
    this.mFactoryParamValues = pFactoryParamValues;
  }

  public String[] getFactoryParamType() {
    return mFactoryParamType;
  }

  public void setFactoryParamType(String[] pFactoryParamType) {
    this.mFactoryParamType = pFactoryParamType;
    this.mParamType = getFactoryParamType(pFactoryParamType);
  }

  public Logger getLog() {
    return mLog;
  }

  public void setLog(Logger pLog) {
    this.mLog = pLog;
  }

  public Class[] getParamType() {
    if (mParamType == null) {
      mParamType = new Class[]{};
    }
    return mParamType;
  }

  public String getComponentPath() {
    return mComponentPath;
  }

  public void setComponentPath(String pComponentPath) {
    this.mComponentPath = pComponentPath;
  }

  public boolean isFactoryEnable() {
    return mFactoryEnable;
  }

  public void setFactoryEnable(boolean pFactoryEnable) {
    this.mFactoryEnable = pFactoryEnable;
  }

}
