package io.github.dingyi.androlua.vm

import android.content.Context
import com.luajava.LuaException
import com.luajava.LuaState
import com.luajava.LuaStateFactory
import io.github.dingyi.androlua.lib.func.LuaPrint
import io.github.dingyi.androlua.loader.LuaDexLoader
import java.io.File

/**
 * 简单的lua vm，不支持和activity交互
 */

class SingleLuaVM(
) :LuaVM() {

    private val luaState = LuaStateFactory.newLuaState()

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
        return File(luaPath, path).absolutePath
    }


    override fun get(key: String): Any? {
        synchronized(luaState) {
            luaState.getGlobal(key)
            return luaState.toJavaObject(-1)
        }
    }

    override fun getLuaDir(): String? {
        return LuaGlobal.getLuaDir()
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

    override fun getLuaDexLoader(): LuaDexLoader? {
        return null
    }

    override fun init() {
        super.init()
        luaState.openBase()
        luaState.openDebug()
        luaState.openLibs()
        luaState.openLuajava()
        luaState.openPackage()


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


    override fun destroy() {
        super.destroy()
        luaState.gc(LuaState.LUA_GCCOLLECT, 1);

    }


}