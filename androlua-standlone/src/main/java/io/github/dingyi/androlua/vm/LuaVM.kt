package io.github.dingyi.androlua.vm

import android.util.Log
import com.androlua.LuaGcable
import io.github.dingyi.androlua.loader.LuaDexLoader
import java.util.*

/**
 * LuaVM的抽象接口
 */
abstract class LuaVM : LuaContext {

    /**
     * 初始化lua虚拟机,部分虚拟机的实现可能不需要执行该方法，请以实现的为准
     */
    open fun init() {

    }


    /**
     * 运行lua字符串
     */
    abstract fun doString(code: String, vararg args: Any?): Any?

    private val messageListener = mutableListOf<VMMessageListener>()

    /**
     * 注册vm的消息监听
     */
    fun registerMessageListener(vmMessageListener: VMMessageListener) {
        messageListener.add(vmMessageListener)
    }

    /**
     * 取消注册vm的消息监听
     */
    fun unregisterMessageListener(vmMessageListener: VMMessageListener) {
        messageListener.remove(vmMessageListener)
    }

    /**
     * 向vm发送一条消息
     */
    override fun sendMsg(msg: String) {
        messageListener.forEach { it.onShowMessage(msg) }
    }

    /**
     * 向vm发送一条错误消息
     */
    override fun sendError(title: String, msg: Exception) {
        messageListener.forEach { it.onShowErrorMessage(title, msg) }
        throw msg
    }

    interface VMMessageListener {
        fun onShowMessage(msg: String)
        fun onShowErrorMessage(title: String, exception: Exception)
    }

    private val gcList: MutableList<LuaGcable>
        get() = mutableListOf()

    /**
     * 注册gc对象
     */
    override fun registerGcable(obj: LuaGcable) {
        gcList.add(obj)
    }


    fun getLibrarys():Map<String,String> {
        return getLuaDexLoader()?.librarys ?: mapOf()
    }

    abstract fun getLuaDexLoader(): LuaDexLoader?


    /**
     * 请在离开activity或者不使用虚拟机的时候执行
     */
    open fun destroy() {
        while (gcList.isNotEmpty()) {
            gcList.removeAt(0)
                .gc()
        }
    }

}