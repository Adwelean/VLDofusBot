package fr.lewon.dofus.bot.scripts.tasks.impl.fight

import fr.lewon.dofus.bot.core.logs.LogItem
import fr.lewon.dofus.bot.model.dungeon.Dungeon
import fr.lewon.dofus.bot.scripts.tasks.BooleanDofusBotTask
import fr.lewon.dofus.bot.scripts.tasks.impl.npc.NpcSpeakTask
import fr.lewon.dofus.bot.scripts.tasks.impl.transport.ReachMapTask
import fr.lewon.dofus.bot.util.game.MoveUtil
import fr.lewon.dofus.bot.util.io.WaitUtil
import fr.lewon.dofus.bot.util.network.GameInfo

class FightDungeonTask(private val dungeon: Dungeon, private val shouldExit: Boolean = true) : BooleanDofusBotTask() {

    override fun doExecute(logItem: LogItem, gameInfo: GameInfo): Boolean {
        if (!ReachMapTask(listOf(dungeon.map)).run(logItem, gameInfo)) {
            return false
        }
        WaitUtil.sleep(2000)
        gameInfo.eventStore.clear()
        NpcSpeakTask(dungeon.npcEnterId, dungeon.npcEnterOptions).run(logItem, gameInfo)
        MoveUtil.waitForMapChange(gameInfo)
        while (!gameInfo.entityIdByNpcId.keys.contains(dungeon.npcExitId)) {
            if (!FightMonsterGroupTask().run(logItem, gameInfo)) {
                return false
            }
        }
        if (shouldExit) {
            WaitUtil.sleep(2000)
            gameInfo.eventStore.clear()
            NpcSpeakTask(dungeon.npcExitId, dungeon.npcExitOptions).run(logItem, gameInfo)
            MoveUtil.waitForMapChange(gameInfo)
        }
        return true
    }

    override fun onStarted(): String {
        return "Fighting dungeon ..."
    }

}