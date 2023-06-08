package io.github.xtyuns.config

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * 插件配置
 */
object PluginConfig: AutoSavePluginConfig("config") {
    /**
     * 监听账号
     */
    val bots: Array<Long> by value()

    /**
     * 上下文消息条数
     */
    val contextLength by value(10)

    /**
     * 查询限制间隔
     */
    val interval by value(60000)
}
