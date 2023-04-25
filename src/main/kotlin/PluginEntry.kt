package io.github.xtyuns

import io.github.xtyuns.config.PluginConfig
import io.github.xtyuns.data.PluginData
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupAwareMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.message.data.buildForwardMessage
import net.mamoe.mirai.message.data.sendTo
import java.util.*

object PluginEntry : KotlinPlugin(JvmPluginDescription.loadFromResource()) {
    private val eventQueueMap = HashMap<Long, LinkedList<GroupAwareMessageEvent>>()

    override fun onEnable() {
        PluginConfig.reload()
        PluginData.reload()

        globalEventChannel().subscribeAlways<GroupAwareMessageEvent> { event ->
            if (!PluginConfig.bots.contains(event.bot.id)) {
                return@subscribeAlways
            }

            updateMessageData(event)
            acceptGroupMessage(event)
        }
    }

    private suspend fun acceptGroupMessage(event: GroupAwareMessageEvent) {
        if (!event.message.contentToString().lowercase().matches(Regex("谁(at|@|艾特)我"))) {
            return
        }

        val messageEvents = PluginData.atMap.getOrDefault("${event.sender.id}_${event.group.id}", emptyArray())
        if (messageEvents.isEmpty()) {
            event.group.sendMessage("empty")
        } else {
            buildForwardMessage(event.group) {
                messageEvents.forEach {
                    add(
                        it["senderId"].toString().toLong(),
                        it["senderName"].toString(),
                        MessageChain.deserializeFromJsonString(it["msg"].toString()),
                        it["time"].toString().toInt()
                    )
                }
            }.sendTo(event.group)
        }
    }

    private fun updateMessageData(event: GroupAwareMessageEvent) {
        val eventQueue = eventQueueMap.getOrElse(event.group.id) { LinkedList<GroupAwareMessageEvent>() }
        if (eventQueue.isEmpty()) {
            eventQueueMap[event.group.id] = eventQueue
        }

        if (eventQueue.size == PluginConfig.contextLength) {
            eventQueue.removeFirst()
        }
        eventQueue.add(event)

        val iterator = eventQueue.iterator()
        val half = PluginConfig.contextLength shr 1
        if (eventQueue.size > half) {
            for (i in 1..half) {
                if (iterator.hasNext()) {
                    iterator.next()
                }
            }
        }

        val msgData: Array<Map<String, Any>> = eventQueue.toTypedArray().map {
            mapOf(
                "senderId" to it.sender.id,
                "senderName" to it.senderName,
                "msg" to it.message.serializeToJsonString(),
                "time" to it.time
            )
        }.toTypedArray()
        while (iterator.hasNext()) {
            val ats = iterator.next().message.filterIsInstance<At>()
            for (at in ats) {
                PluginData.atMap["${at.target}_${event.group.id}"] = msgData
            }
        }
    }
}
