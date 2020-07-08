### 数据埋点相关(使用`GTM.event(xxx)`方式,代码侵入极高, 但是可以实现高度自定义)

#### 1.SDK 集成
##### 1.1 首先在project视图下，将aar文件添加到libs文件夹里

##### 1.2 然后需要在使用的模块的build.gradle文件中添加如下配置：
```
repositories {
    flatDir {
        dirs 'libs'
    }
}
```

##### 1.3 最后在dependencies中添加配置：
```
compile(name: 'zlyq-android-sdk-1.0.1', ext: 'aar')
或
implementation(name: 'zlyq-android-sdk-1.0.1', ext: 'aar')
```

#### 2.SDK 服务启动
##### 2.1 AndroidManifest.xml添加网络访问权限
```
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

##### 2.2 在application中初始化
```
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ZADataManager.Builder builder = new ZADataManager.Builder(this);
        builder.setPushUrl("接入方服务器地址")// 必填!!!!!!
                .setApiKey("接入的apiKey")// 必填!!!!!!
                .setDebug(BuildConfig.DEBUG)//是否是debug
                .setPushLimitMinutes(5)//多少分钟 push一次
                .setPushLimitNum(100)//多少条 就主动进行push
                .setProjectId(2)//项目id
                .start();//开始

        // 主动push，实时
        Map customMap = new HashMap();
        customMap.put("custom_key1", "custom_value1");
        customMap.put("custom_key2", "custom_value2");
        ZADataAPI.event("event", customMap);
        ZADataAPI.pushEvent("event", customMap);//关键代码，即时上报
    }
}
```

##### 2.3 如果想进行事件统计,但是不想进行推送服务
```
/**
* 停止事件的上传任务(仍会记录事件,停止事件推送)
*/
ZADataManager.cancelEventPush();
```

##### 2.4 如果想立刻停止所有sdk的事件服务
```
/**
* 停止事件的上传任务(仍会记录事件,停止事件推送)
*/
ZADataManager.destoryEventService();
```

#### 3.功能支持情况

| V1.0.0功能  | 是否支持  |
| :------------ | :------------ |
|  接口自定义 | 支持  |
|  缓存策略 | 支持  |
|  外部cookie注入 | 支持  |
|  推送周期设定 | 支持  |
|  强制推送 | 支持  |
|  自定义埋点事件 | 支持  |
|  独立运行 | 支持  |
|  多线程写入 | 支持  |
|  后台线程服务 | 支持  |

#### 4.上传规则
- **固定周期进行上传**: 比如每5分钟,进行一次数据上传.数据为触发推送的时间节点之前的数据.用于大部分统计.
- **固定条数进行上传**: 比如每100条,进行一次数据上传.数据为触发100条推送开始之前的数据.用于大部分统计.
- **实时上传**: 每次点击就进行push操作.用于特定统计.

#### 5.统计操作

##### 1.**Event** 点击事件
- **event 自定义事件名**
- **customMap 对应自定义事件的扩展字段** ,map形式输入**
```
Map customMap = new HashMap();
customMap.put("custom_key1", "custom_value1");
customMap.put("custom_key2", "custom_value2");
ZADataAPI.event("event", customMap);
```

##### 2.**login** 事件
- **接入方登陆成功后调用**
- **userId对应接入方系统的userId**
```
ZADataAPI.login("接入方的userId");
```

##### 3.**logout** 事件
- **接入方退出登陆成功后调用**
```
ZADataAPI.logout();
```

##### 4.**setUserProfile ** 事件
- **不存在则新增, 存在则覆盖**
- **map 对应画像的相关数据** ,map形式输入**
```
Map map = new HashMap();
map.put("name", "小明");
map.put("gender", "男");
ZADataAPI.setUserProfile(map);
```

##### 5.**setOnceUserProfile ** 事件
- **首次调用正常赋值, 重复调用不会覆盖**
- **map 对应画像的相关数据** ,map形式输入**
```
Map map = new HashMap();
map.put("name", "小明");
map.put("gender", "男");
ZADataAPI.setOnceUserProfile(map);
```

##### 6.**appendUserProfile ** 事件
- **只有数组字段才可以使用, 以追加的方式添加到数组**
- **map 对应画像的相关数据** ,map形式输入**
```
Map map = new HashMap();
map.put("name", "小明");
map.put("gender", "男");
ZADataAPI.appendUserProfile(map);
```

##### 7.**increaseUserProfile ** 事件
- **只有数字类型字段可以调用, 用作计数**
- **map 对应画像的相关数据** ,map形式输入**
```
Map map = new HashMap();
map.put("age", 25);
ZADataAPI.increaseUserProfile(map);
```

##### 8.**deleteUserProfile ** 事件
- **删除用户画像**
- **map 对应画像的相关数据** ,map形式输入**
```
Map map = new HashMap();
map.put("name", "小明");
map.put("gender", "男");
ZADataAPI.deleteUserProfile(map);
```

##### 9.**unsetUserProfile ** 事件
- **取消画像字段的赋值**
- **map 对应画像的相关数据** ,map形式输入**
```
Map map = new HashMap();
map.put("name", "小明");
map.put("gender", "男");
ZADataAPI.unsetUserProfile(map);
```

##### 10.**commonParams ** 事件
- **commonParams为自定义event事件的预制属性**
```
Map<String, Object> commonParams = ZADataAPI.commonParams();
```

#### 6.混淆（根据所选功能做混淆处理）
```
-keep class com.zlyq.client.android.analytics.**{*;}
-keep class com.zlyq.client.android.analytics.exception.**{*;}
-keep class com.zlyq.client.android.analytics.enums.**{*;}
```