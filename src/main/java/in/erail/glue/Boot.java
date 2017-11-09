package in.erail.glue;

import in.erail.glue.common.Constant;

public class Boot {
  public static void main(String[] pArgs) {
    Glue.instance().resolve(Constant.Component.Path.INITIAL);
  }
}
