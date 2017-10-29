package vinscom.ioc.test.component;

import io.vertx.core.json.JsonObject;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import vinscom.ioc.annotation.StartService;
import vinscom.ioc.component.ServiceArray;
import vinscom.ioc.component.ServiceMap;

public class PropertiesComponent {

  public String propString;
  public String[] propArray;
  public List<String> propList;
  public Map<String, String> propMap;
  public Object propComponent;
  public boolean propBoolean = false;
  public Boolean propBoolean2 = false;
  public boolean startup = false;
  public JsonObject propJson;
  public EnumTestValues propEnum;
  public Set<String> propSet;
  public ServiceMap propServiceMap;
  public int propInt;
  public Integer propInteger; 
  public File propFile;
  public long propLong;
  public Long propLong2;
  public Logger propLogger;
  public ServiceArray propServiceArray;

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

  public ServiceArray getPropServiceArray() {
    return propServiceArray;
  }

  public void setPropServiceArray(ServiceArray pPropServiceArray) {
    this.propServiceArray = pPropServiceArray;
  }
  
}
