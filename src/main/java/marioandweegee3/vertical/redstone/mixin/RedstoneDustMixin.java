package marioandweegee3.vertical.redstone.mixin;

import marioandweegee3.vertical.redstone.block.TransmitterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneDustMixin {
    @Inject(at = @At("TAIL"), method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", cancellable = true)
    private static void canConnect(BlockState state, @Nullable Direction d, CallbackInfoReturnable<Boolean> ci) {
        Block block = state.getBlock();

        if(block instanceof TransmitterBlock) {
            ci.setReturnValue(false);
        }
    }
}
