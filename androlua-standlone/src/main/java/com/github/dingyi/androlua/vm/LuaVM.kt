package com.github.dingyi.androlua.vm

import android.app.Activity
import com.androlua.LuaGcable
import com.github.dingyi.androlua.lib.func.LuaPrint
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
        return File(luaPath, path).absolutePath
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
        var loadPath = path
        var ok = 0

        if (path[0] !== '/') loadPath = getLuaDir().toString() + "/" + path
        luaState.top = 0
        loadLuaPath = loadPath
        ok = luaState.LloadFile(loadPath)
        if (ok == 0) {
            luaState.getGlobal("debug")
            luaState.getField(-1, "traceback")
            luaState.remove(-2)
            luaState.insert(-2)
            for (element in args) {
                luaState.pushObjectValue(element)
            }
            ok = luaState.pcall(args.size, 1, -2 - args.size)
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

    fun init() {

        luaState.openBase()
        luaState.openDebug()
        luaState.openLibs()
        luaState.openLuajava()
        luaState.openPackage()

        luaState.pushContext(this)

        val print = LuaPrint(this)
        print.register("print")

    }

    fun init(activity: Activity?, runLuaPath: String) {

        luaState.openBase()
        luaState.openDebug()
        luaState.openLibs()
        luaState.openLuajava()
        luaState.openPackage()

        loadLuaPath = runLuaPath
        if (activity != null) {

            luaState.pushJavaObject(activity);
            luaState.setGlobal("activity");
            luaState.getGlobal("activity");
            luaState.setGlobal("this");


        }
        luaState.pushContext(this)

        luaState.getGlobal("luajava");

        getLuaExtDir()?.let { luaExtDir ->
            luaState.pushString(luaExtDir);
            luaState.setField(-2, "luaextdir");
        }

        getLuaDir()?.let {
            luaState.pushString(it);
            luaState.setField(-2, "luadir");
        }

        luaState.pushString(luaPath);
        luaState.setField(-2, "luapath");
        luaState.pop(1);

        val print = LuaPrint(this)
        print.register("print")


        doFile(luaPath.toString())
    }


    override fun runFunc(func: String, vararg args: Any?): Any? {
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