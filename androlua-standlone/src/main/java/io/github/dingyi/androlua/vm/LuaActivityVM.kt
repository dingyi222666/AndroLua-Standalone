package io.github.dingyi.androlua.vm

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.androlua.LuaGcable
import io.github.dingyi.androlua.lib.func.LuaPrint
import com.luajava.LuaException
import com.luajava.LuaState
import com.luajava.LuaStateFactory
import io.github.dingyi.androlua.loader.LuaDexLoader
import java.io.File
import java.lang.ref.WeakReference

/**
 * LuaActivity VM,支持和LuaVM交互,自动注册print函数
 */
class LuaActivityVM(
    private val luaDir: String
) : LuaVM.VMMessageListener, LuaVM() {

    private var initActivity: WeakReference<Activity> = WeakReference(null)
    private val luaState = LuaStateFactory.newLuaState()

    private lateinit var luaDexLoader : LuaDexLoader

    private var loadLuaPath: String? = null

    override fun getLuaState(): LuaState {
        return luaState
    }

    override fun getSharedData(): Any {
        return LuaGlobal.sharedData
    }

    override fun getSharedData(p0: String?): Any? {
        return LuaGlobal.getSharedData(p0)
    }

    override fun getSharedData(p0: String?, p1: Any?): Any? {
        return LuaGlobal.getSharedData(p0, p1)
    }

    override fun getWidth(): Int {
        return LuaGlobal.width
    }

    override fun getLuaPath(p0: String?, p1: String?): String {
        return luaDir + File.separator + p0 + File.separator + p1
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

    override fun setSharedData(p0: String?, p1: Any?): Boolean {
        return LuaGlobal.setSharedData(p0, p1)
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

    override fun getLuaDexLoader(): LuaDexLoader? {
        return luaDexLoader
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

    override fun getContext(): Context {
        return LuaGlobal.context
    }

    override fun getHeight(): Int {
        return LuaGlobal.height
    }


    override fun doString(funcSrc: String, vararg args: Any?): Any? {

        luaState.setTop(0)
        var ok = luaState.LloadString(funcSrc)
        if (ok == 0) {
            luaState.getGlobal("debug")
            luaState.getField(-1, "traceback")
            luaState.remove(-2)
            luaState.insert(-2)
            val l = args.size
            for (i in 0 until l) {
                luaState.pushObjectValue(args[i])
            }
            ok = luaState.pcall(l, 1, -2 - l)
            if (ok == 0) {
                return luaState.toJavaObject(-1)
            }
        }

        throw LuaException(getErrorReason(ok).toString() + ": " + luaState.toString(-1))

    }


    override fun get(key: String): Any? {
        synchronized(luaState) {
            luaState.getGlobal(key)
            return luaState.toJavaObject(-1)
        }
    }

    override fun set(name: String, value: Any?) {
        synchronized(luaState) {
            try {
                luaState.pushObjectValue(value);
                luaState.setGlobal(name);
            } catch (e: LuaException) {
                sendError("setField", e);
            }
        }
    }

    fun init(activity: Activity?, runLuaPath: String) {
        super.init()
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


            initActivity = WeakReference(activity)

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

        luaState.pushString(getLuaPath());
        luaState.setField(-2, "luapath");
        luaState.pop(1);


        registerMessageListener(this)

        val print = LuaPrint(this)
        print.register("print")


        luaDexLoader = LuaDexLoader(this)

        // load lib dex
        luaDexLoader.loadLibs()


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

    override fun onShowMessage(msg: String) {
        Log.e("Lua", msg)
        Toast.makeText(initActivity.get() ?: context, msg, Toast.LENGTH_LONG).show()
    }

    override fun onShowErrorMessage(title: String, exception: Exception) {
        Toast.makeText(
            LuaGlobal.applicationContext,
            exception.stackTraceToString(),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun destroy() {
        super.destroy()
        unregisterMessageListener(this)

        luaState.gc(LuaState.LUA_GCCOLLECT, 1);

    }

}