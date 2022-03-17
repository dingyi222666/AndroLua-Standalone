package com.dingyi.androlua_standalone

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dingyi.androlua.vm.LuaGlobal
import com.dingyi.androlua.vm.LuaVM
import com.dingyi.androlua_standalone.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile

class MainActivity : AppCompatActivity(), LuaVM.VMMessageListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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



            //create lua vm
            val luaVM = LuaVM(assetsPath)

            //set base path
            luaVM.apply {

                setLuaCpath(
                    getDir("lib", Context.MODE_PRIVATE).absolutePath
                            + applicationInfo.nativeLibraryDir + "/lib?.so" + ";" + "/lib?.so"
                )

            }


            luaVM
                .registerMessageListener(this@MainActivity)

            //run main.lua
            luaVM
                .init(this@MainActivity, "main.lua")


        }
    }

    override fun onShowMessage(msg: String) {
        Toast
            .makeText(
                this, msg, Toast
                    .LENGTH_LONG
            )
            .show()
    }

    override fun onShowErrorMessage(title: String, exception: Exception) {

    }
}