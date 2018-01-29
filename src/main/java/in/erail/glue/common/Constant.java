package in.erail.glue.common;

public class Constant {

  public static class Component {

    public static final String CLASS = "$class";
    public static final String SCOPE = "$scope";
    public static final String BASED_ON = "$basedOn";

    public static class Path {

      public static final String INITIAL = "/Initial";
    }

    public static class Modifier {

      public static final String PLUS = "+";
      public static final String MINU = "-";
      public static final String FROM = "^";
    }
  }

  public static class EnvVar {

    public static final String SEPERATOR = ",";
    public static final String METRIC_REGISTRY_NAME = "METRIC_REGISTRY_NAME";
    public static final String LAYERS = "GLUE_LAYERS";

    public static class Java {
      public static final String METRIC_REGISTRY_NAME = "metric.registry.name";
      public static final String LAYERS = "glue.layers";
    }

  }

}
