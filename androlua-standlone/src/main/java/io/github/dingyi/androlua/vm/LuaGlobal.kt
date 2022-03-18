package io.github.dingyi.androlua.vm

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import com.androlua.LuaGcable
import com.luajava.LuaException
import com.luajava.LuaState
import com.luajava.LuaTable
import io.github.dingyi.androlua.loader.LuaDexLoader
import java.io.File


object LuaGlobal : LuaVM {


    lateinit var libDir: String
        private set
    lateinit var odexDir: String
        private set
    lateinit var luaMdDir: String
        private set
    private var luaCPath: String? = null
    private var luaLPath: String? = null
    private var luaExtDir: String? = null

    private lateinit var globalContext: Application
    private lateinit var mSharedPreferences: SharedPreferences

    fun init(application: Application) {
        globalContext = application
        mSharedPreferences = getSharedPreferences(applicationContext)

        odexDir = application.getDir("odex", Context.MODE_PRIVATE).getAbsolutePath();
        libDir = application.getDir("lib", Context.MODE_PRIVATE).getAbsolutePath();
        luaMdDir = application.getDir("lua", Context.MODE_PRIVATE).getAbsolutePath();

        luaCpath = application.getDir(
            "lib",
            Context.MODE_PRIVATE
        ).absolutePath + application.applicationInfo.nativeLibraryDir + "/lib?.so" + ";" + "/lib?.so"
    }


    val applicationContext: Application
        get() = globalContext


    override fun init() {
        //
    }



    override fun doString(code: String, vararg args: Any?): Any? {
        return null
    }


    override fun call(func: String, vararg args: Any?) {}
    override fun runFunc(func: String, vararg args: Any?): Any? {
        return null
    }

    override fun set(name: String?, value: Any?) {}

    override fun getLuaPath(): String? {
        return null
    }



    override fun getLuaPath(path: String): String? {
        return null
    }

    override fun getLuaPath(p0: String?, p1: String?): String? {
        return null
    }


    override fun getLuaDir(): String? {
        return null
    }

    override fun getLuaDir(dir: String?): String? {
        return null
    }

    override fun getLuaExtDir(): String? {
        return luaExtDir
    }

    override fun getLuaExtDir(dir: String?): String? {
        return "$luaExtDir/$dir"
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
        if (luaLPath == null) {
            throw LuaException("No init LuaLPath,please init it")
        }
        return checkNotNull(luaLPath)
    }

    override fun getLuaCpath(): String {
        if (luaCPath == null) {
            throw LuaException("No init LuaCPath,please init it")
        }
        return checkNotNull(luaCPath)
    }

    override fun setLuaLpath(path: String) {
        luaLPath = path
    }

    override fun setLuaCpath(path: String) {
        luaCPath = path
    }

    override fun getContext(): Context {
        return globalContext
    }

    override fun getLuaState(): LuaState? {
        return null
    }

    override fun getSharedData(): Any {
        return mSharedPreferences.all
    }

    override fun doFile(path: String, vararg arg: Any?): Any? {
        return null
    }

    override fun sendMsg(msg: String) {}

    override fun sendError(title: String, msg: Exception) {}

    override fun getWidth(): Int {
        return globalContext.resources.displayMetrics.widthPixels
    }

    override fun getHeight(): Int {
        return globalContext.resources.displayMetrics.heightPixels
    }


    override fun getSharedData(key: String?): Any? {
        return mSharedPreferences.all[key]
    }

    override fun getSharedData(key: String?, def: Any?): Any? {
        return mSharedPreferences.all.getOrElse(key) { def }
    }

    override fun setSharedData(key: String?, value: Any?): Boolean {
        val edit = mSharedPreferences.edit()

        when (value) {
            null -> edit.remove(key)
            is Long -> edit.putLong(key,value)
            is Int -> edit.putInt(key,value)
            is Float -> edit.putFloat(key,value)
            is Set<*> -> edit.putStringSet(key,value as Set<String>)
            is LuaTable<*,*> -> edit.putStringSet(key,value.values as HashSet<String>)
            is Boolean -> edit.putBoolean(key,value)
            else -> return false
        }

        edit.apply()
        return true
    }

    override fun registerGcable(obj: LuaGcable) {}

    override fun getLuaDexLoader(): LuaDexLoader? {
        return null
    }

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val deContext = context.createDeviceProtectedStorageContext()
            if (deContext != null) PreferenceManager.getDefaultSharedPreferences(deContext) else PreferenceManager.getDefaultSharedPreferences(
                context
            )
        } else {
            PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

}