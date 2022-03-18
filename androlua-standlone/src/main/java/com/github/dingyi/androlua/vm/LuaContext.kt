package com.github.dingyi.androlua.vm

import android.content.Context
import com.androlua.LuaGcable
import com.luajava.LuaState
import java.util.ArrayList


/**
 * Lua的Context对象
 */
interface LuaContext : com.androlua.LuaContext {

    /**
     * 获取类加载器
     */
    override fun getClassLoaders(): ArrayList<ClassLoader>? = null

    /**
     * 调用指定函数
     */
    override fun call(func: String, vararg args: Any?) {
        runFunc(func, args)
    }

    fun runFunc(func: String, vararg args: Any?): Any?

    override fun set(name: String?, value: Any?)


    fun setLuaLpath(path: String)

    fun setLuaCpath(path: String)


    fun doFile(p0: String?): Any {
        return doFile(p0, null)
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


    override fun regGc(p0: com.androlua.LuaGcable) {
        registerGcable(p0)
    }

    override fun getGlobalData(): MutableMap<Any?, Any?> {
        return mutableMapOf()
    }

    /**
     * 注册gc对象
     */
    fun registerGcable(obj: LuaGcable)

}