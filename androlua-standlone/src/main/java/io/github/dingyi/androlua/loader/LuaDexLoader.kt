package io.github.dingyi.androlua.loader

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.content.res.Resources.Theme
import com.androlua.LuaApplication
import com.androlua.LuaDexClassLoader
import com.androlua.LuaResources
import com.androlua.LuaUtil
import com.luajava.LuaException
import dalvik.system.DexClassLoader
import io.github.dingyi.androlua.vm.LuaContext
import io.github.dingyi.androlua.vm.LuaGlobal
import java.io.File


class LuaDexLoader(context: LuaContext) {

    val classLoaders = ArrayList<ClassLoader>()

    val librarys = HashMap<String, String>()

    private val mContext: LuaContext = context
    private val luaDir: String = context.luaDir

    private val odexDir: String
    fun loadApp(pkg: String): LuaDexClassLoader? {
        try {
            var dex = dexCache[pkg]
            if (dex == null) {
                val manager: PackageManager = mContext.context.packageManager
                val info = manager.getPackageInfo(pkg, 0).applicationInfo
                dex = LuaDexClassLoader(
                    info.publicSourceDir,
                    LuaGlobal.odexDir,
                    info.nativeLibraryDir,
                    mContext.context.classLoader
                )
                dexCache[pkg] = dex
            }
            if (!classLoaders.contains(dex)) {
                classLoaders.add(dex)
            }
            return dex
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(LuaException::class)
    fun loadLibs() {
        val libs: Array<File> =
            File(mContext.getLuaDir().toString() + "/libs").listFiles()
                ?: return
        for (f in libs) {
            if (f.isDirectory) continue
            if (f.absolutePath.endsWith(".so")) loadLib(f.name) else loadDex(f.absolutePath)
        }
    }

    @Throws(LuaException::class)
    fun loadLib(name: String) {
        var fn = name
        val i = name.indexOf(".")
        if (i > 0) fn = name.substring(0, i)
        if (fn.startsWith("lib")) fn = fn.substring(3)
        val libDir: String =
            mContext.context.getDir(fn, Context.MODE_PRIVATE).absolutePath
        val libPath = "$libDir/lib$fn.so"
        var f = File(libPath)
        if (!f.exists()) {
            f = File("$luaDir/libs/lib$fn.so")
            if (!f.exists()) throw LuaException("can not find lib $name")
            LuaUtil.copyFile("$luaDir/libs/lib$fn.so", libPath)
        }
        librarys[fn] = libPath
    }

    @Throws(LuaException::class)
    fun loadDex(path: String): DexClassLoader {
        var path = path
        var dex = dexCache[path]
        if (dex == null) dex = loadApp(path)
        if (dex == null) {
            val name = path
            if (path[0] != '/') path = "$luaDir/$path"
            if (!File(path).exists()) path += if (File("$path.dex").exists()) ".dex" else if (File(
                    "$path.jar"
                ).exists()
            ) ".jar" else throw LuaException("$path not found")
            var id = LuaUtil.getFileMD5(path)
            if (id != null && id == "0") id = name
            dex = dexCache[id]
            if (dex == null) {
                dex = LuaDexClassLoader(
                    path,
                    odexDir,
                    LuaGlobal.applicationContext.applicationInfo.nativeLibraryDir,
                    mContext.getContext().getClassLoader()
                )
                dexCache[id] = dex
            }
        }
        if (!classLoaders.contains(dex)) {
            classLoaders.add(dex)
            path = dex.dexPath
        }
        return dex
    }


    companion object {
        private val dexCache = HashMap<String?, LuaDexClassLoader>()
    }

    init {
        val app = LuaGlobal
        //localDir = app.getLocalDir();
        odexDir = app.odexDir
    }
}
