package fr.lewon.dofus.bot.util.game

import fr.lewon.dofus.bot.game.fight.FighterCharacteristic
import fr.lewon.dofus.bot.sniffer.model.types.fight.charac.CharacterCharacteristic
import fr.lewon.dofus.bot.sniffer.model.types.fight.charac.impl.CharacterCharacteristicDetailed
import fr.lewon.dofus.bot.sniffer.model.types.fight.charac.impl.CharacterCharacteristicValue
import fr.lewon.dofus.bot.util.filemanagers.DTBCharacteristicManager

object CharacteristicUtil {

    fun getCharacteristicValue(charac: CharacterCharacteristic): Int {
        return when (charac) {
            is CharacterCharacteristicDetailed -> charac.base + charac.additional + charac.contextModif + charac.objectsAndMountBonus + charac.alignGiftBonus
            is CharacterCharacteristicValue -> charac.total
            else -> error("Untreated characteristic type : ${charac::class.java.typeName}")
        }
    }

    fun getCharacteristicValue(
        fighterCharacteristic: FighterCharacteristic,
        characsById: Map<Int, CharacterCharacteristic>
    ): Int? {
        val keyword = fighterCharacteristic.keyword
        val characId = DTBCharacteristicManager.getCharacteristicId(keyword)
        val charac = characsById[characId] ?: return null
        return getCharacteristicValue(charac)
    }

    fun getCharacteristicValue(
        fighterCharacteristic: FighterCharacteristic,
        characs: List<CharacterCharacteristic>
    ): Int? {
        val characsById = characs.map { it.characteristicId to it }.toMap()
        return getCharacteristicValue(fighterCharacteristic, characsById)
    }

}