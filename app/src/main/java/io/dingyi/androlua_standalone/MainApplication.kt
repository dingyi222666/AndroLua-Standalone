package io.dingyi.androlua_standalone

import android.app.Application
import io.github.dingyi.androlua.vm.LuaGlobal

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        LuaGlobal
            .init(this)


        CrashHandler.init(this)
    }


}