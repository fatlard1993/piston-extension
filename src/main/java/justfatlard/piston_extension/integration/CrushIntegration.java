package justfatlard.piston_extension.integration;

import justfatlard.xp_crush.Crush;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * An item a long reach pinned, handed to xp-crush to crush. Its own watch on pistons never sees a
 * long reach, because the blocks are set down in one step rather than moved.
 */
public final class CrushIntegration {
	private CrushIntegration() {}

	public static void pinned(ServerLevel level, ItemEntity item, int blocks) {
		Crush.pinned(level, item, blocks);
	}
}
