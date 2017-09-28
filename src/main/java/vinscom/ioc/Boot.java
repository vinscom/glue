package vinscom.ioc;

import vinscom.ioc.common.Constant;

public class Boot {
  public static void main(String[] pArgs) {
    Glue.instance().resolve(Constant.Component.Path.INITIAL);
  }
}
