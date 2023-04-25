package io.github.xtyuns.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * 插件配置
 */
object PluginConfig: AutoSavePluginConfig("config") {
    val bots: Array<Long> by value()
    val contextLength by value(10)
}
