package com.dingyi.androlua.vm

import android.app.Activity
import com.android.cglib.dx.a.b.L
import com.dingyi.androlua.lib.func.LuaPrint
import com.luajava.JavaFunction
import com.luajava.LuaException
import com.luajava.LuaState
import com.luajava.LuaStateFactory
import java.io.File


class LuaVM(
    private val luaDir: String
) : LuaContext by LuaGlobal {

    private val luaState = LuaStateFactory.newLuaState()

    private var loadLuaPath: String? = null

    private val messageListener = mutableListOf<VMMessageListener>()

    override fun getLuaState(): LuaState {
        return luaState
    }

    override fun getLuaPath(): String? {
        return loadLuaPath
    }

    private var luaCPath: String? = null
    private var luaLPath: String? = null
    private var luaExtDir: String? = null


    override fun getLuaPath(path: String): String? {
        return File(getLuaPath(), path).absolutePath
    }


    override fun getLuaDir(): String? {
        return luaDir
    }

    override fun getLuaDir(dir: String?): String {
        return File(luaDir, dir).absolutePath
    }

    override fun getLuaExtDir(): String? {
        return luaExtDir ?: LuaGlobal.getLuaExtDir()
    }

    override fun getLuaExtDir(dir: String?): String? {
        return "${getLuaExtDir()}/$dir"
    }

    override fun setLuaExtDir(dir: String?) {
        luaExtDir = dir
    }

    override fun getLuaExtPath(path: String?): String? {
        return File(getLuaExtDir(), path).absolutePath
    }

    override fun getLuaExtPath(dir: String?, name: String?): String? {
        return File(getLuaExtDir(dir), name).absolutePath
    }


    override fun getLuaLpath(): String {
        return luaLPath ?: LuaGlobal.getLuaLpath()
    }

    override fun getLuaCpath(): String {

        return luaCPath ?: LuaGlobal.getLuaCpath()
    }

    override fun setLuaLpath(path: String) {
        luaLPath = path
    }

    override fun setLuaCpath(path: String) {
        luaCPath = path
    }

    override fun doFile(path: String, vararg args: Any?): Any? {
        loadLuaPath = path
        var loadPath = path
        var ok = 0

        if (path[0] !== '/') loadPath = getLuaDir().toString() + "/" + path
        luaState.top = 0
        ok = luaState.LloadFile(loadPath)
        if (ok == 0) {
            luaState.getGlobal("debug")
            luaState.getField(-1, "traceback")
            luaState.remove(-2)
            luaState.insert(-2)
            val l: Int = args.size
            for (i in 0 until l) {
                luaState.pushObjectValue(args[i])
            }
            ok = luaState.pcall(l, 1, -2 - l)
            if (ok == 0) {
                return luaState.toJavaObject(-1)
            }
        }

        throw LuaException(getErrorReason(ok) + ": " + luaState.toString(-1));

    }


    private val gcList = mutableListOf<LuaGcable>()

    override fun registerGcable(obj: LuaGcable) {
        gcList.add(obj)
    }

    fun init(activity: Activity?,runLuaPath:String) {
        loadLuaPath = runLuaPath
        if (activity!=null) {

            luaState.pushJavaObject(activity);
            luaState.setGlobal("activity");
            luaState.getGlobal("activity");
            luaState.setGlobal("this");

            luaState.pushString("_LuaContext");
            luaState.pushJavaObject(this);
            luaState.setTable(-1001000);
        }
        luaState.getGlobal("luajava");
        luaState.pushString(getLuaExtDir());
        luaState.setField(-2, "luaextdir");
        luaState.pushString(getLuaDir());
        luaState.setField(-2, "luadir");
        luaState.pushString(getLuaPath());
        luaState.setField(-2, "luapath");
        luaState.pop(1);

        val print = LuaPrint(this)
        print.register("print")


        doFile(getLuaPath().toString())
    }


    override fun call(func: String, vararg args: Any?): Any? {
        synchronized(luaState) {
            try {
                luaState.top = 0
                luaState.pushGlobalTable()
                luaState.pushString(func)
                luaState.rawGet(-2)
                if (luaState.isFunction(-1)) {
                    luaState.getGlobal("debug")
                    luaState.getField(-1, "traceback")
                    luaState.remove(-2)
                    luaState.insert(-2)
                    val l = args.size
                    for (i in 0 until l) {
                        luaState.pushObjectValue(args[i])
                    }
                    val ok = luaState.pcall(l, 1, -2 - l)
                    if (ok == 0) {
                        return luaState.toJavaObject(-1)
                    }
                    throw LuaException(getErrorReason(ok) + ": " + luaState.toString(-1))
                }
            } catch (e: LuaException) {
                sendError(func, e)

            }
        }
        return null
    }

    fun destroy() {
        while (gcList.isNotEmpty()) {
            gcList.removeAt(0)
                .gc()
        }
    }


    fun registerMessageListener(vmMessageListener: VMMessageListener) {
        messageListener.add(vmMessageListener)
    }

    fun unregisterMessageListener(vmMessageListener: VMMessageListener) {
        messageListener.remove(vmMessageListener)
    }

    override fun sendMsg(msg: String) {
        messageListener.forEach { it.onShowMessage(msg) }
    }

    override fun sendError(title: String, msg: Exception) {
        messageListener.forEach { it.onShowErrorMessage(title,msg) }
        throw msg
    }

    interface VMMessageListener {
        fun onShowMessage(msg: String)
        fun onShowErrorMessage(title: String, exception: Exception)
    }

}