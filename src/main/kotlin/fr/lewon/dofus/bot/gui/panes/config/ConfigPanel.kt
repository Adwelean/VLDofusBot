package fr.lewon.dofus.bot.gui.panes.config

import fr.lewon.dofus.bot.gui.panes.character.CharacterSelectionPanel
import fr.lewon.dofus.bot.sniffer.DofusMessageReceiverUtil
import fr.lewon.dofus.bot.util.filemanagers.ConfigManager
import fr.lewon.dofus.bot.util.geometry.PointAbsolute
import fr.lewon.dofus.bot.util.io.ConverterUtil
import fr.lewon.dofus.bot.util.jna.JNAUtil
import fr.lewon.dofus.bot.util.network.GameSnifferUtil
import net.miginfocom.swing.MigLayout
import java.awt.event.ItemEvent
import javax.swing.*

object ConfigPanel : JPanel(MigLayout()) {

    private val locateCursorLabel = JLabel("Locate cursor")
    private val locateCursorButton = JButton("Locate")
    private val archMonsterSoundLabel = JLabel("Arch monster sound")
    private val archMonsterSoundCheckBox = JCheckBox()
    private val networkInterfaceSelectionLabel = JLabel("Network interface")
    private val networkInterfaceSelectionComboBox =
        JComboBox(DofusMessageReceiverUtil.getNetworkInterfaceNames().toTypedArray())

    init {
        // Cursor locator
        locateCursorButton.addActionListener { locatePoint() }
        addLine(locateCursorLabel, locateCursorButton)

        // Arch monster sound
        archMonsterSoundCheckBox.isSelected = ConfigManager.config.playArchMonsterSound
        addLine(archMonsterSoundLabel, archMonsterSoundCheckBox)
        archMonsterSoundCheckBox.addItemListener {
            updateArchMonsterSound(archMonsterSoundCheckBox.isSelected)
        }

        // Network Interface GUI
        val savedNetworkInterfaceName = ConfigManager.config.networkInterfaceName
            ?: networkInterfaceSelectionComboBox.getItemAt(0).also { updateNetworkInterface(it) }
        networkInterfaceSelectionComboBox.selectedItem = savedNetworkInterfaceName
        addLine(networkInterfaceSelectionLabel, networkInterfaceSelectionComboBox)
        networkInterfaceSelectionComboBox.addItemListener { evt ->
            if (evt.stateChange == ItemEvent.SELECTED) {
                val networkInterfaceName = evt.item as String
                updateNetworkInterface(networkInterfaceName)
            }
        }
    }

    private fun locatePoint() {
        val locatorFrame = CursorLocatorFrame("Click on position to register") {
            println("-----")
            val currentCharacter = CharacterSelectionPanel.cardList.selectedItem
            currentCharacter?.let { c ->
                GameSnifferUtil.getConnection(c)?.let { connection ->
                    val gameInfo = GameSnifferUtil.getGameInfoByConnection(connection)
                    JNAUtil.updateGameBounds(gameInfo)
                    val windowPos = JNAUtil.getGamePosition(connection.pid)
                    val pointAbsolute = PointAbsolute(it.x - windowPos.x, it.y - windowPos.y)
                    println("PointAbsolute(${pointAbsolute.x}, ${pointAbsolute.y})")
                    val pointRelative = ConverterUtil.toPointRelative(gameInfo, pointAbsolute)
                    println("PointRelative(${pointRelative.x}f, ${pointRelative.y}f)")
                    val uiPoint = ConverterUtil.toUIPoint(gameInfo, pointAbsolute)
                    println("UIPoint(${uiPoint.x}, ${uiPoint.y})")
                }
            } ?: println("Couldn't find dofus connection information.")
        }
        locatorFrame.isVisible = true
    }

    private fun updateArchMonsterSound(playArchMonsterSound: Boolean) {
        ConfigManager.editConfig { it.playArchMonsterSound = playArchMonsterSound }
    }

    private fun updateNetworkInterface(networkInterfaceName: String) {
        ConfigManager.editConfig { it.networkInterfaceName = networkInterfaceName }
        GameSnifferUtil.changeNetworkInterface(networkInterfaceName)
    }

    private fun addLine(leftComponent: JComponent, rightComponent: JComponent, separator: Boolean = true) {
        add(leftComponent)
        add(rightComponent, "width 80:80:80, al right, wrap")
        if (separator) addSeparator()
    }

    private fun addSeparator() {
        add(JSeparator(JSeparator.HORIZONTAL), "span 2 1, width max, wrap")
    }

}