package vinscom.ioc;

import vinscom.ioc.common.PropertyContext;
import vinscom.ioc.common.Tuple;
import vinscom.ioc.common.Constant;
import vinscom.ioc.common.Util;
import vinscom.ioc.enumeration.ComponentScopeType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vinscom.ioc.annotation.StartService;
import vinscom.ioc.common.JsonLoader;
import vinscom.ioc.enumeration.MethodArgumentType;

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
    loadPropertiesInStack(objInstance, pProperties,pPath);
    processPropertyStack();

    logger.debug(() -> "Component[" + pPath + "]:Loading Finished");
    return objInstance;
  }

  protected void processPropertyStack() {
    while (!mPropertyStack.isEmpty()) {
      processProperty(mPropertyStack.pop());
    }
  }

  protected void processProperty(PropertyContext pPropCtx) {

    if (pPropCtx.getMethod() == null) {
      return;
    }

    logger.debug(() -> "Component[" + pPropCtx.getComponentPath() + "]:Processing " + pPropCtx.getMethod().getName() + pPropCtx.getMethodArgumentType().toString());
    
    try {
      switch (pPropCtx.getMethodArgumentType()) {
        case STRING:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), pPropCtx.getValue());
          break;
        case ARRAY:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), new Object[]{pPropCtx.getValue().split(",")});
          break;
        case LIST:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), Arrays.asList((Object[]) pPropCtx.getValue().split(",")));
          break;
        case BOOLEAN:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), Boolean.parseBoolean(pPropCtx.getValue()));
          break;
        case MAP:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), Util.getMapFromValue(pPropCtx.getValue()));
          break;
        case JSON:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), JsonLoader.load(pPropCtx.getComponentPath(), pPropCtx.getValue()));
          break;
        case COMPONENT:
          logger.debug(() -> "Component[" + pPropCtx.getComponentPath() + "]:Processing component property: " + pPropCtx.getValue());
          String componentPath = pPropCtx.getValue();
          Tuple<Boolean, Object> instance = getInstance(componentPath, getPropertiesCache().get(componentPath));
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), instance.value2);
          if (instance.value1) {
            loadPropertiesInStack(instance.value2, getPropertiesCache().get(componentPath),componentPath);
          }
          break;
        case NONE:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance());
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }

  }

  protected void loadPropertiesInStack(Object pInstance, Properties pProperties, String pComponentPath) {

    Method startupmethod = Util.getMethodWithAnnotation(pInstance.getClass(), StartService.class);

    if (startupmethod != null) {
      PropertyContext propCtx = new PropertyContext();
      propCtx.setInstance(pInstance);
      propCtx.setMethod(startupmethod);
      propCtx.setValue(null);
      propCtx.setMethodArgumentType(MethodArgumentType.NONE);
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
              propCtx.setValue((String) entry.getValue());
              propCtx.setMethodArgumentType(Util.findMethodArgumentType(propCtx.getMethod()));
              propCtx.setComponentPath(pComponentPath);
              return propCtx;
            })
            .forEachOrdered((propertyCtx) -> {
              logger.debug(() -> "Component[" + pComponentPath + "]:Ready to process:" + propertyCtx.getMethod().getName() + ", Value=" + propertyCtx.getValue() + "," + propertyCtx.getMethodArgumentType());
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

    logger.debug(() -> "Component[" + pPath + "]:Class=" +  clazz);
    logger.debug(() -> "Component[" + pPath + "]:Scope=" +  scope);
    
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
