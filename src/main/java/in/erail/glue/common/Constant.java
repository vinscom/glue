package in.erail.glue.common;

public class Constant {

  public static class Component {

    public static final String SPECIAL_PROPERTY = "$";
    public static final String CLASS = SPECIAL_PROPERTY + "class";
    public static final String SCOPE = SPECIAL_PROPERTY + "scope";
    public static final String BASED_ON = SPECIAL_PROPERTY + "basedOn";
    public static final String INSTANCE_FACTORY = SPECIAL_PROPERTY + "instanceFactory";
    public static final String BASED_ON_SPECIAL_PROPERTIES = SPECIAL_PROPERTY + "_basedOn";
    public static final String COMPONENT_PATH_SPECIAL_PROPERTIES = SPECIAL_PROPERTY + "componentPath";
    public static final String MOUNT_PATH_PROPERTIES = "mountPath";

    public static class Path {

      public static final String INITIAL = "/Initial";
    }

    public static class Modifier {

      public static final String PLUS = "+";
      public static final String MINU = "-";
      public static final String FROM = "^";
    }
  }

  public static class SystemProperties {

    public static final String SEPERATOR = ",";
    public static final String METRIC_REGISTRY_NAME = "metric.registry.name";
    public static final String LAYERS = "glue.layers";
    public static final String GLUE_SERIALIZATION_FACTORY = "glue.serialization.factory";
    public static final String VALUE_PROXY_CLASS = "glue.value.proxy.class";
    public static final String GLUE_CONFIG = "glue.config";

  }
  
}
