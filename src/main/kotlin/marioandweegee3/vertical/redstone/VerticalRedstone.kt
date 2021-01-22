package marioandweegee3.vertical.redstone

import marioandweegee3.vertical.redstone.block.TransmitterBlock
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class VerticalRedstone: ModInitializer {
    companion object {
        const val MOD_ID: String = "verticalredstone"

        val logger: Logger = LogManager.getLogger(MOD_ID)
    }

    override fun onInitialize() {
        logger.info("Initializing...")

        val blockSettings = FabricBlockSettings.copy(Blocks.OBSERVER)
        val itemSettings = Item.Settings().group(ItemGroup.REDSTONE)

        val blocks = mapOf(
            Identifier(MOD_ID, "upward_transmitter") to TransmitterBlock(blockSettings, Direction.UP),
            Identifier(MOD_ID, "downward_transmitter") to TransmitterBlock(blockSettings, Direction.DOWN),
        )


        for ((id, block) in blocks) {
            register(BLOCK, id, block)
            register(ITEM, id, BlockItem(block, itemSettings))

            logger.info("Registered block $id")
        }

        logger.info("Done!")
    }


}