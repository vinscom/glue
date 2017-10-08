package vinscom.ioc.test.component;

import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import vinscom.ioc.annotation.StartService;

public class PropertiesComponent {

  public String propString;
  public String[] propArray;
  public List<String> propList;
  public Map<String, String> propMap;
  public Object propComponent;
  public boolean propBoolean = false;
  public boolean startup = false;
  public JsonObject propJson;
  public EnumTestValues propEnum;
  public Set<String> propSet;
  
  @StartService
  public void startup(){
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

}
