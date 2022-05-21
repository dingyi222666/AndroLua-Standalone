## AndoroLua-Standalone

本项目是AndroLua+的单独剥离版，项目只需要引用单个模块，即可低耦合的使用LuaJava,让luajava使用更简易。

提示:本项目并不能直接运行androlua+的代码，java层修改了代码，导致相应的很多函数都需要重写

### 特性

- 低耦合的使用方式

### 使用方式

> 首先接入androlua-standlone到当前项目

```groovy
implementation "io.github.dingyi222666:androlua-standlone:1.0.4"
```

> 在项目的Application的onCreate方法里初始化LuaGlobal

```kotlin
//kotlin
LuaGlobal.init(this)
```

接下来可选多种使用vm的方式

#### SingleLuaVM

SingleLuaVM 是简单低耦合的运行单个lua虚拟机的类，无需为虚拟机设置lua运行路径以及lua目录即可运行lua代码

> 创建类

```kotlin
val vm = SingleLuaVM()
```

> 运行代码

```kotlin

//假设 当前类有个print方法，接收一个String
vm.set("javaObject",this)
//调用print方法
vm.doString("javaObject.print 'hello luavm' ")

```

#### LuaActivityVM

LuaActivityVM 是 类似于AndroLua+ LuaActivity的 一类 VM对象，可以传递activity，使得lua拥有操作activity的能力

> 创建类

目前只推荐通过继承ProxyActivity来使用该类型的VM

以下代码就继承了ProxyLuaActivity,并且指定了默认的运行lua路径

```kotlin
//import zip4j
import net.lingala.zip4j.ZipFile

class MainActivity : ProxyLuaActivity(
    luaDir = LuaGlobal.applicationContext.getExternalFilesDir("test")?.parentFile?.absolutePath.toString()
)
```

> 覆盖获取Lua运行路径方法

接下来你可以覆盖getRunLuaPath方法 返回运行的lua文件的路径 如果文件不为绝对路径，那么久采用相对路径，即luadir+路径 该方法仅会在runOnCreate时调用

```kotlin
 override fun getRunLuaPath(): String {
    return "main.lua"
}
```

> 覆盖onCreate方法

然后覆盖onCreate方法 实现你的解压lua文件逻辑

这里需要注意是解压完之后需要调用 runOnCreate(savedInstanceState) 方法来实现默认的调用lua文件逻辑

```kotlin
 override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)


    //Un Assets File
    val assetsPath = getExternalFilesDir("test")?.parentFile?.absolutePath.toString()

    // get apk path
    val apkPath = this.packageResourcePath

    //create and use apk
    lifecycleScope.launch {
        //run on io thread
        withContext(Dispatchers.IO) {
            ZipFile(apkPath).use { apkFile ->
                apkFile
                    .fileHeaders
                    .filter { it.fileName.startsWith("assets/") && it.isDirectory.not() }
                    .forEach {
                        apkFile
                            .extractFile(
                                it,
                                assetsPath,
                                it.fileName.substring("assets/".length)
                            )
                    }
            }
        }
        runOnCreate(savedInstanceState)
    }

}
```


