package in.erail.glue.common;

import com.google.common.base.MoreObjects;
import java.io.Serializable;

public class Tuple<K, V> implements Serializable{

  public K value1;
  public V value2;

  public Tuple(K value1, V value2) {
    this.value1 = value1;
    this.value2 = value2;
  }

  @Override
  public String toString() {
    return MoreObjects
            .toStringHelper(this)
            .add("Value1", value1)
            .add("Value2", value2)
            .toString();
  }

}
