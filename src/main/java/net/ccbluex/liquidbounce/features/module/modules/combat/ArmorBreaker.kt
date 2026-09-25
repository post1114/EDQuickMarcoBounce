/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Gapple
import net.ccbluex.liquidbounce.utils.inventory.attackDamage
import net.ccbluex.liquidbounce.utils.inventory.inventorySlot
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword

object ArmorBreaker : Module("ArmorBreaker", Category.COMBAT) {

    val onTick = handler<GameTickEvent> {
        val player = mc.thePlayer ?: return@handler

        if (KillAura.target == null)
            return@handler

        if (Gapple.eating)
            return@handler

        // Only operate on the player inventory window (skip chests, etc.)
        if (player.openContainer !== player.inventoryContainer)
            return@handler

        val heldSlot = 36 + player.inventory.currentItem

        val candidates = findSwords(player, 36..44) ?: findSwords(player, 0..35)
            ?: return@handler

        val currentIdx = candidates.indexOfFirst { it.first == heldSlot }
        val next = if (currentIdx == -1) candidates.first()
            else candidates[(currentIdx + 1) % candidates.size]

        if (next.first == heldSlot)
            return@handler

        // Mode 2: swap the clicked slot with hotbar slot `currentItem`
        mc.playerController.windowClick(0, next.first, player.inventory.currentItem, 2, player)
    }

    private fun findSwords(player: EntityPlayerSP, range: IntRange): List<Pair<Int, ItemStack>>? =
        range.mapNotNull { slot ->
            val stack = player.inventorySlot(slot).stack ?: return@mapNotNull null
            if (stack.item is ItemSword) slot to stack else null
        }.sortedBy { it.second.attackDamage }
            .ifEmpty { null }
}
