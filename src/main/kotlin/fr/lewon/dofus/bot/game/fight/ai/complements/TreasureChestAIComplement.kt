package fr.lewon.dofus.bot.game.fight.ai.complements

import fr.lewon.dofus.bot.core.model.spell.DofusSpellLevel
import fr.lewon.dofus.bot.game.fight.Fighter
import fr.lewon.dofus.bot.model.characters.spells.SpellCombination

class TreasureChestAIComplement : AIComplement() {

    override fun canAttack(playerFighter: Fighter): Boolean {
        return playerFighter.maxHp / 6 < playerFighter.getCurrentHp()
    }

    override fun canMove(playerFighter: Fighter): Boolean {
        return true
    }

    override fun mustUseAllMP(playerFighter: Fighter): Boolean {
        return false
    }

    override fun getIdealDistance(playerFighter: Fighter, spells: List<DofusSpellLevel>, playerRange: Int): Int {
        return 0
    }

    override fun getIdealDistanceOLD(playerFighter: Fighter, spells: List<SpellCombination>, playerRange: Int): Int {
        return 0
    }

    override fun shouldAvoidUsingMp(): Boolean {
        return false
    }

}