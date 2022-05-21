package io.dingyi.androlua_standalone

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.dingyi.androlua_standalone.R
import com.luajava.LuaFunction
import io.github.dingyi.androlua.lib.activity.ProxyLuaActivity
import io.github.dingyi.androlua.vm.LuaGlobal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile

class MainActivity : ProxyLuaActivity(
    luaDir = LuaGlobal.applicationContext.getExternalFilesDir("test")?.parentFile?.absolutePath.toString()
) {



    override fun onCreate(savedInstanceState: Bundle?) {


        //Un Assets File
        val assetsPath = getExternalFilesDir("test")?.parentFile?.absolutePath.toString()

        // get apk path
        val apkPath = this.packageResourcePath

        //create and use apk
        lifecycleScope.launch {

            //run on io thread
            withContext(Dispatchers.IO) {
                ZipFile(apkPath).use { apkFile ->
                    apkFile
                        .fileHeaders
                        .filter { it.fileName.startsWith("assets/") && it.isDirectory.not() }
                        .forEach {
                            apkFile
                                .extractFile(
                                    it,
                                    assetsPath,
                                    it.fileName.substring("assets/".length)
                                )
                        }
                }
            }


            runOnCreate(savedInstanceState)

            val func1 = requireLuaVM()
                .get("print")

            val func2 = requireLuaVM()
                .get("onCreate")

            println(func1)
            println(func2)

        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }


}