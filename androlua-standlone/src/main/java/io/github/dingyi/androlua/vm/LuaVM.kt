package io.github.dingyi.androlua.vm

import com.androlua.LuaGcable
import io.github.dingyi.androlua.loader.LuaDexLoader
import java.util.*

/**
 * LuaVM的抽象接口
 */
interface LuaVM : LuaContext {

    /**
     * 初始化lua虚拟机,部分虚拟机的实现可能不需要执行该方法，请以实现的为准
     */
    fun init() {

    }



    /**
     * 运行lua字符串
     */
    fun doString(code: String, vararg args: Any?): Any?

    private
    val messageListener: MutableList<VMMessageListener>
        get() = mutableListOf()

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

    override fun sendMsg(msg: String) {
        messageListener.forEach { it.onShowMessage(msg) }
    }

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
    fun destroy() {
        while (gcList.isNotEmpty()) {
            gcList.removeAt(0)
                .gc()
        }
    }

}