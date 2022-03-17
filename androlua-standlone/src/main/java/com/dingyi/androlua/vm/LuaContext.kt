package com.dingyi.androlua.vm

import android.content.Context
import com.luajava.LuaState


/**
 * Lua的Context对象
 */
interface LuaContext {

    /**
     * 获取类加载器
     */
    fun getClassLoaders(): List<ClassLoader>? = null

    /**
     * 调用指定函数
     */
    fun call(func: String, vararg args: Any?) : Any?

    operator fun set(name: String?, value: Any?)

    fun getLuaPath(): String?

    fun getLuaPath(path: String): String?


    fun getLuaDir(): String?

    fun getLuaDir(dir: String?): String?

    fun getLuaExtDir(): String?

    fun getLuaExtDir(dir: String?): String?

    fun setLuaExtDir(dir: String?)

    fun getLuaExtPath(path: String?): String?

    fun getLuaExtPath(dir: String?, name: String?): String?

    fun getLuaLpath(): String

    fun getLuaCpath(): String


    fun setLuaLpath(path: String)

    fun setLuaCpath(path: String)


    fun getContext(): Context

    fun getLuaState(): LuaState?

    fun doFile(path: String, vararg arg: Any?): Any?

    fun doFile(path: String) {
        doFile(path, null)
    }

    //生成错误信息
    fun getErrorReason(error: Int): String {
        when (error) {
            6 -> return "error error"
            5 -> return "GC error"
            4 -> return "Out of memory"
            3 -> return "Syntax error"
            2 -> return "Runtime error"
            1 -> return "Yield error"
        }
        return "Unknown error $error"
    }


    fun sendMsg(msg: String)

    fun sendError(title: String, msg: Exception)

    fun getWidth(): Int

    fun getHeight(): Int


    fun getSharedData(key: String?): Any?

    fun getSharedData(key: String?, def: Any?): Any?

    fun setSharedData(key: String?, value: Any?): Boolean

    /**
     * 注册gc对象
     */
    fun registerGcable(obj: LuaGcable)

}