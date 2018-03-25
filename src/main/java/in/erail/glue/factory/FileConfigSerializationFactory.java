package in.erail.glue.factory;

import com.google.common.collect.ListMultimap;
import in.erail.glue.ConfigSerializationFactory;
import in.erail.glue.common.ValueWithModifier;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author vinay
 */
public class FileConfigSerializationFactory implements ConfigSerializationFactory {

  private static final String FILE_LOCATION = ".";
  private static final String FILE_NAME = "glue.ser";
  private final Logger log = LogManager.getLogger(FileConfigSerializationFactory.class.getCanonicalName());

  @Override
  public Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig) {
    return save(pConfig, DEFAULT_IDENTIFIER);
  }

  @Override
  public Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig, String pIdentifier) {

    Path fileLocation = Paths.get(FILE_LOCATION, pIdentifier + FILE_NAME);

    return Single
            .just(fileLocation)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable((location) -> {
              try (FileOutputStream file = new FileOutputStream(location.toFile());
                      ObjectOutputStream output = new ObjectOutputStream(file)) {
                output.writeObject(pConfig);
              } catch (IOException err) {
                return Completable.error(err);
              }
              return Completable.complete();
            });
  }

  @Override
  public Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load() {
    return load(DEFAULT_IDENTIFIER);
  }

  @Override
  public Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load(String pIdentifier) {
    Path fileLocation = Paths.get(FILE_LOCATION, pIdentifier + FILE_NAME);
    return Maybe
            .just(fileLocation)
            .subscribeOn(Schedulers.io())
            .flatMap((location) -> {
              Map<String, ListMultimap<String, ValueWithModifier>> result;
              try (FileInputStream file = new FileInputStream(location.toFile());
                      ObjectInputStream input = new ObjectInputStream(file)) {
                result = (Map<String, ListMultimap<String, ValueWithModifier>>) input.readObject();
              } catch (IOException | ClassNotFoundException ex) {
                log.info(() -> "Not able to load config from : " + fileLocation.toString());
                log.debug(() -> ex);
                return Maybe.empty();
              }
              return Maybe.just(result);
            });
  }

}
