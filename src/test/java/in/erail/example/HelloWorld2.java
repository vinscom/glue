package in.erail.example;

import in.erail.glue.annotation.StartService;
import java.time.Instant;
import org.apache.logging.log4j.Logger;

public class HelloWorld2 {
  
  private MessageFormatter mFormatter;
  private Logger mLog;
  
  @StartService
  public void start(){
    getLog().info(getFormatter().format(Instant.now()));
  }

  public MessageFormatter getFormatter() {
    return mFormatter;
  }

  public void setFormatter(MessageFormatter pFormatter) {
    this.mFormatter = pFormatter;
  }

  public Logger getLog() {
    return mLog;
  }

  public void setLog(Logger pLog) {
    this.mLog = pLog;
  }

}
