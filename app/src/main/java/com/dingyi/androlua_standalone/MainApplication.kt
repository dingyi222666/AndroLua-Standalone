package com.dingyi.androlua_standalone

import android.app.Application
import com.dingyi.androlua.vm.LuaGlobal

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        LuaGlobal
            .init(this)


    }


}