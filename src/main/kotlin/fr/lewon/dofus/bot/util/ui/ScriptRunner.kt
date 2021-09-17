package fr.lewon.dofus.bot.util.ui

import fr.lewon.dofus.bot.gui.LogItem
import fr.lewon.dofus.bot.gui.MainFrame
import fr.lewon.dofus.bot.scripts.DofusBotScript
import fr.lewon.dofus.bot.util.WindowsUtil
import java.awt.Color

object ScriptRunner {

    private var isThreadRunning = false
    private lateinit var runnerThread: Thread
    private lateinit var currentLogItem: LogItem

    @Synchronized
    fun runScript(dofusScript: DofusBotScript) {
        if (isThreadRunning) {
            error("Cannot run script, there is already one running")
        }
        WindowsUtil.bringGameToFront()
        WindowsUtil.updateGameBounds()
        runnerThread = Thread {
            currentLogItem = DTBLogger.log("Executing Dofus script : [${dofusScript.name}]")
            try {
                dofusScript.execute(currentLogItem)
                onScriptOk()
            } catch (e: Exception) {
                onScriptKo(e)
            }
        }
        runnerThread.start()
        MainFrame.loading()
        isThreadRunning = true
    }

    @Synchronized
    fun stopScript() {
        if (isThreadRunning) {
            runnerThread.stop()
            onScriptCanceled()
        }
    }

    private fun onScriptKo(e: Exception) {
        DTBLogger.closeLog("Execution KO - ${e.localizedMessage}", currentLogItem)
        e.printStackTrace()
        onScriptEnd(Color.RED)
    }

    private fun onScriptCanceled() {
        DTBLogger.closeLog("Execution canceled", currentLogItem)
        onScriptEnd(Color.ORANGE)
    }

    private fun onScriptOk() {
        DTBLogger.closeLog("Execution OK", currentLogItem)
        onScriptEnd(Color.GREEN)
    }

    private fun onScriptEnd(progressBarColor: Color) {
        MainFrame.stopLoading(progressBarColor)
        isThreadRunning = false
    }
}