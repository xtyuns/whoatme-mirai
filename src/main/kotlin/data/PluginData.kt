package io.github.xtyuns.data

import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

/**
 * 插件数据
 */
object PluginData: AutoSavePluginData("data") {
    val atMap: MutableMap<String, Array<Map<String, Any>>> by value()
}
