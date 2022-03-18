## AndoroLua-Standalone

本项目是AndroLua+的单独剥离版，项目只需要引用单个模块，即可低耦合的使用LuaJava,让luajava使用更简易。

提示:本项目并不能直接运行androlua+的代码，java层修改了代码，导致相应的很多函数都需要重写

### 特性

- 低耦合的使用方式

### 使用方式

> 1.首先接入androlua-standlone到当前项目

```groovy
implementation "io.github.dingyi222666:androlua-standlone:1.0.1"
```

> 2. 在项目的Application的onCreate方法里初始化LuaGlobal

```kotlin
//kotlin
LuaGlobal.init(this)
```

接下来可选多种使用vm的方式

#### SingleLuaVM

SingleLuaVM 是简单低耦合的运行单个lua虚拟机的类，无需为虚拟机设置lua运行路径以及lua目录即可运行lua代码

> 1.创建类

```kotlin
val vm = SingleLuaVM()
```

> 2.运行代码

```kotlin

//假设 当前类有个print方法，接收一个String
vm.set("javaObject",this)
//调用print方法
vm.doString("javaObject.print 'hello luavm' ")

```