package com.dingyi.androlua.vm

/**
 * 代表可回收的对象
 */
interface LuaGcable {

    /**
     * 是否已经gc
     */
    fun isGc():Boolean

    /**
     * 执行gc
     */
    fun gc()
}