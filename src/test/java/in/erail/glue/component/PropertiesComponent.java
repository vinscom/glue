package in.erail.glue.component;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import io.vertx.core.json.JsonObject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import in.erail.glue.annotation.StartService;
import java.util.regex.Pattern;

public class PropertiesComponent {

  public String propString;
  public String propNullString;
  public String[] propArray;
  public String[] propNullArray;
  private Object[] propComponentArray;
  private Object[] propVarArgComponentArray;
  public List<String> propList;
  public List<String> propNullList;
  public Map<String, String> propMap;
  public Map<String, String> propNullMap;
  public Object propComponent;
  public Object propNullComponent;
  public boolean propBoolean = false;
  public boolean propNullBoolean = false;
  public Boolean propBoolean2 = false;
  public Boolean propNullBoolean2 = false;
  public boolean startup = false;
  public JsonObject propJson;
  public JsonObject propNullJson;
  public EnumTestValues propEnum;
  public EnumTestValues propNullEnum;
  public Set<String> propSet;
  public Set<String> propNullSet;
  public ServiceMap propServiceMap;
  public ServiceMap propNullServiceMap;
  public int propInt;
  public int propNullInt;
  public Integer propInteger;
  public Integer propNullInteger;
  public File propFile;
  public File propNullFile;
  public long propLong;
  public long propNullLong;
  public Long propLong2;
  public Long propNullLong2;
  public Logger propLogger;
  public Pattern propPattern;
  public Pattern propNullPattern;
  public Meter propMeter;
  public Histogram propHistogram;
  public Counter propCounter;
  public Timer propTimer;
  @SuppressWarnings("rawtypes")
  public Class propClass;
  public String mGlueMountPath;

  @StartService
  public void startup() {
    setStartup(true);
  }

  public boolean isStartup() {
    return startup;
  }

  public void setStartup(boolean pStartup) {
    this.startup = pStartup;
  }

  public String getPropString() {
    return propString;
  }

  public void setPropString(String pPropString) {
    this.propString = pPropString;
  }

  public String[] getPropArray() {
    return propArray;
  }

  public void setPropArray(String[] pPropArray) {
    this.propArray = pPropArray;
  }

  public List<String> getPropList() {
    return propList;
  }

  public void setPropList(List<String> pPropList) {
    this.propList = pPropList;
  }

  public Map<String, String> getPropMap() {
    return propMap;
  }

  public void setPropMap(Map<String, String> pPropMap) {
    this.propMap = pPropMap;
  }

  public Object getPropComponent() {
    return propComponent;
  }

  public void setPropComponent(Object pPropComponent) {
    this.propComponent = pPropComponent;
  }

  public boolean isPropBoolean() {
    return propBoolean;
  }

  public void setPropBoolean(boolean pPropBoolean) {
    propBoolean = pPropBoolean;
  }

  public JsonObject getPropJson() {
    return propJson;
  }

  public void setPropJson(JsonObject pPropJson) {
    this.propJson = pPropJson;
  }

  public EnumTestValues getPropEnum() {
    return propEnum;
  }

  public void setPropEnum(EnumTestValues pPropEnum) {
    this.propEnum = pPropEnum;
  }

  public Set<String> getPropSet() {
    return propSet;
  }

  public void setPropSet(Set<String> pPropSet) {
    this.propSet = pPropSet;
  }

  public Boolean isPropBoolean2() {
    return propBoolean2;
  }

  public void setPropBoolean2(Boolean pPropBoolean2) {
    this.propBoolean2 = pPropBoolean2;
  }

  public ServiceMap getPropServiceMap() {
    return propServiceMap;
  }

  public void setPropServiceMap(ServiceMap pPropServiceMap) {
    this.propServiceMap = pPropServiceMap;
  }

  public int getPropInt() {
    return propInt;
  }

  public void setPropInt(int pPropInt) {
    this.propInt = pPropInt;
  }

  public Integer getPropInteger() {
    return propInteger;
  }

  public void setPropInteger(Integer pPropInteger) {
    this.propInteger = pPropInteger;
  }

  public File getPropFile() {
    return propFile;
  }

  public void setPropFile(File pPropFile) {
    this.propFile = pPropFile;
  }

  public long getPropLong() {
    return propLong;
  }

  public void setPropLong(long pPropLong) {
    this.propLong = pPropLong;
  }

  public Long getPropLong2() {
    return propLong2;
  }

  public void setPropLong2(Long pPropLong2) {
    this.propLong2 = pPropLong2;
  }

  public Logger getPropLogger() {
    return propLogger;
  }

  public void setPropLogger(Logger pPropLogger) {
    this.propLogger = pPropLogger;
  }

  public String getPropNullString() {
    return propNullString;
  }

  public void setPropNullString(String pPropNullString) {
    this.propNullString = pPropNullString;
  }

