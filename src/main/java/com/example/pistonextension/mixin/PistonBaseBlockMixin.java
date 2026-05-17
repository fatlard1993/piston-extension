package com.example.pistonextension.mixin;

import com.example.pistonextension.PistonExtension;
import com.example.pistonextension.PistonExtensionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PistonBaseBlock.class)
public abstract class PistonBaseBlockMixin {

	@Shadow
	@Final
	private boolean isSticky;

	@Inject(method = "checkIfExtend", at = @At("HEAD"), cancellable = true)
	private void pistonExtension$onCheckIfExtend(
			Level level, BlockPos pos, BlockState state, CallbackInfo ci) {

		if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
			return;
		}

		PistonExtensionData data = PistonExtensionData.get(serverLevel);
		int extensionLength = data.getExtensionLength(pos);

		if (extensionLength <= 1) {
			return;
		}

		Direction facing = state.getValue(DirectionalBlock.FACING);
		boolean extended = state.getValue(PistonBaseBlock.EXTENDED);
		boolean hasPower = level.hasNeighborSignal(pos)
				|| pistonExtension$hasNeighborSignalExceptFacing(level, pos, facing);

		if (hasPower && !extended) {
			if (PistonExtension.placeExtension(serverLevel, pos, facing,
					extensionLength, this.isSticky)) {
				level.setBlock(pos, state.setValue(PistonBaseBlock.EXTENDED, true),
						Block.UPDATE_ALL);
			}
			ci.cancel();
		} else if (!hasPower && extended) {
			// Remove all extension blocks
			PistonExtension.removeExtension(level, pos, facing);

			// Sticky: try to pull the block beyond the extension back
			if (this.isSticky) {
				BlockPos pullFrom = PistonExtension.offsetPos(pos, facing, extensionLength + 1);
				BlockState pullState = level.getBlockState(pullFrom);
				if (!pullState.isAir()
						&& PistonBaseBlock.isPushable(pullState, level, pullFrom,
								facing.getOpposite(), false, facing)) {
					BlockPos pullTo = PistonExtension.offsetPos(pos, facing, 1);
					if (level.getBlockState(pullTo).isAir()) {
						level.setBlock(pullTo, pullState, Block.UPDATE_ALL);
						level.removeBlock(pullFrom, false);
					}
				}
			}

			level.setBlock(pos, state.setValue(PistonBaseBlock.EXTENDED, false),
					Block.UPDATE_ALL);
			ci.cancel();
		}
	}

	private static boolean pistonExtension$hasNeighborSignalExceptFacing(
			Level level, BlockPos pos, Direction facing) {
		for (Direction dir : Direction.values()) {
			if (dir != facing && level.hasSignal(pos.relative(dir), dir)) {
				return true;
			}
		}
		return false;
	}
}
