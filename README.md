#目录
##一、SDK介绍与快速入门
##二、SDK安装与初始化
####SDK 手动安装：

下载SDK并解压得到如下文件：

```
├── JavaBaas-sdk-<版本号>.jar           // JavaBaas SDK核心模块
├── fastjson-<版本号>.jar               // JavaBaas SDK序列化模块
├── okhttp-<版本号>.jar                 // JavaBaas SDK网络请求模块
├── okio-<版本号>.jar                   // 七牛云存储SDK依赖模块
├── happy-dns-0.2.5.jar                // 七牛云存储SDK依赖模块
└── qiniu-android-sdk-<版本号>.jar      // 七牛云存储SDK核心模块
```

将解压出的模块导入到项目中。

####Maven自动导入安装（coming soon）



####SDK初始化：

新建一个类 `App`继承自`Application `类：

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化参数依次为 this, AppKey, AppId, 后台服务器地址,
        // 获取InstallationId的回调(用来处理与InstallationId有关的操作,如绑定到用户等)
        JBCloud.init(this , "1f7049bfde7d440cb31210aa5e4d44ed" , "5645b2a574242e39eee89829" , "https://api.javabaas.com" , null);
    }
}
```

然后在`AndroidManifest.xml`中配置SDK所需的一些权限以及声明刚才创建的`App`类

```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />

    <application
        android:name="com.javabaas.sample.App"
        ...
        >
        ...
    </application>
```

SDK配置完成，可以进行一些简单的访问测试：

```java
public void onSave(View view) {
       final JBObject testC = new JBObject("testC");
       testC.put("testA", "测试A");
       testC.put("testB", "测试B");
       testC.saveInBackground(new SaveCallback() {
                @Override
                public void done(JBObject object) {
                    Log.d("ObjectTest" ,"保存成功 id=" + testC.getId());
                }

                @Override
                public void error(JBException e) {
                    Log.d("ObjectTest" ,"保存失败 "+e.getResponseErrorMsg);
                    e.printStackTrace();
                }
            });
}
```

##三、对象
###3.1 JBObject
`JBOject`是JavaBaas中的基础对象，也可以理解为`JBOject`对应着数据库表中的一条信息。

假如你在云端数据库中使用`FoodLike`表来记录用户最喜欢的食物，那么表中至少会有`foodName`(食品名称)，`userName`(用户名称)属性，那么，你应该这样生成`JBOject`:

```java
JBObject jbObject = new JBObject("FoodLike");
jbObject.put("foodName","dumpling");
jbObject.put("userName","ZhangSan");
```
有几点需要注意:

* 每个`JBOject`都必须在云端有对应的数据库表和相应的字段。
* 每个`JBOject`都有保留字段，分别为`_id``acl``createdPlat``updatedPlat``createdAt``updatedAt`，这些字段由系统自动生成和修改，不需要开发者进行指定。

###3.2 同步与异步
  JavaBaas提供了数据检索，保存，更新，删除，查询的同步与异步的方法。  
  
  注: 在Android UI主线程中调用同步的方法，可能会导致UI主线程阻塞。所以，在UI主线程中请使用异步的方式。
  

###3.3 检索对象
 如果你知道了云端中某条数据的`objectId`，那么可以通过以下代码获取此条数据对应的`JBOject`对象:

```java
1.同步检索:
JBQuery<JBObject> jbQuery = JBQuery.getInstance("FoodLike");
jbQuery.whereEqualTo("_id", objectID);
JBObject result;
try {
		List<JBObject> resultList = jbQuery.find();
		result = resultList.get(0);
} catch (JBException e) {
		e.printStackTrace();
}

