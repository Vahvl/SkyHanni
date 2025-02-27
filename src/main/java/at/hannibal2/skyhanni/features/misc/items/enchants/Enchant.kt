package at.hannibal2.skyhanni.features.misc.items.enchants

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.features.chroma.ChromaManager
import at.hannibal2.skyhanni.utils.LorenzColor
import com.google.gson.annotations.Expose
import java.util.TreeSet

open class Enchant : Comparable<Enchant> {
    @Expose
    var nbtName = ""

    @Expose
    var loreName = ""

    @Expose
    private var goodLevel = 0

    @Expose
    private var maxLevel = 0

    private fun isNormal() = this is Normal
    private fun isUltimate() = this is Ultimate
    private fun isStacking() = this is Stacking

    open fun getFormattedName(level: Int) = getFormat(level) + loreName

    open fun getFormat(level: Int): String {
        val config = SkyHanniMod.feature.inventory.enchantParsing

        // TODO change color to string (support for bold)
        val color = when {
            level >= maxLevel -> config.perfectEnchantColor
            level > goodLevel -> config.greatEnchantColor
            level == goodLevel -> config.goodEnchantColor
            else -> config.poorEnchantColor
        }

        // TODO when chroma is disabled maybe use the neu chroma style instead of gold
        if (color.get() == LorenzColor.CHROMA && !(ChromaManager.config.enabled.get() || EnchantParser.isSbaLoaded)) return "§6§l"
        return color.get().getChatColor()
    }

    override fun toString() = "$nbtName $goodLevel $maxLevel\n"

    override fun compareTo(other: Enchant): Int {
        if (this.isUltimate() == other.isUltimate()) {
            if (this.isStacking() == other.isStacking()) {
                return this.loreName.compareTo(other.loreName)
            }
            return if (this.isStacking()) -1 else 1
        }
        return if (this.isUltimate()) -1 else 1
    }

    class Normal : Enchant() {
    }

    class Ultimate : Enchant() {
        override fun getFormat(level: Int) = "§d§l"
    }

    class Stacking : Enchant() {
        @Expose
        private var nbtNum: String? = null

        @Expose
        private var statLabel: String? = null

        @Expose
        private var stackLevel: TreeSet<Int>? = null

        override fun toString() = "$nbtNum ${stackLevel.toString()} ${super.toString()}"
    }

    class Dummy(name: String) : Enchant() {
        init {
            loreName = name
            nbtName = name
        }

        // Ensures enchants not yet in repo stay as vanilla formatting
        // (instead of that stupid dark red lowercase formatting *cough* sba *cough*)
        override fun getFormattedName(level: Int) = "§9$loreName"
    }
}
