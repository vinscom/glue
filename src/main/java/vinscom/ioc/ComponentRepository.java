package vinscom.ioc;

import com.google.common.collect.ListMultimap;
import vinscom.ioc.common.PropertyContext;
import vinscom.ioc.common.Tuple;
import vinscom.ioc.common.Constant;
import vinscom.ioc.common.Util;
import vinscom.ioc.enumeration.ComponentScopeType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vinscom.ioc.annotation.StartService;
import vinscom.ioc.common.ValueWithModifier;

public class ComponentRepository implements Glue {

  private static final PropertiesRepository mPropertiesRepository;
  private static final Map<String, Object> mSingletonRepository;

  static {
    mSingletonRepository = new ConcurrentHashMap<>();
    mPropertiesRepository = new PropertiesRepository();
    mPropertiesRepository.setLayers(Util.getSystemLayers());
    mPropertiesRepository.init().await();
  }

  protected Logger logger = LogManager.getLogger(ComponentRepository.class.getCanonicalName());

  protected synchronized Object resolve(String pPath, ListMultimap<String, ValueWithModifier> pProperties) {

    ArrayDeque<PropertyContext> propertyStack = new ArrayDeque<>();
    logger.debug(() -> "Component[" + pPath + "]:Loading");
    Tuple<Boolean, Object> instance = getInstance(pPath, pProperties);

    boolean isNewInstance = instance.value1;
    Object objInstance = instance.value2;

    logger.debug(() -> "Component[" + pPath + "]:Got instance isNewInstance[" + isNewInstance + "]");

    if (!isNewInstance) {
      return objInstance;
    }

    logger.debug(() -> "Component[" + pPath + "]:Loading properties");
    loadPropertiesInStack(objInstance, pProperties, pPath, propertyStack);
    processPropertyStack(propertyStack);

    logger.debug(() -> "Component[" + pPath + "]:Loading Finished");
    return objInstance;
  }

  protected void processPropertyStack(Deque<PropertyContext> pPropertyStack) {
    try {
      while (!pPropertyStack.isEmpty()) {
        PropertyContext propCtx = pPropertyStack.pop();
        logger.debug(() -> "Component[" + propCtx.getComponentPath() + "]:Processing property " + propCtx.getMethod().getName());
        processProperty(propCtx, pPropertyStack);
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      java.util.logging.Logger.getLogger(ComponentRepository.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected void processProperty(PropertyContext pPropCtx, Deque<PropertyContext> pPropertyStack) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    ValueProxy v = pPropCtx.getValue();

    if (v != null) {

      if (v.isDeferredValue() && v.getDeferredComponent() == null) {
        Tuple<Boolean, Object> instance = getInstance(v.getDeferredComponentPath(), getPropertiesCache().get(v.getDeferredComponentPath()));
        v.setDeferredComponent(instance.value2);
        pPropertyStack.push(pPropCtx);
        if (instance.value1) {
          loadPropertiesInStack(instance.value2, getPropertiesCache().get(v.getDeferredComponentPath()), v.getDeferredComponentPath(), pPropertyStack);
        }
        return;
      }

      v.process();
      pPropCtx.getMethod().invoke(pPropCtx.getInstance(), v.getValue());

    } else {
      pPropCtx.getMethod().invoke(pPropCtx.getInstance());
    }

  }

  protected void loadPropertiesInStack(Object pInstance, ListMultimap<String, ValueWithModifier> pProperties, String pComponentPath, Deque<PropertyContext> pPropertyStack) {

    Method startupmethod = Util.getMethodWithAnnotation(pInstance.getClass(), StartService.class);

    if (startupmethod != null) {
      PropertyContext propCtx = new PropertyContext();
      propCtx.setInstance(pInstance);
      propCtx.setMethod(startupmethod);
      propCtx.setValue(null);
      propCtx.setComponentPath(pComponentPath);
      logger.debug(() -> "Component[" + pComponentPath + "]:Found start service method:" + propCtx.getMethod().getName());
      pPropertyStack.push(propCtx);
    }

    pProperties
            .asMap()
            .entrySet()
            .stream()
            .filter((t) -> !(Constant.Component.CLASS.equals(t.getKey()) || Constant.Component.SCOPE.equals(t.getKey())))
            .map((entry) -> {
              PropertyContext propCtx = new PropertyContext();
              propCtx.setInstance(pInstance);
              propCtx.setMethod(Util.getMethod(pInstance.getClass(), Util.buildSetPropertyName((String) entry.getKey())));
              ValueProxy v = new ValueProxy(Util.getMethodFirstArgumentClass(propCtx.getMethod()), entry.getValue(), pComponentPath);
              propCtx.setValue(v);
              propCtx.setComponentPath(pComponentPath);
              return propCtx;
            })
            .forEachOrdered((propertyCtx) -> {
              logger.debug(() -> "Component[" + pComponentPath + "]:Ready to process:" + propertyCtx.getMethod().getName() + ", Value=" + propertyCtx.getValue());
              pPropertyStack.push(propertyCtx);
            });

  }

  /**
   *
   * @param pPath
   * @param pProperties
   * @return Returns Tuple where value1 = true if new Object is created. Or else false
   */
  protected synchronized Tuple<Boolean, Object> getInstance(String pPath, ListMultimap<String, ValueWithModifier> pProperties) {

    String clazz = Util.getLastValue(pProperties, Constant.Component.CLASS);
    ComponentScopeType scope = ComponentScopeType.valueOf(Util.getLastValue(pProperties, Constant.Component.SCOPE, ComponentScopeType.GLOBAL.toString()));

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

  public static ComponentRepository instance() {
    return new ComponentRepository();
  }

  protected Map<String, Object> getSingletonRepository() {
    return mSingletonRepository;
  }

  public static Map<String, ListMultimap<String, ValueWithModifier>> getPropertiesCache() {
    return mPropertiesRepository.getPropertiesCache();
  }
}
