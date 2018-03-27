package in.erail.glue.factory;

import com.google.common.collect.ListMultimap;
import in.erail.glue.ConfigSerializationFactory;
import in.erail.glue.PropertiesRepository;
import in.erail.glue.common.Util;
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
public class LocalConfigSerializationFactory implements ConfigSerializationFactory {

  public static final String ENV_FILE_LOCATION = "local.config.factory.file.location";
  public static final String ENV_FILE_NAME = "local.config.factory.file.name";
  public static final String ENV_IDENTIFIER = "local.config.factory.identifier";
  public static final String ENV_DISABLE_SAVE = "local.config.factory.disable.save";

  private static final String DEFAULT_FILE_LOCATION = "";
  private static final String DEFAULT_FILE_NAME = "glue.ser";
  private static final String DEFAULT_DISABLE_SAVE = "false";
  private final Logger log = LogManager.getLogger(LocalConfigSerializationFactory.class.getCanonicalName());

  private final String mFileLocation;
  private final String mFileName;
  private final String mIdentifier;
  private final boolean mSaveDisabled;

  public LocalConfigSerializationFactory() {
    mFileLocation = Util.getEnvironmentValue(ENV_FILE_LOCATION, DEFAULT_FILE_LOCATION);
    mFileName = Util.getEnvironmentValue(ENV_FILE_NAME, DEFAULT_FILE_NAME);
    mIdentifier = Util.getEnvironmentValue(ENV_IDENTIFIER, DEFAULT_IDENTIFIER);
    mSaveDisabled = Boolean.parseBoolean(Util.getEnvironmentValue(ENV_DISABLE_SAVE, DEFAULT_DISABLE_SAVE));
  }

  @Override
  public Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig) {
    return save(pConfig, mIdentifier);
  }

  @Override
  public Completable save(Map<String, ListMultimap<String, ValueWithModifier>> pConfig, String pIdentifier) {

    if (mSaveDisabled) {
      return Completable.complete();
    }

    Path fileLocation = Paths.get(mFileLocation, mFileName + pIdentifier);

    return Single
            .just(fileLocation)
            .subscribeOn(Schedulers.io())
            .flatMapCompletable((location) -> {
              try (FileOutputStream file = new FileOutputStream(location.toFile());
                      ObjectOutputStream output = new ObjectOutputStream(file)) {
                output.writeObject(pConfig);
                log.debug("Done writing");
              } catch (IOException err) {
                return Completable.error(err);
              }
              return Completable.complete();
            });
  }

  @Override
  public Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load() {
    return load(mIdentifier);
  }

  @Override
  public Maybe<Map<String, ListMultimap<String, ValueWithModifier>>> load(String pIdentifier) {
    Path fileLocation = Paths.get(mFileLocation, mFileName + pIdentifier);
    return Maybe
            .just(fileLocation)
            .subscribeOn(Schedulers.io())
            .flatMap((location) -> {
              log.debug("Reading....");
              Map<String, ListMultimap<String, ValueWithModifier>> result;
              try (FileInputStream file = new FileInputStream(location.toFile());
                      ObjectInputStream input = new ObjectInputStream(file)) {
                result = (Map<String, ListMultimap<String, ValueWithModifier>>) input.readObject();
                log.info(() -> "Config loaded from : " + location.toString());
                return Maybe.just(result);
              } catch (IOException | ClassNotFoundException ex) {
                log.info(() -> "Not able to load config from : " + location.toString());
                log.debug(ex);
              }
              return Maybe.empty();
            });
  }

  public static void main(String[] pArgs) {
    PropertiesRepository.setLayers(Util.getSystemLayers());
    new PropertiesRepository().init();
  }
}
