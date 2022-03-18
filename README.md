## AndoroLua-Standalone

本项目是AndroLua+的单独剥离版，项目只需要引用单个模块，即可低耦合的使用LuaJava,让结合luajava使用更简易。

### 特性

- 低耦合的使用方式

### 使用方式

1.接入androlua-standlone到当前模块

```groovy

```

2. 在项目的Application的onCreate方法里初始化LuaGlobal

```kotlin
LuaGlobal.init(this)
```

```java
LuaGlobal
        .INSTANCE
        .init(this);
```

3.如果需要运行脚本文件 请提前解压脚本文件到本地文件目录,这里不做演示


4.初始化Lua虚拟机

```kotlin
val luaDirPath = ""
val luaVM = LuaVM(luaDirPath)

//注册消息监听事件
luaVM
    .registerMessageListener(this)


```

5.运行
```kotlin
 
//run main.lua
luaVM
    .init(this@MainActivity, "$luaDirPath/main.lua")
```