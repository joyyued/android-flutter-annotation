package com.joyy.neza_compiler.config

/**
 * @author: Jiang Pengyong
 * @date: 2021/8/11 9:59 上午
 * @email: 56002982@qq.com
 * @des: flutter 的配置
 */
object FlutterConfig {

    // ======================= Engine =============================
    private val engineList = ArrayList<String>()

    fun setEngineList(engine: String) {
        engineList.clear()
        engineList.add(engine)
    }

    fun setEngineList(engineArray: Array<String>) {
        engineList.clear()
        engineList.addAll(engineArray)
    }

    fun getEngineList() = engineList

    fun getDefaultEngine(): String? {
        return if (engineList.isEmpty()) {
            null
        } else {
            engineList.first()
        }
    }

    // ======================= Channel =============================
}