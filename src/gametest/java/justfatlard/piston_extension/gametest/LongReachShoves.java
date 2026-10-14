package justfatlard.piston_extension.gametest;

import justfatlard.piston_extension.PistonExtensionData;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

/**
 * A sticky piston set to push three, with glass on its face, the way an item smasher is built:
 * an item in the way with room ahead is shoved along, a pig in the way is shoved along, and an
 * item caught against a wall is crushed by xp-crush, which never sees a long reach move on its own.
 */
public final class LongReachShoves implements FabricClientGameTest {

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			TestServerContext server = world.getServer();
			world.getConnection().waitForChunksRender();
			BlockPos origin = server.computeOnServer(s -> world.getConnection().getServerPlayer().blockPosition());
			int x = origin.getX() + 10;
			int y = 120;
			int z = origin.getZ();

			// Room ahead: an item where the head lands is shoved on past the glass.
			build(server, x, y, z, false);
			server.runCommand("summon minecraft:item %d.5 %d %d.5 {Item:{id:\"minecraft:diamond_sword\",count:1}}".formatted(x + 2, y, z));
			server.runCommand("summon minecraft:pig %d.5 %d %d.5 {NoAI:1b}".formatted(x + 3, y, z + 0));
			context.waitTicks(10);
			power(context, server, x, y, z);
			server.runOnServer(s -> {
				var level = s.overworld();
				for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(new BlockPos(x, y, z)).inflate(8))) {
					check(item.getX() > x + 5, "the item was left at " + item.getX() + ", inside the reach");
				}
				for (Pig pig : level.getEntitiesOfClass(Pig.class, new AABB(new BlockPos(x, y, z)).inflate(8))) {
					check(pig.getBoundingBox().minX >= x + 5 - 0.01, "the pig was left at " + pig.getX() + ", inside the reach");
				}
			});

			// A wall ahead: the item where the glass lands is crushed.
			int z2 = z + 6;
			build(server, x, y, z2, true);
			server.runCommand("summon minecraft:item %d.5 %d %d.5 {Item:{id:\"minecraft:diamond_sword\",count:1}}".formatted(x + 4, y, z2));
			context.waitTicks(10);
			power(context, server, x, y, z2);
			server.runOnServer(s -> {
				var level = s.overworld();
				AABB around = new AABB(new BlockPos(x + 4, y, z2)).inflate(2);
				check(level.getEntitiesOfClass(ItemEntity.class, around).isEmpty(), "the item pinned against the wall was not crushed");
				check(!level.getEntitiesOfClass(ExperienceOrb.class, around.inflate(3)).isEmpty(), "the crushed item paid nothing");
				check(level.getBlockState(new BlockPos(x + 4, y, z2)).is(net.minecraft.world.level.block.Blocks.GLASS), "the glass did not reach the wall");
			});
		}
	}

	/** A sticky piston facing east set to push three, glass on its face, and obsidian five out if asked. */
	private static void build(TestServerContext server, int x, int y, int z, boolean wall) {
		server.runCommand("fill %d %d %d %d %d %d minecraft:smooth_stone".formatted(x - 2, y - 1, z - 2, x + 10, y - 1, z + 2));
		server.runCommand("fill %d %d %d %d %d %d minecraft:air".formatted(x - 2, y, z - 2, x + 10, y + 2, z + 2));
		server.runCommand("setblock %d %d %d minecraft:sticky_piston[facing=east]".formatted(x, y, z));
		server.runCommand("setblock %d %d %d minecraft:glass".formatted(x + 1, y, z));
		if (wall) server.runCommand("setblock %d %d %d minecraft:obsidian".formatted(x + 5, y, z));
		server.runOnServer(s -> PistonExtensionData.get(s.overworld()).setExtensionLength(new BlockPos(x, y, z), 3));
	}

	private static void power(ClientGameTestContext context, TestServerContext server, int x, int y, int z) {
		server.runCommand("setblock %d %d %d minecraft:redstone_block".formatted(x - 1, y, z));
		context.waitTicks(10);
	}

	private static void check(boolean ok, String complaint) {
		if (!ok) throw new AssertionError(complaint);
	}
}
