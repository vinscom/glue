package vinscom.ioc;

import vinscom.ioc.common.PropertyContext;
import vinscom.ioc.common.Tuple;
import vinscom.ioc.common.Constant;
import vinscom.ioc.common.Util;
import vinscom.ioc.enumeration.ComponentScopeType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vinscom.ioc.annotation.StartService;

public class ComponentManager extends PropertiesHolder implements Glue {

  protected Logger logger = LogManager.getLogger(ComponentManager.class.getCanonicalName());
  private static ComponentManager mSelf = null;
  private final Map<String, Object> mSingletonRepository;
  private final Deque<PropertyContext> mPropertyStack;

  public ComponentManager() {
    mSingletonRepository = new HashMap<>();
    mPropertyStack = new ArrayDeque<>();
  }

  protected synchronized Object resolve(String pPath, Properties pProperties) {

    logger.debug(() -> "Component[" + pPath + "]:Loading");
    Tuple<Boolean, Object> instance = getInstance(pPath, pProperties);

    boolean isNewInstance = instance.value1;
    Object objInstance = instance.value2;

    logger.debug(() -> "Component[" + pPath + "]:Got instance isNewInstance[" + isNewInstance + "]");

    if (!isNewInstance) {
      return objInstance;
    }

    logger.debug(() -> "Component[" + pPath + "]:Loading properties");
    loadPropertiesInStack(objInstance, pProperties, pPath);
    processPropertyStack();

    logger.debug(() -> "Component[" + pPath + "]:Loading Finished");
    return objInstance;
  }

  protected void processPropertyStack() {
    try {
      while (!mPropertyStack.isEmpty()) {
        PropertyContext propCtx = mPropertyStack.pop();
        processProperty(propCtx);
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      java.util.logging.Logger.getLogger(ComponentManager.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected void processProperty(PropertyContext pPropCtx) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    ValueProxy v = pPropCtx.getValue();

    if (v != null) {

      if (v.isStage1ProcessingRequired()) {
        v.processStage1();
        String defCompPath = v.getDeferredComponentPath();
        if (getPropertiesCache().containsKey(defCompPath)) {
          v.setDeferredValue(true);
          Tuple<Boolean, Object> instance = getInstance(defCompPath, getPropertiesCache().get(defCompPath));
          v.setDeferredComponent(instance.value2);
          mPropertyStack.push(pPropCtx);
          if (instance.value1) {
            loadPropertiesInStack(instance.value2, getPropertiesCache().get(defCompPath), defCompPath);
          }
          return;
        }
      }

      if (v.isStage2ProcessingRequired()) {
        v.processStage2();
        pPropCtx.getMethod().invoke(pPropCtx.getInstance(), v.getValue());
      }

    } else {
      pPropCtx.getMethod().invoke(pPropCtx.getInstance());
    }

  }

  protected void loadPropertiesInStack(Object pInstance, Properties pProperties, String pComponentPath) {

    Method startupmethod = Util.getMethodWithAnnotation(pInstance.getClass(), StartService.class);

    if (startupmethod != null) {
      PropertyContext propCtx = new PropertyContext();
      propCtx.setInstance(pInstance);
      propCtx.setMethod(startupmethod);
      propCtx.setValue(null);
      propCtx.setComponentPath(pComponentPath);
      logger.debug(() -> "Component[" + pComponentPath + "]:Found start service method:" + propCtx.getMethod().getName());
      mPropertyStack.push(propCtx);
    }

    pProperties
            .entrySet()
            .stream()
            .filter((t) -> !(Constant.Component.CLASS.equals(t.getKey()) || Constant.Component.SCOPE.equals(t.getKey())))
            .map((entry) -> {
              PropertyContext propCtx = new PropertyContext();
              propCtx.setInstance(pInstance);
              propCtx.setMethod(Util.getMethod(pInstance.getClass(), Util.buildSetPropertyName((String) entry.getKey())));
              ValueProxy v = new ValueProxy(Util.getMethodFirstArgumentClass(propCtx.getMethod()), (String) entry.getValue(), pComponentPath);
              propCtx.setValue(v);
              propCtx.setComponentPath(pComponentPath);
              return propCtx;
            })
            .forEachOrdered((propertyCtx) -> {
              logger.debug(() -> "Component[" + pComponentPath + "]:Ready to process:" + propertyCtx.getMethod().getName() + ", Value=" + propertyCtx.getValue());
              mPropertyStack.push(propertyCtx);
            });

  }

  /**
   *
   * @param pPath
   * @param pProperties
   * @return Returns Tuple where value1 = true if new Object is created. Or else false
   */
  protected Tuple<Boolean, Object> getInstance(String pPath, Properties pProperties) {

    String clazz = pProperties.getProperty(Constant.Component.CLASS);
    ComponentScopeType scope = ComponentScopeType.valueOf(pProperties.getProperty(Constant.Component.SCOPE, ComponentScopeType.GLOBAL.toString()));

    logger.debug(() -> "Component[" + pPath + "]:Class=" + clazz);
    logger.debug(() -> "Component[" + pPath + "]:Scope=" + scope);

    if (ComponentScopeType.GLOBAL == scope) {
      Map<String, Object> repo = getSingletonRepository();
      if (repo.containsKey(pPath)) {
        logger.debug(() -> "Component[" + pPath + "]:Singleton already initialised");
        return new Tuple<>(false, repo.get(pPath));
      }
    }

    logger.debug(() -> "Component[" + pPath + "]:Creating instance");
    Object instance = Util.createInstance(clazz);

    if (ComponentScopeType.GLOBAL == scope) {
      logger.debug(() -> "Component[" + pPath + "]:Adding instance to singleton repository");
      getSingletonRepository().put(pPath, instance);
    }

    return new Tuple<>(true, instance);
  }

  @Override
  public Object resolve(String pPath) {
    return resolve(pPath, getPropertiesCache().get(pPath));
  }

  @Override
  public <T> T resolve(String pPath, Class<T> pClass) {
    return pClass.cast(resolve(pPath));
  }

  public static ComponentManager instance() {
    return instance(null);
  }

  public synchronized static ComponentManager instance(List<String> pLayers) {
    if (mSelf != null) {
      return mSelf;
    }

    mSelf = new ComponentManager();

    if (pLayers == null) {
      mSelf.setLayers(Util.getSystemLayers());
    } else {
      mSelf.setLayers(pLayers);
    }

    mSelf.init().await();
    return mSelf;
  }

  protected Map<String, Object> getSingletonRepository() {
    return mSingletonRepository;
  }

  protected Deque<PropertyContext> getPropertyStack() {
    return mPropertyStack;
  }

}
