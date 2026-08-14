package justfatlard.piston_extension.mixin;

import justfatlard.piston_extension.PistonExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHeadBlock.class)
public class PistonHeadBlockMixin {

	/**
	 * Allow piston head to survive when a shaft block with matching facing
	 * is behind it (not just a piston base).
	 */
	@Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
	private void pistonExtension$onCanSurvive(BlockState state, LevelReader level,
			BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		Direction facing = state.getValue(DirectionalBlock.FACING);
		BlockPos behindPos = pos.relative(facing.getOpposite());
		BlockState behindState = level.getBlockState(behindPos);
		if (behindState.getBlock() == PistonExtension.PISTON_SHAFT
				&& behindState.getValue(DirectionalBlock.FACING) == facing) {
			cir.setReturnValue(true);
		}
	}

	/**
	 * Vanilla ignores the moved parameter entirely: it always destroys the
	 * piston base when the head is removed. Respect the flag so our mod can
	 * safely remove heads without destroying the base.
	 */
	@Inject(method = "affectNeighborsAfterRemoval", at = @At("HEAD"), cancellable = true)
	private void pistonExtension$onAffectNeighbors(BlockState state, ServerLevel level,
			BlockPos pos, boolean moved, CallbackInfo ci) {
		if (moved) {
			ci.cancel();
		}
	}
}
