package io.github.dingyi.androlua.lib.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.dingyi.androlua.vm.LuaActivityVM
import io.github.dingyi.androlua.vm.LuaGlobal
import io.github.dingyi.androlua.vm.LuaVM

abstract class ProxyLuaActivity(
    private val luaDir: String,
    private val createLuaVM: Boolean = true,
) : AppCompatActivity() {

    private var luaActivityVM: LuaActivityVM? = null


    protected fun requireLuaVM(): LuaActivityVM {
        return checkNotNull(luaActivityVM) { "luaActivityVM not create" }
    }

    protected fun createLuaVM() {
        luaActivityVM = LuaActivityVM(luaDir)
    }

    protected fun getLuaVM(): LuaActivityVM? {
        return luaActivityVM
    }

    /**
     * 该方法请在适当的时候在onCreate中调用
     */
    fun runOnCreate(savedInstanceState: Bundle?) {
        if (createLuaVM) {
            createLuaVM()
            requireLuaVM().init(this, getRunLuaPath())
        }
        getLuaVM()?.runFunc("onCreate", savedInstanceState)
    }



    /**
     * 获取运行的lua路径，默认为main.lua
     * 在onCreate后面调用
     */
    protected fun getRunLuaPath() = "main.lua"

    override fun onStart() {
        super.onStart()
        getLuaVM()?.runFunc("onStart")
    }

    override fun onResume() {
        super.onResume()
        getLuaVM()?.runFunc("onResume")
    }

    override fun onPause() {
        super.onPause()
        getLuaVM()?.runFunc("onPause")
    }

    override fun onStop() {
        super.onStop()
        getLuaVM()?.runFunc("onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        getLuaVM()?.runFunc("onDestroy")
        getLuaVM()?.destroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        getLuaVM()?.runFunc("onSaveInstanceState", outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        getLuaVM()?.runFunc("onRestoreInstanceState", savedInstanceState)
    }

    override fun onBackPressed() {
        getLuaVM()?.runFunc("onBackPressed")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        getLuaVM()?.runFunc("onRequestPermissionsResult", requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getLuaVM()?.runFunc("onActivityResult", requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getLuaVM()?.runFunc("onNewIntent", intent)
    }

    override fun onRestart() {
        super.onRestart()
        getLuaVM()?.runFunc("onRestart")
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return (getLuaVM()?.runFunc("onCreateOptionsMenu", menu) as Boolean?)
            ?: super.onCreateOptionsMenu(menu)
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val result = (getLuaVM()?.runFunc("onKeyDown", keyCode, event) as Boolean?)
        return result ?: super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return (getLuaVM()?.runFunc("onKeyUp", keyCode, event) as Boolean?) ?: super.onKeyDown(keyCode, event)
    }


}