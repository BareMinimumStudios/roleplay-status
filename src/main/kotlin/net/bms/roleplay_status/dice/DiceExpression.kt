package net.bms.roleplay_status.dice

import java.util.random.RandomGenerator

data class DiceExpression(val count: Int, val sides: Int, val modifier: Int) {
    val notation: String get() = "${count}d$sides" + when { modifier > 0 -> "+$modifier"; modifier < 0 -> "$modifier"; else -> "" }
    fun roll(random: RandomGenerator): List<Int> = List(count) { random.nextInt(sides) + 1 }
    fun total(results: List<Int>): Int = results.sum() + modifier
    companion object {
        private val pattern = Regex("([0-9]{0,2})[dD]([0-9]{1,3})([+-][0-9]{1,4})?")
        @JvmStatic fun parse(input: String): DiceExpression? {
            val match = pattern.matchEntire(input) ?: return null
            val count = match.groupValues[1].ifEmpty { "1" }.toInt()
            val sides = match.groupValues[2].toInt()
            val modifier = match.groupValues[3].ifEmpty { "0" }.toInt()
            if (count !in 1..20 || sides !in setOf(2,4,6,8,10,12,20,100) || modifier !in -1000..1000) return null
            return DiceExpression(count, sides, modifier)
        }
        @JvmStatic fun reason(input: String): String = input.filter { !it.isISOControl() && Character.getType(it) != Character.FORMAT.toInt() && it != '\u00a7' }.trim().take(128).let { if (it.lastOrNull()?.isHighSurrogate() == true) it.dropLast(1) else it }
        @JvmStatic fun withinRange(sameDimension: Boolean, distanceSquared: Double, range: Int): Boolean = sameDimension && distanceSquared <= range.toDouble() * range
    }
}
