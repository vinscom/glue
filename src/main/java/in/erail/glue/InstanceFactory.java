package in.erail.glue;

import java.util.Optional;

/**
 *
 * @author vinay
 */
public interface InstanceFactory {

  Optional<Object> createInstance();
}
