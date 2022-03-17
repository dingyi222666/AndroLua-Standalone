package com.dingyi.androlua.lib.func

import com.android.cglib.dx.a.b.L
import com.dingyi.androlua.vm.LuaContext
import com.luajava.JavaFunction


class LuaPrint(
    private val luaContext: LuaContext
) : JavaFunction(luaContext.getLuaState()) {


    private val luaState = checkNotNull(luaContext.getLuaState())

    private val output = StringBuilder()

    override fun execute(): Int {
        if (luaState.top < 2) {
            luaContext.sendMsg("")
            return 0
        }
        for (i in 2..luaState.top) {
            val contentType = luaState.type(i)
            var valStringContext:String? = null
            val contentTypeName = luaState.typeName(i)
            valStringContext = if (contentTypeName == "userdata") {
                val obj = luaState.toJavaObject(i)
                obj?.toString()
            } else if (contentTypeName == "boolean") {
                if (luaState.toBoolean(i)) "true" else "false"
            } else {
                luaState.toString(i) ?: null
            }
            if (valStringContext == null) valStringContext = contentTypeName
            output.append("\t")
            output.append(valStringContext)
            output.append("\t")
        }
        luaContext.sendMsg(output.toString().substring(1, output.length - 1))
        output.setLength(0)
        return 0
    }
}