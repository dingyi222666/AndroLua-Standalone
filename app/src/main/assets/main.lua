---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by dingyi.
--- DateTime: 2022/3/17 18:44
---


local Log = luajava.bindClass "android.util.Log"
local Toast = luajava.bindClass "android.widget.Toast"

function onCreate()
    Log.d("lua","onCreate")
    print("onCreate")
end

function onDestroy()
    Log.d("lua","onDestroy")
    print("onDestroy")
end

function onResume()
    Log.d("lua","onResume")
    print("onResume")
end


