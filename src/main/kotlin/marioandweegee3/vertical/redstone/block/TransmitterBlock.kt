package marioandweegee3.vertical.redstone.block

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.RedstoneWireBlock
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import net.minecraft.world.TickPriority
import net.minecraft.world.World
import java.util.*
import kotlin.math.min

class TransmitterBlock(settings: Settings, private val powerDirection: Direction): Block(settings) {
    init {
        defaultState = stateManager
            .defaultState
            .with(powered, false)

        require(powerDirection in setOf(Direction.UP, Direction.DOWN))
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(powered)
    }

    override fun emitsRedstonePower(state: BlockState?): Boolean = true

    private fun getPower(world: World, pos: BlockPos): Int {
        val searchDir = powerDirection.opposite
        val searchPos = pos.offset(searchDir)
        var power = world.getEmittedRedstonePower(searchPos, searchDir)
        val searchState = world.getBlockState(searchPos)
        if (searchState.block is RedstoneWireBlock)
            power += searchState[RedstoneWireBlock.POWER]

        return min(power, 15)
    }

    private fun hasPower(world: World, pos: BlockPos): Boolean =
        getPower(world, pos) > 0

    override fun scheduledTick(
        state: BlockState,
        world: ServerWorld,
        pos: BlockPos,
        random: Random
    ) {
        val isPowered = state[powered]
        val hasPower = hasPower(world, pos)
        if (isPowered && !hasPower) {
            world.setBlockState(pos, state.with(powered, false), 2)
        } else if (!isPowered) {
            when {
                !hasPower -> world.blockTickScheduler.schedule(pos, this, DELAY, TickPriority.VERY_HIGH)
                else -> world.setBlockState(pos, state.with(powered, true), 2)
            }
        }
    }

    override fun getComparatorOutput(state: BlockState, world: World, pos: BlockPos): Int {
        return if(state[powered]) 15 else 0
    }

    override fun hasComparatorOutput(state: BlockState): Boolean {
        return true
    }

    override fun getWeakRedstonePower(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        direction: Direction
    ): Int = if (direction == powerDirection.opposite && state[powered]) 15 else 0

    override fun getStrongRedstonePower(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        direction: Direction
    ): Int = getWeakRedstonePower(state, world, pos, direction)


    override fun onPlaced(
        world: World,
        pos: BlockPos,
        state: BlockState,
        placer: LivingEntity?,
        itemStack: ItemStack
    ) {
        if (hasPower(world, pos))
            world.blockTickScheduler
                .schedule(pos, this, 1)
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!moved && !state.isOf(newState.block)) {
            super.onStateReplaced(state, world, pos, newState, moved)
            updateTarget(world, pos)
            world.updateComparators(pos, this)
        }
    }

    override fun onBlockAdded(
        state: BlockState,
        world: World,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean
    ) {
        updateTarget(world, pos)
    }

    private fun updateTarget(world: World, pos: BlockPos) {
        val direction = powerDirection
        val blockPos = pos.offset(direction)
        world.updateNeighbor(blockPos, this, pos)
        world.updateNeighborsExcept(blockPos, this, direction.opposite)
        world.updateComparators(pos, this)
    }

    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        block: Block,
        fromPos: BlockPos,
        notify: Boolean
    ) {
        val isPowered = state[powered]
        val hasPower = hasPower(world, pos)

        if(isPowered != hasPower && !world.blockTickScheduler.isTicking(pos, this)) {
            val tickPriority = TickPriority.VERY_HIGH

            world.blockTickScheduler.schedule(pos, this, DELAY, tickPriority)
        }
    }

    companion object {
        val powered: BooleanProperty = BooleanProperty.of("powered")
        const val DELAY: Int = 2
    }
}