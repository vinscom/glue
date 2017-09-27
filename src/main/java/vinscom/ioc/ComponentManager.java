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

import vinscom.ioc.annotation.StartService;
import vinscom.ioc.enumeration.MethodArgumentType;

public class ComponentManager extends PropertiesHolder {

  private static ComponentManager self = null;
  private final Map<String, Object> mSingletonRepository;
  private final Deque<PropertyContext> mPropertyStack;

  public ComponentManager() {
    mSingletonRepository = new HashMap<>();
    mPropertyStack = new ArrayDeque<>();
  }

  protected Object resolve(String pPath, Properties pProperties) {

    Tuple<Boolean, Object> instance = getInstance(pPath, pProperties);

    boolean isNewInstance = instance.value1;
    Object objInstance = instance.value2;

    if (!isNewInstance) {
      return objInstance;
    }

    loadPropertiesInStack(objInstance, pProperties);
    processPropertyStack();

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
        case MAP:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), Util.getMapFromValue(pPropCtx.getValue()));
          break;
        case COMPONENT:
          Tuple<Boolean, Object> instance = getInstance(pPropCtx.getValue(), getPropertiesCache().get(pPropCtx.getValue()));
          pPropCtx.getMethod().invoke(pPropCtx.getInstance(), instance.value2);
          if (instance.value1) {
            loadPropertiesInStack(instance.value2, getPropertiesCache().get(pPropCtx.getValue()));
          }
          break;
        case NONE:
          pPropCtx.getMethod().invoke(pPropCtx.getInstance());
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }

  }

  protected void loadPropertiesInStack(Object pInstance, Properties pProperties) {

    Method startupmethod = Util.getMethodWithAnnotation(pInstance.getClass(), StartService.class);

    if (startupmethod != null) {
      PropertyContext propCtx = new PropertyContext();
      propCtx.setInstance(pInstance);
      propCtx.setMethod(startupmethod);
      propCtx.setValue(null);
      propCtx.setMethodArgumentType(MethodArgumentType.NONE);
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
              return propCtx;
            })
            .forEachOrdered((propertyInfo) -> {
              mPropertyStack.push(propertyInfo);
            });

  }

  /**
   *
   * @param pPath
   * @param pProperties
   * @return Returns Tuple where value1 = true if new Object is created. Or else false
   */
  protected Tuple<Boolean, Object> getInstance(String pPath, Properties pProperties) {

    String pClass = pProperties.getProperty(Constant.Component.CLASS);
    ComponentScopeType pScope = ComponentScopeType.valueOf(pProperties.getProperty(Constant.Component.SCOPE, ComponentScopeType.GLOBAL.toString()));

    if (ComponentScopeType.GLOBAL == pScope) {
      Map<String, Object> repo = getSingletonRepository();
      if (repo.containsKey(pPath)) {
        return new Tuple<>(false, repo.get(pPath));
      }
    }

    Object instance = Util.createInstance(pClass);

    if (ComponentScopeType.GLOBAL == pScope) {
      getSingletonRepository().put(pPath, instance);
    }

    return new Tuple<>(true, instance);
  }

  public Object resolve(String pPath) {
    return resolve(pPath, getPropertiesCache().get(pPath));
  }

  public <T> T resolve(String pPath, Class<T> pClass) {
    return pClass.cast(resolve(pPath));
  }

  public synchronized static ComponentManager instance() {
    return self;
  }
  
  public synchronized static ComponentManager instance(List<String> pLayers) {
    if (self != null) {
      return self;
    }

    self = new ComponentManager();
    self.setLayers(pLayers);
    self.init().await();
    return self;
  }

  protected Map<String, Object> getSingletonRepository() {
    return mSingletonRepository;
  }

  protected Deque<PropertyContext> getPropertyStack() {
    return mPropertyStack;
  }

}
