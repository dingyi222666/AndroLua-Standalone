package com.dingyi.androlua_standalone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.github.dingyi.androlua.vm.LuaVM
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

            luaVM
                .registerMessageListener(this@MainActivity)


            //run main.lua
            luaVM
                .init(this@MainActivity, "$assetsPath/main.lua")


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