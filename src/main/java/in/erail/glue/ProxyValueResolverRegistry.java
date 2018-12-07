package in.erail.glue;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import in.erail.glue.common.FileLoader;
import in.erail.glue.common.JsonLoader;
import in.erail.glue.common.Util;
import in.erail.glue.common.ValueWithModifier;
import in.erail.glue.component.ServiceMap;
import in.erail.glue.enumeration.PropertyValueModifier;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author vinay
 */
public class ProxyValueResolverRegistry {

  private static final Map<Class, BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object>> classToValue = new HashMap<>();

  public static Map<Class, BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object>> getClassToValue() {
    return classToValue;
  }

  static {
    //String
    classToValue.put(String.class, (t, u) -> {
      String v = Util.getLastValueWithModifier(t).getValue();
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      return v;
    });

    //JsonObject
    classToValue.put(JsonObject.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        return new JsonObject();
      }
      return JsonLoader.load(u.getComponentPath(), v);
    });

    //ServiceMap
    classToValue.put(ServiceMap.class, (t, u) -> {
      return new ServiceMap((Map) classToValue.get(Map.class).apply(t, u));
    });

    //Enum
    classToValue.put(Enum.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        //logger.error(componentPath + ":: Enum of type:" + getTargetClass().getCanonicalName() + " not set");
        return null;
      }
      return Enum.valueOf(u.getTargetClass(), v);
    });

    //boolean
    classToValue.put(boolean.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        //logger.error(componentPath + ":: boolean value can't be empty. Setting it to false");
        return false;
      }
      return Boolean.parseBoolean(v);
    });

    //Boolean
    classToValue.put(Boolean.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      return Boolean.valueOf(v);
    });

    //int
    classToValue.put(int.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        //logger.error(componentPath + ":: int value can't be empty. Setting it to 0");
        return 0;
      }
      return Integer.parseInt(v);
    });

    //Integer
    classToValue.put(Integer.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      return (Integer) classToValue.get(int.class).apply(t, u);
    });

    //long
    classToValue.put(long.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        //logger.error(componentPath + ":: long value can't be empty. Setting it to 0");
        return 0l;
      }
      return Long.parseLong(v);
    });

    //Long
    classToValue.put(Long.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      return (Long) classToValue.get(long.class).apply(t, u);
    });

    //File
    classToValue.put(File.class, (t, u) -> {
      return FileLoader.load(u.getComponentPath(), (String) classToValue.get(String.class).apply(t, u));
    });

    //Logger
    classToValue.put(Logger.class, (t, u) -> {
      String loggerName = u.getComponentPath();
      if (loggerName.startsWith("/") && loggerName.length() >= 2) {
        loggerName = loggerName.substring(1);
      }
      return LogManager.getLogger(loggerName.replace("/", "."));
    });

    //Pattern
    classToValue.put(Pattern.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      return Pattern.compile(v);
    });

    //Meter
    classToValue.put(Meter.class, (t, u) -> {
      return Util.getMetricRegistry().meter((String) classToValue.get(String.class).apply(t, u));
    });

    //Timer
    classToValue.put(Timer.class, (t, u) -> {
      return Util.getMetricRegistry().timer((String) classToValue.get(String.class).apply(t, u));
    });

    //Counter
    classToValue.put(Counter.class, (t, u) -> {
      return Util.getMetricRegistry().counter((String) classToValue.get(String.class).apply(t, u));
    });

    //Histogram
    classToValue.put(Histogram.class, (t, u) -> {
      return Util.getMetricRegistry().histogram((String) classToValue.get(String.class).apply(t, u));
    });

    //Class
    classToValue.put(Class.class, (t, u) -> {
      String v = (String) classToValue.get(String.class).apply(t, u);
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      try {
          return Class.forName(v);
      } catch (ClassNotFoundException ex) {
          java.util.logging.Logger.getLogger(ProxyValueResolverRegistry.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
    });
    
    //List
    createList(ArrayList.class);
    classToValue.put(List.class, createList(ArrayList.class));
    classToValue.put(Collection.class, createList(ArrayList.class));

    //Map
    createMap(HashMap.class);
    classToValue.put(Map.class, createMap(HashMap.class));

    //Set
    createSet(HashSet.class);
    classToValue.put(Set.class, createSet(HashSet.class));

  }

  public static BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> createList(Class pClazz) {

    if (classToValue.containsKey(pClazz)) {
      return classToValue.get(pClazz);
    }

    BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> fnNew = (t, u) -> {
      try {
        final List<String> result = (List<String>) pClazz.getDeclaredConstructor().newInstance();
        List<ValueWithModifier> v = (List<ValueWithModifier>) t;
        v.stream().forEach((vwm) -> {
          if (Strings.isNullOrEmpty(vwm.getValue())) {
            return;
          }
          List<String> l = Arrays.asList(Util.convertCSVIntoArray(vwm.getValue()));
          switch (vwm.getPropertyValueModifier()) {
            case PLUS:
              result.addAll(l);
              break;
            case MINUS:
              result.removeAll(l);
              break;
            default:
              result.clear();
              result.addAll(l);
          }
        });

        return result;
      } catch (InstantiationException
              | IllegalAccessException
              | IllegalArgumentException
              | InvocationTargetException
              | NoSuchMethodException
              | SecurityException ex) {
        java.util.logging.Logger.getLogger(ProxyValueResolverRegistry.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
    };

    BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> fnOld = classToValue.putIfAbsent(pClazz, fnNew);
    return fnOld == null ? fnNew : fnOld;
  }

  public static BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> createMap(Class pClazz) {

    if (classToValue.containsKey(pClazz)) {
      return classToValue.get(pClazz);
    }

    BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> fnNew = (t, u) -> {
      try {
        final Map<String, String> result = (Map<String, String>) pClazz.getDeclaredConstructor().newInstance();

        List<ValueWithModifier> v = (List<ValueWithModifier>) t;
        v.stream().forEach((vwm) -> {
          if (Strings.isNullOrEmpty(vwm.getValue())) {
            return;
          }
          Map<String, String> m = Util.getMapFromValue(vwm.getValue());
          switch (vwm.getPropertyValueModifier()) {
            case PLUS:
              result.putAll(m);
              break;
            case MINUS:
              result.keySet().removeAll(m.keySet());
              break;
            default:
              result.clear();
              result.putAll(m);
          }
        });

        return result;
      } catch (InstantiationException
              | IllegalAccessException
              | NoSuchMethodException
              | SecurityException
              | IllegalArgumentException
              | InvocationTargetException ex) {
        java.util.logging.Logger.getLogger(ProxyValueResolverRegistry.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
    };

    BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> fnOld = classToValue.putIfAbsent(pClazz, fnNew);
    return fnOld == null ? fnNew : fnOld;
  }

  public static BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> createSet(Class pClazz) {

    if (classToValue.containsKey(pClazz)) {
      return classToValue.get(pClazz);
    }

    BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> fnNew = (t, u) -> {
      try {
        final Set<String> result = (Set<String>) pClazz.getDeclaredConstructor().newInstance();
        List<ValueWithModifier> v = (List<ValueWithModifier>) t;
        v.stream().forEach((vwm) -> {
          if (Strings.isNullOrEmpty(vwm.getValue())) {
            return;
          }
          List<String> l = Arrays.asList(Util.convertCSVIntoArray(vwm.getValue()));
          switch (vwm.getPropertyValueModifier()) {
            case PLUS:
              result.addAll(l);
              break;
            case MINUS:
              result.removeAll(l);
              break;
            default:
              result.clear();
              result.addAll(l);
          }
        });
        return result;
      } catch (InstantiationException
              | IllegalAccessException
              | NoSuchMethodException
              | SecurityException
              | IllegalArgumentException
              | InvocationTargetException ex) {
        java.util.logging.Logger.getLogger(ProxyValueResolverRegistry.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
    };

    BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> fnOld = classToValue.putIfAbsent(pClazz, fnNew);
    return fnOld == null ? fnNew : fnOld;
  }

  public static BiFunction<Collection<ValueWithModifier>, DefaultValueProxy, Object> createArray(Class pClazz) {

    if (classToValue.containsKey(pClazz)) {
      return classToValue.get(pClazz);
    }

    if (classToValue.containsKey(pClazz.getComponentType())) {
      classToValue.putIfAbsent(pClazz, (t, u) -> {
        final List<String> items = (List<String>) classToValue.get(List.class).apply(t, u);
        Object result = Array.newInstance(pClazz.getComponentType(), items.size());

        for (int i = 0; i < items.size(); i++) {
          String item = items.get(i);
          List<ValueWithModifier> v = new ArrayList<>(1);
          ValueWithModifier vwm = new ValueWithModifier(item, PropertyValueModifier.NONE);
          v.add(vwm);
          Array.set(result, i, classToValue.get(pClazz.getComponentType()).apply(v, u));
        }
        return result;
      });
    } else {
      classToValue.putIfAbsent(pClazz, (t, u) -> {
        final List<String> result = (List<String>) classToValue.get(List.class).apply(t, u);
        return result
                .stream()
                .map(c -> Glue.instance().resolve(c))
                .collect(Collectors.toList())
                .toArray();
      });
    }

    return classToValue.get(pClazz);
  }

}
