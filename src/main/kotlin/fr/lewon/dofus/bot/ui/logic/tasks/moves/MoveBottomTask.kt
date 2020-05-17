package fr.lewon.dofus.bot.ui.logic.tasks.moves

import fr.lewon.dofus.bot.json.DTBPoint
import fr.lewon.dofus.bot.ui.DofusTreasureBotGUIController
import fr.lewon.dofus.bot.ui.LogItem
import fr.lewon.dofus.bot.util.DTBConfigManager
import fr.lewon.dofus.bot.util.Directions

class MoveBottomTask(
    controller: DofusTreasureBotGUIController,
    parentLogItem: LogItem?
) : MoveTask(Directions.BOTTOM, controller, parentLogItem) {

    override fun getMoveDest(): DTBPoint {
        return DTBConfigManager.config.bottomAccessPos
    }

}