package in.erail.example;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class ShortDateFormatter implements MessageFormatter{

  @Override
  public String format(Instant pInstant) {
    return DateTimeFormatter.ISO_DATE.format(pInstant);
  }
  
}
