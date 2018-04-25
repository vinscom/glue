package in.erail.glue;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ListMultimap;
import in.erail.glue.common.PropertyContext;
import in.erail.glue.common.Tuple;
import in.erail.glue.common.Constant;
import in.erail.glue.common.Util;
import in.erail.glue.enumeration.ComponentScopeType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.erail.glue.annotation.StartService;
import in.erail.glue.common.ValueWithModifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

public class ComponentRepository implements Glue {

  private static final PropertiesRepository mPropertiesRepository;
  private static final Map<String, Object> mSingletonRepository;

  static {
    PropertiesRepository.setLayers(Util.getSystemLayers());
    mSingletonRepository = new HashMap<>();
    mPropertiesRepository = new PropertiesRepository();
    mPropertiesRepository.init();
  }

  protected Logger logger = LogManager.getLogger(ComponentRepository.class.getCanonicalName());

  protected synchronized Object resolve(String pPath, ListMultimap<String, ValueWithModifier> pProperties) {

    ArrayDeque<PropertyContext> propertyStack = new ArrayDeque<>();
    logger.debug(() -> "Component[" + pPath + "]:Loading");
    Tuple<Boolean, Object> instance = getInstance(pPath, pProperties);

    boolean isNewInstance = instance.value1;
    Object objInstance = instance.value2;

    logger.debug(() -> "Component[" + pPath + "]:Got instance isNewInstance[" + isNewInstance + "]");

    if (!isNewInstance || objInstance == null) {
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

      if (v.isDeferredValue() && !v.isDeferredComponentProcessed()) {
        logger.debug(() -> "Component[" + pPropCtx.getComponentPath() + "]: Property referes to another component. Processing " + v.getDeferredComponentPath());
        Tuple<Boolean, Object> instance = getInstance(v.getDeferredComponentPath(), getPropertiesCache().get(v.getDeferredComponentPath()));

        boolean isNewInstance = instance.value1;
        Object objInstance = instance.value2;

        v.setDeferredComponent(objInstance);
        v.setDeferredComponentProcessed(true);
        pPropertyStack.push(pPropCtx);

        if (isNewInstance && objInstance != null) {
          loadPropertiesInStack(objInstance, getPropertiesCache().get(v.getDeferredComponentPath()), v.getDeferredComponentPath(), pPropertyStack);
        }

        return;
      }

      v.process();
      logger.debug(() -> "Component[" + pPropCtx.getComponentPath() + "]: Property :" + pPropCtx.getMethod().getName() + ", Value derived :" + v.getValue());
      if (v.getValue() != null && pPropCtx.getMethod().isVarArgs() && v.getValue().getClass().isArray()) {
        Object[] varParam = (Object[]) v.getValue();
        pPropCtx.getMethod().invoke(pPropCtx.getInstance(), (Object) Arrays.copyOf(varParam, varParam.length, v.getTargetClass()));
      } else if (v.getValue() != null && v.getValue().getClass().isArray()) {
        Object[] varParam = (Object[]) v.getValue();
        pPropCtx.getMethod().invoke(pPropCtx.getInstance(), new Object[]{Arrays.copyOf(varParam, varParam.length, v.getTargetClass())});
      } else {
        pPropCtx.getMethod().invoke(pPropCtx.getInstance(), v.getValue());
      }

    } else {
      logger.debug(() -> "Component[" + pPropCtx.getComponentPath() + "]: Invoking Property without arguments :" + pPropCtx.getMethod().getName());
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
            .filter((t) -> !t.getKey().startsWith(Constant.Component.SPECIAL_PROPERTY))
            .filter((t) -> Util.getMethod(pInstance.getClass(), Util.buildSetPropertyName((String) t.getKey())) != null)
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
   * @param pPath Component Path
   * @param pProperties Properties loaded from properties file
   * @return Returns Tuple where value1 = true if new Object is created. Or else false
   */
  protected Tuple<Boolean, Object> getInstance(String pPath, ListMultimap<String, ValueWithModifier> pProperties) {

    Preconditions.checkNotNull(pProperties, "Property can't be null");

    ComponentScopeType scope = ComponentScopeType.valueOf(Util.getLastValue(pProperties, Constant.Component.SCOPE, ComponentScopeType.GLOBAL.toString()));
    logger.debug(() -> "Component[" + pPath + "]:Scope=" + scope);

    Tuple<Boolean, Object> result;

    synchronized (mSingletonRepository) {

      if (ComponentScopeType.GLOBAL == scope && mSingletonRepository.containsKey(pPath)) {
        logger.debug(() -> "Component[" + pPath + "]:Singleton already initialised");
        result = new Tuple<>(false, mSingletonRepository.get(pPath));
      } else {
        logger.debug(() -> "Component[" + pPath + "]:Creating instance");

        Object instance = null;
        String factoryPath = Util.getLastValue(pProperties, Constant.Component.INSTANCE_FACTORY);
        if (!Strings.isNullOrEmpty(factoryPath)) {
          logger.debug(() -> "Component[" + pPath + "]: Creating instance using factory=" + factoryPath);
          InstanceFactory factoryInst = Glue.instance().resolve(factoryPath);
          Optional<Object> inst = factoryInst.createInstance();
          if (inst.isPresent()) {
            instance = inst.get();
          } else {
            logger.debug(() -> "Component[" + pPath + "]: No instance create by factory of " + pPath);
          }
        } else {
          String clazz = Util.getLastValue(pProperties, Constant.Component.CLASS);
          logger.debug(() -> "Component[" + pPath + "]: Creating instance of Class=" + clazz);
          instance = Util.createInstance(clazz);
        }

        result = new Tuple<>(true, instance);

        if (ComponentScopeType.GLOBAL == scope) {
          logger.debug(() -> "Component[" + pPath + "]:Adding instance to singleton repository");
          mSingletonRepository.put(pPath, result.value2);
        }
      }

    }

    return result;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T resolve(String pPath) {
    return (T) resolve(pPath, getPropertiesCache().get(pPath));
  }

  public static ComponentRepository instance() {
    return new ComponentRepository();
  }

  public static Map<String, ListMultimap<String, ValueWithModifier>> getPropertiesCache() {
    return mPropertiesRepository.getPropertiesCache();
  }
}
