package vinscom.ioc;

import vinscom.ioc.common.PropertyInfo;
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

  private final Map<String, Object> mSingletonRepository;
  private final Deque<PropertyInfo> mPropertyStack;

  public ComponentManager() {
    mSingletonRepository = new HashMap<>();
    mPropertyStack = new ArrayDeque();
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

  protected void processProperty(PropertyInfo pPropertyInfo) {

    if (pPropertyInfo.getMethod() == null) {
      return;
    }

    try {
      switch (pPropertyInfo.getMethodArgumentType()) {
        case STRING:
          pPropertyInfo.getMethod().invoke(pPropertyInfo.getInstance(), pPropertyInfo.getValue());
          break;
        case ARRAY:
          pPropertyInfo.getMethod().invoke(pPropertyInfo.getInstance(), new Object[]{pPropertyInfo.getValue().split(",")});
          break;
        case LIST:
          pPropertyInfo.getMethod().invoke(pPropertyInfo.getInstance(), Arrays.asList((Object[]) pPropertyInfo.getValue().split(",")));
          break;
        case MAP:
          pPropertyInfo.getMethod().invoke(pPropertyInfo.getInstance(), Util.getMapFromValue(pPropertyInfo.getValue()));
          break;
        case COMPONENT:
          Tuple<Boolean, Object> instance = getInstance(pPropertyInfo.getValue(), getPropertiesCache().get(pPropertyInfo.getValue()));
          pPropertyInfo.getMethod().invoke(pPropertyInfo.getInstance(), instance.value2);
          if (instance.value1) {
            loadPropertiesInStack(instance.value2, getPropertiesCache().get(pPropertyInfo.getValue()));
          }
          break;
        case NONE:
          pPropertyInfo.getMethod().invoke(pPropertyInfo.getInstance());
      }
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      throw new RuntimeException(ex);
    }

  }

  protected void loadPropertiesInStack(Object pInstance, Properties pProperties) {

    Method startupmethod = Util.getMethodWithAnnotation(pInstance.getClass(), StartService.class);

    if (startupmethod != null) {
      PropertyInfo propertyInfo = new PropertyInfo();
      propertyInfo.setInstance(pInstance);
      propertyInfo.setMethod(startupmethod);
      propertyInfo.setValue(null);
      propertyInfo.setMethodArgumentType(MethodArgumentType.NONE);
      mPropertyStack.push(propertyInfo);
    }

    pProperties
            .entrySet()
            .stream()
            .filter((t) -> !(Constant.Component.CLASS.equals(t.getKey()) || Constant.Component.SCOPE.equals(t.getKey())))
            .map((entry) -> {
              PropertyInfo propertyInfo = new PropertyInfo();
              propertyInfo.setInstance(pInstance);
              propertyInfo.setMethod(Util.getMethod(pInstance.getClass(), Util.buildSetPropertyName((String) entry.getKey())));
              propertyInfo.setValue((String) entry.getValue());
              propertyInfo.setMethodArgumentType(Util.findMethodArgumentType(propertyInfo.getMethod()));
              return propertyInfo;
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

  public static ComponentManager create(List<String> pLayers) {
    ComponentManager repo = new ComponentManager();
    repo.setLayers(pLayers);
    repo.init().await();
    return repo;
  }

  protected Map<String, Object> getSingletonRepository() {
    return mSingletonRepository;
  }

  protected Deque getPropertyStack() {
    return mPropertyStack;
  }

}
