package justfatlard.piston_extension.quest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import justfatlard.piston_extension.PistonExtensionData;
import justfatlard.village_quests.quest.VillagerQuest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A piston that has to reach further than one block.
 *
 * <p>Right-clicking a piston cycles how far it pushes. There is no item, no
 * recipe and no particle to find: the block looks identical at every setting, so
 * the only way anyone learns this is being told, and nothing in the game tells
 * anyone anything about a block they have already used a hundred times.
 *
 * <p>The ask is a reach of three, because one is what a piston already does and
 * two could be an accident. Three is unmistakably somebody who knows.
 */
public class LongPistonQuest extends VillagerQuest {
	/** Cheap enough to run when a dialogue opens, wide enough to cover a work site. */
	private static final int SEARCH = 6;

	private static final int REQUIRED_REACH = 3;

	private final BlockPos near;

	public LongPistonQuest(String requesterName, UUID villagerUuid, BlockPos near) {
		super(VillagerQuest.QuestType.CREATION, requesterName, villagerUuid, 7);
		this.near = near.immutable();
	}

	@Override
	public String getDescription() {
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		String[] lines = {
			this.requesterName + ": \"I want a gate that opens properly. A piston only shoves things one block "
				+ "and that is not a gate, that is a nudge. Unless - and I am told this works - you tap the piston and it changes its mind.\"",
			this.requesterName + ": \"Somebody told me a piston can be made to reach further. Right-click it, apparently, "
				+ "and it cycles. I want one near here that pushes three, and I want to see it.\"",
			this.requesterName + ": \"A piston that pushes one block is a door knocker. Set one to three. "
				+ "You tap it to change it - do not ask me why nobody says so.\""
		};
		return lines[rng.nextInt(lines.length)];
	}

	@Override
	public String getObjective() {
		return "set up a piston near " + this.requesterName + " that reaches " + REQUIRED_REACH
			+ " blocks - right-click a piston to cycle how far it pushes";
	}

	@Override
	public boolean checkCompletion(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel world)) return false;

		PistonExtensionData data = PistonExtensionData.get(world);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -SEARCH; dx <= SEARCH; dx++) {
			for (int dy = -SEARCH; dy <= SEARCH; dy++) {
				for (int dz = -SEARCH; dz <= SEARCH; dz++) {
					cursor.set(this.near.getX() + dx, this.near.getY() + dy, this.near.getZ() + dz);
					if (!world.isLoaded(cursor)) continue;

					BlockState state = world.getBlockState(cursor);
					if (!state.is(Blocks.PISTON) && !state.is(Blocks.STICKY_PISTON)) continue;

					if (data.getExtensionLength(cursor) >= REQUIRED_REACH) return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onComplete(ServerPlayer player) {
		// The gate stays. It is the village's now, and it works.
	}
}
