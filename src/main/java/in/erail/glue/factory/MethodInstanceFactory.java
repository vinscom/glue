package in.erail.glue.factory;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import in.erail.glue.Glue;
import in.erail.glue.InstanceFactory;
import in.erail.glue.ValueProxy;
import in.erail.glue.ValueProxyBuilder;
import in.erail.glue.annotation.StartService;
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
import java.util.logging.Level;
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
  private Method mMethod;
  private Object[] mMethodParam;
  private Class mMethodClass;
  private Object mMethodClassInstance;

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

  @StartService
  public void start() {

    if (!isFactoryEnable()) {
      return;
    }

    getLog().debug(() -> this);

    try {
      if (!Strings.isNullOrEmpty(getFactoryClass())) {
        mMethodClass = Class.forName(getFactoryClass());
      } else if (!Strings.isNullOrEmpty(getFactoryInstance())) {
        mMethodClassInstance = Glue.instance().resolve(getFactoryInstance());
        mMethodClass = mMethodClassInstance.getClass();
      } else {
        getLog().error("Not able to create instance" + this);
        return;
      }

      Method defaultMethod = null;

      all_methods:
      for (Method m : mMethodClass.getMethods()) {

        if (!m.getName().equals(getFactoryMethodName())) {
          continue;
        }

        if (defaultMethod == null) {
          defaultMethod = m;
        }

        Class[] pType = m.getParameterTypes();

        if (getParamType().length == pType.length) {
          for (int i = 0; i < pType.length; i++) {
            if (!pType[i].equals(getParamType()[i])) {
              continue all_methods;
            }
          }
          mMethod = m;
          break;
        }
      }

      if (mMethod == null) {
        mMethod = defaultMethod;
      }

      if (mMethodClassInstance == null && mMethod != null && !Modifier.isStatic(mMethod.getModifiers())) {
        mMethodClassInstance = mMethodClass.getDeclaredConstructor().newInstance();
      }

      if (mMethod == null) {
        mMethodParam = new Object[0];
      } else {
        mMethodParam = getFactoryMethodParams(getFactoryParamValues(), mMethod);
      }
    } catch (IllegalArgumentException
            | ClassNotFoundException
            | SecurityException
            | InstantiationException
            | IllegalAccessException 
            | NoSuchMethodException 
            | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Optional createInstance() {

    if (!isFactoryEnable() || mMethod == null) {
      return Optional.empty();
    }

    Object instance = null;

    try {
      instance = mMethod.invoke(mMethodClassInstance, mMethodParam);
    } catch (IllegalAccessException
            | IllegalArgumentException
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
      ValueProxy vp = ValueProxyBuilder
              .newBuilder()
              .setTargetClass(paramsType[i])
              .setPropertyValue(vmc)
              .setComponentPath(getComponentPath())
              .build();
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

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper(this)
            .add("mFactoryClass", mFactoryClass)
            .add("mFactoryInstance", mFactoryInstance)
            .add("mFactoryMethodName", mFactoryMethodName)
            .add("mFactoryParamValues", mFactoryParamValues != null ? Joiner.on(",").join(mFactoryParamValues) : null)
            .add("mFactoryParamType", mFactoryParamType != null ? Joiner.on(",").join(mFactoryParamType) : null)
            .add("mParamType", mParamType != null ? Joiner.on(",").join(mParamType) : null)
            .add("mComponentPath", mComponentPath)
            .add("mFactoryEnable", mFactoryEnable)
            .add("mMethod", mMethod != null ? mMethod.getName() : null)
            .add("mMethodParam", mMethodParam != null ? Joiner.on(",").join(mMethodParam) : null)
            .add("mMethodClass", mMethodClass != null ? mMethodClass.getCanonicalName() : null)
            .add("mMethodClassInstance", mMethodClassInstance != null ? mMethodClassInstance.getClass().toGenericString() : null)
            .toString();
  }

}
