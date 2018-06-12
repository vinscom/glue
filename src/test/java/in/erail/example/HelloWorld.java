package in.erail.example;

import in.erail.glue.annotation.StartService;
import org.apache.logging.log4j.Logger;

public class HelloWorld {
  
  private String mMessage;
  private Logger mLog;
  
  @StartService
  public void start(){
    getLog().info(getMessage());
  }

  public String getMessage() {
    return mMessage;
  }

  public void setMessage(String pMessage) {
    this.mMessage = pMessage;
  }

  public Logger getLog() {
    return mLog;
  }

  public void setLog(Logger pLog) {
    this.mLog = pLog;
  }
  
}
