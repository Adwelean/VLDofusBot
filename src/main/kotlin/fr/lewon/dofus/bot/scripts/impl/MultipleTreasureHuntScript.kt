package fr.lewon.dofus.bot.scripts.impl

import fr.lewon.dofus.bot.core.logs.LogItem
import fr.lewon.dofus.bot.gui.alert.SoundType
import fr.lewon.dofus.bot.model.hunt.HuntLevel
import fr.lewon.dofus.bot.scripts.DofusBotParameter
import fr.lewon.dofus.bot.scripts.DofusBotParameterType
import fr.lewon.dofus.bot.scripts.DofusBotScript
import fr.lewon.dofus.bot.scripts.DofusBotScriptStat
import fr.lewon.dofus.bot.scripts.tasks.impl.hunt.ExecuteHuntTask
import fr.lewon.dofus.bot.scripts.tasks.impl.hunt.FetchHuntTask
import fr.lewon.dofus.bot.scripts.tasks.impl.transport.ReachMapTask
import fr.lewon.dofus.bot.scripts.tasks.impl.windows.RestartGameTask
import fr.lewon.dofus.bot.util.FormatUtil
import fr.lewon.dofus.bot.util.game.TreasureHuntUtil
import fr.lewon.dofus.bot.util.io.WaitUtil
import fr.lewon.dofus.bot.util.network.GameInfo

class MultipleTreasureHuntScript : DofusBotScript("Multiple treasure hunts") {

    private val huntLevelParameter = DofusBotParameter(
        "hunt_level", "Hunt level", "200", DofusBotParameterType.CHOICE, HuntLevel.values().map { it.label }
    )

    private val huntCountParameter = DofusBotParameter(
        "hunt_count", "Amount of hunts to process before stopping", "50", DofusBotParameterType.INTEGER
    )

    private val cleanCacheParameter = DofusBotParameter(
        "clean_cache_every", "Amount of hunt(s) before cleaning Dofus cache", "12", DofusBotParameterType.INTEGER
    )

    override fun getParameters(): List<DofusBotParameter> {
        return listOf(
            huntLevelParameter,
            huntCountParameter,
            cleanCacheParameter
        )
    }

    private val huntFetchDurations = ArrayList<Long>()
    private val huntDurations = ArrayList<Long>()
    private val successRateStat = DofusBotScriptStat("Success rate")
    private val averageHuntFetchDurationStat = DofusBotScriptStat("Average hunt fetch duration")
    private val averageHuntDurationStat = DofusBotScriptStat("Average hunt duration")
    private val nextRestartInStat = DofusBotScriptStat("Next restart in")

    override fun getStats(): List<DofusBotScriptStat> {
        return listOf(
            successRateStat,
            averageHuntFetchDurationStat,
            averageHuntDurationStat,
            nextRestartInStat
        )
    }

    override fun getDescription(): String {
        val huntCount = huntCountParameter.value.toInt()
        val cleanCacheEvery = cleanCacheParameter.value.toInt()
        var description = "Executes $huntCount hunt(s) starting with the current treasure hunt by : \n"
        description += " - Reaching treasure hunt start location \n"
        description += " - Finding hints and resolving treasure hunt steps \n"
        description += " - Fighting the chest at the end \n"
        description += "Dofus cache will be cleaned every $cleanCacheEvery hunt(s)"
        return description
    }

    override fun execute(logItem: LogItem, gameInfo: GameInfo) {
        clearStats()
        var successCount = 0
        var cleanCacheCount = cleanCacheParameter.value.toInt()
        val huntLevel = HuntLevel.fromLabel(huntLevelParameter.value)
            ?: error("Invalid hunt level")

        for (i in 0 until huntCountParameter.value.toInt()) {
            nextRestartInStat.value = "$cleanCacheCount hunt(s)"
            val fetchStartTimeStamp = System.currentTimeMillis()
            if (gameInfo.treasureHunt == null && !FetchHuntTask(huntLevel).run(logItem, gameInfo)) {
                error("Couldn't fetch a new hunt")
            }
            val fetchDuration = System.currentTimeMillis() - fetchStartTimeStamp
            huntFetchDurations.add(fetchDuration)
            averageHuntFetchDurationStat.value = FormatUtil.durationToStr(huntFetchDurations.average().toLong())

            val huntStartTimeStamp = System.currentTimeMillis()

            if (!ReachMapTask(listOf(TreasureHuntUtil.getLastHintMap(gameInfo))).run(logItem, gameInfo)) {
                error("Couldn't reach hunt start")
            }
            val success = ExecuteHuntTask().run(logItem, gameInfo)
            WaitUtil.sleep(300)

            val huntDuration = System.currentTimeMillis() - huntStartTimeStamp

            if (success) {
                successCount++
                huntDurations.add(huntDuration)
                averageHuntDurationStat.value = FormatUtil.durationToStr(huntDurations.average().toLong())
            }
            cleanCacheCount--
            successRateStat.value = "$successCount / ${i + 1}"
            nextRestartInStat.value = "$cleanCacheCount hunt(s)"
            if (cleanCacheCount == 0) {
                RestartGameTask().run(logItem, gameInfo)
                cleanCacheCount = cleanCacheParameter.value.toInt()
            }
            if (!success) {
                SoundType.FAILED.playSound()
                WaitUtil.sleep(600 * 1000 - huntDuration)
                if (gameInfo.treasureHunt != null) {
                    TreasureHuntUtil.giveUpHunt(gameInfo)
                }
            }
        }
    }

    private fun clearStats() {
        huntDurations.clear()
        successRateStat.resetValue()
        averageHuntDurationStat.resetValue()
        nextRestartInStat.resetValue()
    }
}