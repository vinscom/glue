# Glue - Draft
Proven and opinionated programming, and configuration model for java based applications. Inspired from ATG Nucleus (Oracle Web Commerce), provide features such as configuration layers and property based component initialization.

## What is Glue ?
You will use Glue for same reason you will use Spring (dependency injection) in your application. But, in case on Glue, few concepts are implemented to make your life much easier form DevOps point of view.

## Properties based configuration

```
#/in/erail/glue/test/component/PropertiesComponent
$class=in.erail.glue.component.PropertiesComponent
$scope=LOCAL
propString=TestString
propArray=a,b,\
    c
propList=a,b,\
    c
propMap=a=b,c=d,\
    e=f
propComponent=/in/erail/glue/test/component/GlobalObjectByDefault
propBoolean=true
propEnum=TWO
propSet=a,b,b,c
propBoolean2=true
propServiceMap=\
    a=/in/erail/glue/test/component/GlobalObjectByDefault,\
    b=/in/erail/glue/test/component/GlobalObjectByDefault,\
    c=/in/erail/glue/test/component/GlobalObjectByDefault
propInt=2
propInteger=2
propFile=testconfig.json
propLong=2
propLong2=2
propLogger=true
propServiceArray=\
    /in/erail/glue/test/component/GlobalObjectByDefault,\
    /in/erail/glue/test/component/GlobalObjectByDefault,\
    /in/erail/glue/test/component/GlobalObjectByDefault
propNullString=
propNullServiceMap=
propNullComponent=
```

Above is test property file **PropertiesComponent.properties** to create component **PropertiesComponent**. Physically, it is present in folder **/vinscom/ioc/test/component**. Folder structure itself is present in another folder called **configuration layer** folder. We can provide multiple configuration layers to Glue. Physical location of PropertiesComponent.properties file under configuration layer becomes actual mounting point of component. In this case, PropertiesComponent is mounted at **/vinscom/ioc/test/component**. To get instance of PropertiesComponent, we use **/in/erail/glue/test/component/PropertiesComponent** path.

### ```$``` Sign
#### ```$class```
Instance of this class will be created whenever instance of this component is created.
#### ```$scope```
Default scope of component is "GLOBAL". Means, Whenever instance of this component is required, same instance of component is returned. This is same is Singleton Pattern.
If new instance is required on each component creation call then "LOCAL" scope can be defined. This will ensure, Glue will return new instance each time. 
#### ```$basedOn```
One component can copy all property values of another component. This can be done as below.

```
#/in/erail/glue/test/component/BasedOnPropertiesComponent
$basedOn=/in/erail/glue/test/component/PropertiesComponent
propString=TestString2
```

In above example, all properties of this component will have value as set in **/in/erail/glue/test/component/PropertiesComponent** component. Specific to this component **propString** will be overriden with value **TestString2**

## Configuration Layer
Layers are passed to Glue using JVM paramters
```
java -Dglue.layers=/testdata/layer1,/testdata/layer2
```

### Override Property Value
If we have two properties file at same location under different layers. Glue will try to override or merge property file in order configuration layers are defined. 
For example:

Layer 1 (/testdata/layer1/)

```
#/in/erail/glue/test/component/MergedComponent
$class=in.erail.glue.component.PropertiesComponent
$scope=LOCAL
propString=TestString
propArray=a,b,\
    c
propList=a,b,\
    c
propMap=a=b,c=d,\
    e=f
propComponent=/in/erail/glue/test/component/PropertiesComponent
propJson=testconfig.json
propSet=a,b,b,c
```

Layer 2 (/testdata/layer2/)

```
#/in/erail/glue/test/component/MergedComponent
propString=TestString2
propArray=a
propList-=a
propList+=b,c,d
propMap-=a=b
propMap+=z=b
propComponent=/in/erail/glue/test/component/PropertiesComponent2
propSet-=a,b
propSet+=e,f
```

#### Override Property Value
In above example, when instance of **/in/erail/glue/test/component/MergedComponent** is created, ```PropertiesComponent.getPropString()``` method will return **TestString2** instead of **TestString**. As you can see in above example, **Layer 2** is overriding propString value.

#### Merge Property Value
In case of List,Map,Set. Glue supports merge of deletion of property value. In above example, ```PropertiesComponent.getPropList()``` will return list with elements b,c,b,c,d. **Hyphen(-)** in ```propList-=a``` will remove *a* from list. And **Plus(+)** in ```propList+=b,c,d``` will add *b,c,d* elements to list. 
Save can be done in case of Map and Set.

### Component Property
A component property can refer to another component property.As can be seen in **/in/erail/glue/test/component/PropertiesComponent**, 

```
propComponent=/in/erail/glue/test/component/GlobalObjectByDefault
```
```PropertiesComponent.getPropComponent()``` will return instance of **GlobalObjectByDefault** component.

### Component property refering to another component property

```
#/in/erail/glue/test/component/RefPropertiesComponent
$class=in.erail.glue.component.PropertiesComponent
$scope=LOCAL
propString^=/in/erail/glue/test/component/PropertiesComponent.propString
propArray^=/in/erail/glue/test/component/PropertiesComponent.propArray
propList^=/in/erail/glue/test/component/PropertiesComponent.propList
propMap^=/in/erail/glue/test/component/PropertiesComponent.propMap
propComponent^=/in/erail/glue/test/component/PropertiesComponent.propComponent
propBoolean^=/in/erail/glue/test/component/PropertiesComponent.propBoolean
propEnum^=/in/erail/glue/test/component/PropertiesComponent.propEnum
propJson^=/in/erail/glue/test/component/MergedComponent.propJson
propServiceMap^=/in/erail/glue/test/component/PropertiesComponent.propServiceMap
```

## Supported Property Type

- String : propString=TestString
- Array[String] : propArray=a,b,c
- List<String> : propList=a,b,c
- Map<String,String> : propMap=a=b,c=d,e=f
- Boolean and boolean : propBoolean=true
- Enum : propEnum=TWO
- Set<String> : propSet=a,b,b,c
- Integer and Int : propInteger=2
- Long and long : propLong=2
- File : propFile=testconfig.json
- JsonObject (Vertx) : propFile=testconfig.json
- ServiceMap : 
- ServiceArray : 
- Logging : 
- Component : 
- Null :