2.异步检索:
JBQuery<JBObject> jbQuery = JBQuery.getInstance("FoodLike");
jbQuery.whereEqualTo("_id", objectID);
jbQuery.findInBackground(new FindCallback<JBObject>() {
		@Override
		public void done(List<JBObject> resultList) {
			JBObject result = resultList.get(0);                    
		}
		
		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```
      
###3.4 保存对象
如3.1所示，假如你在本地生成一个`JBObject`之后，那么可以使用以下代码将其保存至云端:

```java
1.同步保存:
try {
		jbObject.save();
		Log.d("ObjectTest" , "保存成功 "+ jbObject.getId());
} catch (JBException e) {
		e.printStackTrace();
}

2.异步保存:
jbObject.saveInBackground(new SaveCallback() {
         @Override
         public void done(JBObject object) {  
              Log.d("ObjectTest" , "保存成功 "+ object.getId());
         }

         @Override
         public void error(JBException e) {
         		e.printStackTrace();
         }
});
```
 

###3.5 更新对象
如果你知道了云端中某条数据的`objectId`，那么可以通过以下代码更新云端对应的数据:

```java
1.同步更新:
JBObject jbObject = new JBObject("FoodLike");
jbObject.setId(objectId);
jbObject.put("foodName","hamburger");
try {
		jbObject.save();
		Log.d("ObjectTest", "修改成功了");
} catch (JBException e) {
		e.printStackTrace();
}

2.异步更新:
JBObject jbObject = new JBObject("FoodLike");
jbObject.setId(objectId);
jbObject.put("foodName","hamburger");
jbObject.saveInBackground(new SaveCallback() {
		@Override
		public void done(JBObject object) {
			Log.d("ObjectTest", "修改成功了");
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```
###3.6 删除对象
如果你知道了云端中某条数据的`objectId`，那么可以通过以下代码删除云端对应的数据:

```java
1.同步删除:
try {
		JBObject.deleteById("FoodLike", objectId);
		Log.d("ObjectTest", "删除成功了");
} catch (JBException e) {
		e.printStackTrace();
}

2.异步删除:
JBObject.deleteByIdInBackground("FoodLike", objectId, new DeleteCallback() {
		@Override
		public void done() {
			Log.d("ObjectTest", "删除成功了");
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

###3.7 关联对象

在数据存储中，类之间可能会有直接的关联关系。

比如，一个公民拥有一个唯一身份证。那么如果在一个公民对象中想指向一个身份证类的对象，那么可以通过以下代码来实现:

```java

JBObject citizenObject = new JBObject("Citizen");

//实例化一个'身份证'对象，并将已知的Id赋值进去。
JBObject identificationCardObject = JBObject.createWithoutDate("IdentificationCard","babc5b153dc0401fb5fcd8ffaae0ddf6");

//将'身份证'对象赋值到'公民'对象中。
citizenObject.put("identificationCard", identificationCardObject);

//保存
citizenObject.saveInBackground(new SaveCallback() {
		@Override
		public void done(JBObject object) {
			Log.d("ObjectTest", "保存成功 " + object.getId());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

###3.8 原子操作 
很多应用都会用到计数器的功能，比如在一个新闻类的应用中，我们需要记录每条新闻的查看次数，可以使用以下代码:

```
//第一个参数为新闻对象所在的表名，第二个参数为新闻对象的id
JBObject testObject = JBObject.createWithoutData("News",objectId);
//第一个参数为要增加的字段名称，第二个参数为要增加的个数
testObject.incrementKeyInBackground("openCount", 1, new RequestCallback() {
		@Override
		public void done() {
			Log.d("ObjectTest", "自增成功");
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});

```
针对上面的情况，还可以直接调用`incrementKeyInBackground`方法，来实现参数加1。

```
testObject.incrementKeyInBackground("openCount", new RequestCallback() {
		@Override
		public void done() {
			Log.d("ObjectTest", "自增成功");
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});

```

###3.9 批量操作 (敬请期待)

###3.10 数据类型 

目前我们支持的数据类型有:

`String``Int``Float``Boolean`以及`Date``File``Object``Array`类型

简单使用用例如下:

```java
JBObject testObject = new JBObject("Test");

//String
testObject.put("testString", "测试");
//Int
testObject.put("testInt",1);
//Float
testObject.put("testFloat",1.111f);
//boolean
testObject.put("testBoolean", true);
//Date
testObject.put("testDate", new Date());
//Array
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
testObject.put("testArray", list);

testObject.saveInBackground(new SaveCallback() {
		@Override
		public void done(JBObject object) {
			Log.d("ObjectTest", "保存成功 " + object.getId());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```
##四、查询
SDK中提供了`JBQuery`类来满足应用不同条件下的查询需求。
###4.1  基本查询
如果想查询指定表中的所有数据，可以使用以下代码:

```java
1.同步查询:
JBQuery jbQuery = JBQuery.getInstance("Test");
List<JBObject> list;
try {
		list = jbQuery.find();
		Log.d("ObjectTest", "查询到的所有条数为" + list.size());
} catch (JBException e) {
		e.printStackTrace();
}

2.异步查询:
JBQuery jbQuery = JBQuery.getInstance("Test");
jbQuery.findInBackground(new FindCallback<JBObject>() {
		@Override
		public void done(List<JBObject> result) {
			Log.d("ObjectTest", "查询到的所有条数为" + result.size());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```
###4.2  约束查询
SDK支持多种约束查询条件，举例如下:

```java
//限制查询条数为10条
jbQuery.setLimit(10);

//查询会跳过前两条
jbQuery.skip(2);

//查询'age'字段为'18'的
jbQuery.whereEqualTo("age",18);
```
###4.3  数组值查询
如果想查询数据库中某个字段包含在指定数组中的所有数据，可以添加以下限制条件:

```java
List<Integer> list = new ArrayList<>();
list.add(18);
list.add(19);
list.add(20);

//查询年龄包含在指定数组中的所有数据
jbQuery.whereContainedIn("age", list);
```

###4.4  模糊查询

JavaBaas支持多种模糊查询条件，举例如下:

```java
//查询所有name中包含'浩'字的
jbQuery.whereMatches("name","浩");

//查询所有name中以'王'开头的
jbQuery.whereStartsWith("name","王");

//查询所有name中以'超'结尾的
jbQuery.whereEndsWith("name","超");
```

###4.5  关系查询

因为在数据存储中，类之间可能会有直接的关联关系。比如一个Student对象studentA，它的英语老师会指向一个Teacher的对象teacherA。

那么，如果我要查询所有英语老师为teacherA的Student对象，可以使用以下代码:

```
JBQuery<JBObject> jbQuery  = JBQuery.getInstance("Student");
//第二个参数为teacherA的id
JBObject teacherA = JBObject.createWithoutData("Teacher","939666c47ded4fafb1a8403b830cdc98");
jbQuery.whereEqualTo("englishTeacher",teacherA);
jbQuery.findInBackground(new FindCallback<JBObject>() {
		@Override
		public void done(List<JBObject> result) {
			Log.d("ObjectTest", "查询成功，查询到的所有条数为" + result.size());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});

```
当涉及到云端中的三张表的关系查询时，就需要用到`whereMatchesKeyInQuery`方法。

假如，在一个图书馆系统中，用户可以关注多个类别，如"经典著作"类别，"历史地理"类别。

如果要查询用户关注的类别中的所有书籍，可以使用以下代码:

```
JBQuery<JBObject> mainQuery = JBQuery.getInstance("Book");
JBQuery<JBObject> subQuery = JBQuery.getInstance("SubscriptionCategory");
subQuery.whereEqualTo("user", JBUser.getCurrentUser());
mainQuery.whereMatchesKeyInQuery("category", "subscriptionCategory", "Category", subQuery);
mainQuery.findInBackground(new FindCallback<JBObject>() {
		@Override
		public void done(List<JBObject> result) {
			Log.d("ObjectTest", "查询成功，查询到的所有条数为" + result.size());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

当我们需要将JBQuery查询对象融合起来一起查询的时候，可以使用静态方法`or`来实现

例如查询Student中年龄大于18或者小于10的对象，可以使用以下代码:

```
JBQuery<JBObject> lotsOfQuery = JBQuery.getInstance("Student");
lotsOfQuery.whereGreaterThan("age", 18);

JBQuery<JBObject> fewQuery = JBQuery.getInstance("Student");
fewQuery.whereLessThan("age", 10);

List<JBQuery<JBObject>> list = new ArrayList<>();
list.add(lotsOfQuery);
list.add(fewQuery);

JBQuery<JBObject> mainQuery = JBQuery.or(MainActivity.this,list);
mainQuery.findInBackground(new FindCallback<JBObject>() {
		@Override
		public void done(List<JBObject> result) {
			Log.d("ObjectTest", "查询成功，查询到的所有条数为" + result.size());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```



###4.6  缓存查询
缓存查询是将查询的结果缓存到本地，当程序处于没有网络连接的状态时，就可以通过缓存查询查出上一次相同查询的结果。
JavaBaas支持的缓存类型有:

```
CACHE_ELSE_NETWORK:先从缓存中获取数据，如果本地没有缓存，从网络中获取数据。
CACHE_ONLY:只从缓存中获取数据，
CACHE_THEN_NETWORK:先从缓存中获取数据，再从网络中获取数据。
IGNORE_CACHE:不从缓存中获取数据，只从网络中获取数据，并且获取到的数据不存入缓存中。这是JavaBaas默认的查询方式。
NETWORK_ELSE_CACHE:先从网络中获取数据，再从缓存中获取数据
NETWORK_ONLY:只从网络中获取数据，从网络中获取的数据会存入缓存中。
```

使用方法如下:

```java
JBQuery<JBObject> jbQuery = JBQuery.getInstance("Data");
jbQuery.setCachePolicy(JBQuery.CachePolicy.CACHE_ELSE_NETWORK);
jbQuery.findInBackground(new FindCallback<JBObject>() {
		@Override
		public void done(List<JBObject> result) {
			Log.d("ObjectTest", "查询成功，查询到的所有条数为" + result.size());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

###4.7  计数查询

如果只是想知道数据库中指定条件的数据个数的话，可以使用计数查询，举例如下:

```java
1.同步查询:
JBQuery jbQuery = JBQuery.getInstance("Test");
try {
		int count = jbQuery.count();
		Log.d("ObjectTest", "查询到的所有条数为" + count);
} catch (JBException e) {
		e.printStackTrace();
}

2.异步查询:
JBQuery jbQuery = JBQuery.getInstance("Test");
//查询'age'字段为'18'的
jbQuery.whereEqualTo("age",18);
jbQuery.countInBackground(new CountCallback() {
		@Override
		public void done(int count) {
			Log.d("ObjectTest", "查询到的所有条数为" + count);
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```
##五、ACL权限控制

JavaBaas提供了ACL(访问控制列表)来管理数据的访问权限。

在默认情况下，数据是可读可写的，在特殊情况下，我们可以设置数据在特定的用户访问的权限。比如，Data表中存入某条数据，此条数据在用户A下才可写，而其它用户都只是可读权限。可以使用以下代码:


```java
JBObject dataObject = new JBObject("Data");
dataObject.put("secretData", "100");
JBAcl acl = new JBAcl();
//此方法为设置数据全部可读
acl.setPublicReadAccess(true);
//第一个参数为用户A的userId，此方法为设置数据只有用户A可写。
acl.setWriteAccess(userAId, true);
dataObject.setACL(acl);

dataObject.saveInBackground(new SaveCallback() {
		@Override
		public void done(JBObject object) {
			Log.d("ObjectTest", "保存成功 ");
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

##六、文件JBFile
###6.1 JBFile
JBFile允许你将文件存储到服务器中，常见的文件类型有:

 * 图片
 * 影像文件
 * 音乐文件
 * 其它二进制数据
 
 
###6.2 文件元数据
###6.4 图像与缩略图获取（待定）
###6.5 进度提示
在JBFile的上传回调中可以获得上传的进度

例如，保存本地的一张图片到云端，并获取上传的进度，可以使用以下代码:

```
JBFile jbFile = new JBFile(new File(Environment.getExternalStorageDirectory() , "demo.jpg"));
jbFile.saveInBackground(new FileUploadCallback() {
		@Override
		public void done(JBFile jbFile) {
			Log.d("ObjectTest", "上传成功");
		}

		@Override
		public void error(JBException e) {
			Log.d("ObjectTest", "上传失败");
		}

		@Override
		public void onProgress(double percent) {
			Log.d("ObjectTest", "上传进度为" + percent);
		}
});
```

##七、用户JBUser

###7.1 JBUser
JavaBaas提供了JBUser类来处理用户相关的功能。

需要注意的是，JBUser继承了JBObject，并在JBObject的基础上增加了一些对用户账户操作的功能。

###7.2 特殊属性
JBUser有几个特定的属性为:

* username:用户的用户名(必须)
* password:用户的密码(必须)
* email:用户的电子邮箱(可选)
* phone:用户的手机号码(可选)

设置属性的方法如下:

```
JBUser jbUser = new JBUser();
//设置用户名             
jbUser.setUsername("ZhangSan");        
//设置密码          
jbUser.setPassword("123456");
//设置手机号   
jbUser.setPhone("110");
```

###7.3 注册
用户注册的示例代码如下:

```
1.同步注册
JBUser jbUser = new JBUser();             
jbUser.setUsername("ZhangSan");               
jbUser.setPassword("123456");
jbUser.setPhone("110");
try {
		jbUser.signUp();
		Log.d("ObjectTest", "注册成功" + jbUser.getId());
} catch (JBException e) {
		e.printStackTrace();
}

2.异步注册
JBUser jbUser = new JBUser();
jbUser.setUsername("ZhangSan");
jbUser.setPassword("123456");
jbUser.setPhone("110");
jbUser.signUpInBackground(new SignUpCallback() {
		@Override
		public void done(JBUser jbUser) {
			Log.d("ObjectTest", "注册成功" + jbUser.getId());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

###7.4 登录
当用户注册成功后，用户可以通过注册的用户名，密码登录到他们的账户。
用户登陆的示例代码如下:

```
//用户名、密码登录
String username = "ZhangSan";
String password = "123456";
JBUser.loginWithUsernameInBackground(username, password, new LoginCallback() {
		@Override
		public void done(JBUser jbUser) {
			Log.d("ObjectTest", "登陆成功" + jbUser.getId());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});

//第三方授权登录(目前仅支持需要传入从第三方平台获取到的accessToken和uid， 并传入登录平台(如QQ、微信微博等)的登陆的方法)
//第一个参数为accessToken，第二个参数为登陆的平台，第三个参数为uid
JBUser.JBThirdPartyUserAuth  jbThirdPartyUserAuth = new JBUser.JBThirdPartyUserAuth("OezXcEiiBSKSxW0eoylIeKszaZo3pw9ABL2UB", JBUser.JBThirdPartyUserAuth.SNS_QQ,"8343726DA09DB9830CC32486A4856E0A");
JBUser.loginWithSnsInBackground(jbThirdPartyUserAuth, new LoginCallback() {
		@Override
		public void done(JBUser jbUser) {
			Log.d("ObjectTest", "登陆成功" + jbUser.getId());
		}

		@Override
		public void error(JBException e) {
			e.printStackTrace();
		}
});
```

###7.5 当前用户
为了避免用户每次打开应用程序的时候都要登陆，可以使用本地缓存的`currentUser`对象

当用户注册或者登陆成功后，本地会生成一个`currentUser`对象，你可以使用此对象来进行判断用户是否登陆:

```
//使用currentUser对象进行判断
JBUser jbUser = JBUser.getCurrentUser();
if (jbUser!=null){
		Log.d("ObjectTest", "currentUser不为空，允许用户使用");
}else {
		Log.d("ObjectTest", "currentUser为空，此时可打开用户注册/登陆的界面");
}

```
清除缓存的`currentUser`对象。

```
JBUser.logout(new LogoutCallback() {
		@Override
		public void onLogout(boolean isSuccess) {
			if (isSuccess){
				Log.d("ObjectTest", "登出成功");
			}
		}
});
```


###7.6 修改密码
假如用户登录成功后，想改变自己的用户信息，可以通过以下代码来更新:

```
JBUser jbUser = JBUser.getCurrentUser();
if (jbUser!=null){
	jbUser.updatePassword("123456", "456789", new RequestCallback() {
			@Override
			public void done() {
				Log.d("ObjectTest", "修改成功");
			}

			@Override
			public void error(JBException e) {
				e.printStackTrace();
			}
	});
}
```
###7.7 SessionToken介绍
`SessionToken`是`JBUser`的一个非常特殊的属性，是
`JBUser`的内建字段。当用户注册成功后，自动生成且唯一。

当用户更改重置密码后，`SessionToken`也会被重置。

`SessionToken`的作用主要有两个方面:

* 服务器用来校验用户登录与否
* 保证在多设备登录同一账号情况下，用户账号安全

##八、设备与推送

`_Installation`是存在于云端的一个用来管理设备信息的默认表。

* `deviceToken` : 设备的唯一标示符
* `deviceType` :  对于Android设备来说，type就是"Android"


##九、调用云方法

有些逻辑是无法通过普通的增删改查数据来实现的，比如记录所有用户打开某界面的次数openCount。这时候，服务端通过提供"云端方法"即可解决这些问题。

假如为了解决上述问题，服务端提供了一个"addOpenCount"云方法，当客户端调用此方法的时候，服务端则会把openCount数量加1。

调用云方法的代码非常简单:

```java
JBCloud.callFunctionInBackground("addOpenCount", null ,new CloudCallback() {
		@Override
		public void done(ResponseEntity responseEntity) {
			Log.d("ObjectTest", "调用成功 " + responseEntity.getMessage());
		}

		@Override
		public void error(JBException e, ResponseEntity responseEntity) {
			Log.d("ObjectTest", "调用失败 " + responseEntity.getMessage());
		}
});
```

有的时候调用云方法还需将参数传递上去，比如我们需要实现一个用户给某产品评分的需求，服务端提供一个"addProductScore"云方法，客户端就可以调用此方法并将所评的分数传上去。

代码如下:

```
HashMap<String, Object> params = new HashMap<>();
params.put("productScore",100);
        
JBCloud.callFunctionInBackground("addProductScore", params, new CloudCallback() {
		@Override
		public void done(ResponseEntity responseEntity) {
			Log.d("ObjectTest", "调用成功 " + responseEntity.getMessage());
		}

		@Override
		public void error(JBException e, ResponseEntity responseEntity) {
			Log.d("ObjectTest", "调用失败 " + responseEntity.getMessage());
		}
});
```