  public ServiceMap getPropNullServiceMap() {
    return propNullServiceMap;
  }

  public void setPropNullServiceMap(ServiceMap pPropNullServiceMap) {
    this.propNullServiceMap = pPropNullServiceMap;
  }

  public Object getPropNullComponent() {
    return propNullComponent;
  }

  public void setPropNullComponent(Object pPropNullComponent) {
    this.propNullComponent = pPropNullComponent;
  }

  public String[] getPropNullArray() {
    return propNullArray;
  }

  public void setPropNullArray(String[] pPropNullArray) {
    this.propNullArray = pPropNullArray;
  }

  public List<String> getPropNullList() {
    return propNullList;
  }

  public void setPropNullList(List<String> pPropNullList) {
    this.propNullList = pPropNullList;
  }

  public Map<String, String> getPropNullMap() {
    return propNullMap;
  }

  public void setPropNullMap(Map<String, String> pPropNullMap) {
    this.propNullMap = pPropNullMap;
  }

  public boolean isPropNullBoolean() {
    return propNullBoolean;
  }

  public void setPropNullBoolean(boolean pPropNullBoolean) {
    this.propNullBoolean = pPropNullBoolean;
  }

  public Boolean getPropNullBoolean2() {
    return propNullBoolean2;
  }

  public void setPropNullBoolean2(Boolean pPropNullBoolean2) {
    this.propNullBoolean2 = pPropNullBoolean2;
  }

  public JsonObject getPropNullJson() {
    return propNullJson;
  }

  public void setPropNullJson(JsonObject pPropNullJson) {
    this.propNullJson = pPropNullJson;
  }

  public EnumTestValues getPropNullEnum() {
    return propNullEnum;
  }

  public void setPropNullEnum(EnumTestValues pPropNullEnum) {
    this.propNullEnum = pPropNullEnum;
  }

  public Set<String> getPropNullSet() {
    return propNullSet;
  }

  public void setPropNullSet(Set<String> pPropNullSet) {
    this.propNullSet = pPropNullSet;
  }

  public int getPropNullInt() {
    return propNullInt;
  }

  public void setPropNullInt(int pPropNullInt) {
    this.propNullInt = pPropNullInt;
  }

  public Integer getPropNullInteger() {
    return propNullInteger;
  }

  public void setPropNullInteger(Integer pPropNullInteger) {
    this.propNullInteger = pPropNullInteger;
  }

  public File getPropNullFile() {
    return propNullFile;
  }

  public void setPropNullFile(File pPropNullFile) {
    this.propNullFile = pPropNullFile;
  }

  public long getPropNullLong() {
    return propNullLong;
  }

  public void setPropNullLong(long pPropNullLong) {
    this.propNullLong = pPropNullLong;
  }

  public Long getPropNullLong2() {
    return propNullLong2;
  }

  public void setPropNullLong2(Long pPropNullLong2) {
    this.propNullLong2 = pPropNullLong2;
  }

  public Pattern getPropPattern() {
    return propPattern;
  }

  public void setPropPattern(Pattern pPropPattern) {
    this.propPattern = pPropPattern;
  }

  public Pattern getPropNullPattern() {
    return propNullPattern;
  }

  public void setPropNullPattern(Pattern pPropNullPattern) {
    this.propNullPattern = pPropNullPattern;
  }

  public Meter getPropMeter() {
    return propMeter;
  }

  public void setPropMeter(Meter pPropMeter) {
    this.propMeter = pPropMeter;
  }

  public Histogram getPropHistogram() {
    return propHistogram;
  }

  public void setPropHistogram(Histogram pPropHistogram) {
    this.propHistogram = pPropHistogram;
  }

  public Counter getPropCounter() {
    return propCounter;
  }

  public void setPropCounter(Counter pPropCounter) {
    this.propCounter = pPropCounter;
  }

  public Timer getPropTimer() {
    return propTimer;
  }

  public void setPropTimer(Timer pPropTimer) {
    this.propTimer = pPropTimer;
  }

  public Object[] getPropComponentArray() {
    return propComponentArray;
  }

  public void setPropComponentArray(Object[] pPropComponentArray) {
    this.propComponentArray = pPropComponentArray;
  }

  public Object[] getPropVarArgComponentArray() {
    return propVarArgComponentArray;
  }

  public void setPropVarArgComponentArray(Object... pPropVarArgComponentArray) {
    this.propVarArgComponentArray = pPropVarArgComponentArray;
  }

  @SuppressWarnings("rawtypes")
  public Class getPropClass() {
    return propClass;
  }

  @SuppressWarnings("rawtypes")
  public void setPropClass(Class pPropClass) {
    this.propClass = pPropClass;
  }

  public String getGlueMountPath() {
    return mGlueMountPath;
  }

  public void setGlueMountPath(String pGlueMountPath) {
    this.mGlueMountPath = pGlueMountPath;
  }

}
