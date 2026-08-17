package justfatlard.piston_extension.integration;

import justfatlard.block_tip.api.BlockTipApi;
import justfatlard.piston_extension.PistonExtensionData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * How far this particular piston has been set to push.
 *
 * <p>The reach is the one thing about a piston that cannot be seen: the block is
 * identical at every setting, and the only way to find out has been to power it
 * and watch. Which is fine once, and miserable in a room full of them.
 */
public final class PistonTipRegistration {
	private PistonTipRegistration() {}

	public static void register() {
		BlockTipApi.describe((level, pos, state, player) -> {
			if (!state.is(Blocks.PISTON) && !state.is(Blocks.STICKY_PISTON)) return null;
			if (!(level instanceof ServerLevel serverLevel)) return null;

			int reach = PistonExtensionData.get(serverLevel).getExtensionLength(pos);
			// One is what every piston does, so saying it is noise.
			return reach <= 1 ? null : "Pushes " + reach + " blocks";
		});
	}
}
