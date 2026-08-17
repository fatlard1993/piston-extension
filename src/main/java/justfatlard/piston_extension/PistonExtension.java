package justfatlard.piston_extension;

import java.util.ArrayList;

import justfatlard.pandorical.api.BlockRegistration;
import justfatlard.pandorical.api.PandoricalApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class PistonExtension implements ModInitializer {
	public static final String MOD_ID = "piston-extension";
	public static Block PISTON_SHAFT;

	@Override
	public void onInitialize() {
      // Guarded class load: the tip registration names block-tip types.
      if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("block-tip")) {
         justfatlard.piston_extension.integration.PistonTipRegistration.register();
      }

		// Guarded class load: PistonQuestRegistration names village-quests types.
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("village-quests-justfatlard")) {
			justfatlard.piston_extension.integration.PistonQuestRegistration.register();
		}

		if (PandoricalApi.isAvailable()) {
			PandoricalApi.content().registerBlock(MOD_ID + ":piston_shaft", new BlockRegistration()
					.baseBlock("minecraft:piston_head")
					.property("facing")
					.model(MOD_ID + ":block/piston_shaft"));
			PandoricalApi.content().registerModAssets(MOD_ID);
		}

		ResourceKey<Block> shaftKey = ResourceKey.create(Registries.BLOCK,
				Identifier.fromNamespaceAndPath(MOD_ID, "piston_shaft"));
		PISTON_SHAFT = Registry.register(BuiltInRegistries.BLOCK, shaftKey,
				new PistonShaftBlock(Block.Properties.of()
						.setId(shaftKey)
						.strength(1.5f)
						.noOcclusion()
						.pushReaction(PushReaction.IMMOVEABLE)));
		UseBlockCallback.EVENT.register(this::onUseBlock);
	}

	public static void removeExtension(Level level, BlockPos pistonPos, Direction facing) {
		for (int i = 1; i <= 3; i++) {
			BlockPos extPos = offsetPos(pistonPos, facing, i);
			Block block = level.getBlockState(extPos).getBlock();
			if (block instanceof PistonHeadBlock || block == PISTON_SHAFT) {
				level.removeBlock(extPos, true);
			} else {
				break;
			}
		}
	}

	public static boolean placeExtension(ServerLevel level, BlockPos pistonPos,
			Direction facing, int length, boolean sticky) {

		ArrayList<BlockState> displaced = new ArrayList<>();
		for (int i = 1; i <= length; i++) {
			BlockPos p = offsetPos(pistonPos, facing, i);
			BlockState s = level.getBlockState(p);
			if (!s.isAir() && !s.canBeReplaced()) {
				if (!PistonBaseBlock.isPushable(s, level, p, facing, true, facing)) {
					return false;
				}
				displaced.add(s);
				level.removeBlock(p, true);
			}
		}

		if (!displaced.isEmpty()) {
			int beyondChain = 0;
			for (int i = length + 1; i <= length + 12; i++) {
				BlockPos p = offsetPos(pistonPos, facing, i);
				BlockState s = level.getBlockState(p);
				if (s.isAir() || s.canBeReplaced()) break;
				if (!PistonBaseBlock.isPushable(s, level, p, facing, true, facing)) {
					return false;
				}
				beyondChain++;
			}

			if (displaced.size() + beyondChain > 12) return false;

			for (int i = beyondChain - 1; i >= 0; i--) {
				BlockPos from = offsetPos(pistonPos, facing, length + 1 + i);
				BlockPos to = offsetPos(pistonPos, facing, length + 1 + i + displaced.size());
				level.setBlock(to, level.getBlockState(from), Block.UPDATE_ALL);
				level.removeBlock(from, false);
			}

			for (int i = 0; i < displaced.size(); i++) {
				BlockPos target = offsetPos(pistonPos, facing, length + 1 + i);
				level.setBlock(target, displaced.get(i), Block.UPDATE_ALL);
			}
		}

		PistonType type = sticky ? PistonType.STICKY : PistonType.DEFAULT;
		for (int i = 1; i <= length; i++) {
			BlockPos extPos = offsetPos(pistonPos, facing, i);
			if (i < length) {
				level.setBlock(extPos, PISTON_SHAFT.defaultBlockState()
						.setValue(DirectionalBlock.FACING, facing), Block.UPDATE_ALL);
			} else {
				level.setBlock(extPos, Blocks.PISTON_HEAD.defaultBlockState()
						.setValue(PistonHeadBlock.FACING, facing)
						.setValue(PistonHeadBlock.SHORT, false)
						.setValue(PistonHeadBlock.TYPE, type), Block.UPDATE_ALL);
			}
		}

		return true;
	}

	public static BlockPos offsetPos(BlockPos pos, Direction dir, int distance) {
		return pos.offset(
				dir.getStepX() * distance,
				dir.getStepY() * distance,
				dir.getStepZ() * distance);
	}

	private InteractionResult onUseBlock(Player player, Level level,
			InteractionHand hand, BlockHitResult hitResult) {

		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

		BlockPos pos = hitResult.getBlockPos();
		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock() instanceof PistonBaseBlock)) return InteractionResult.PASS;
		if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
		if (!state.getValue(PistonBaseBlock.EXTENDED)) return InteractionResult.PASS;
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

		PistonExtensionData data = PistonExtensionData.get(serverLevel);
		int currentLength = data.getExtensionLength(pos);
		int newLength = (currentLength % 3) + 1;
		Direction facing = state.getValue(DirectionalBlock.FACING);
		boolean sticky = state.getBlock() == Blocks.STICKY_PISTON;

		removeExtension(level, pos, facing);
		data.setExtensionLength(pos, newLength);
		placeExtension(serverLevel, pos, facing, newLength, sticky);

		if (sticky && newLength < currentLength) {
			BlockPos oldStuckPos = offsetPos(pos, facing, currentLength + 1);
			BlockPos newStuckPos = offsetPos(pos, facing, newLength + 1);
			BlockState stuckState = serverLevel.getBlockState(oldStuckPos);
			if (!stuckState.isAir() && !stuckState.canBeReplaced()
					&& PistonBaseBlock.isPushable(stuckState, serverLevel, oldStuckPos,
							facing.getOpposite(), false, facing)
					&& serverLevel.getBlockState(newStuckPos).isAir()) {
				serverLevel.setBlock(newStuckPos, stuckState, Block.UPDATE_ALL);
				serverLevel.removeBlock(oldStuckPos, false);
			}
		}

		player.sendOverlayMessage(
				Component.literal("Piston extension: " + newLength + " block"
						+ (newLength > 1 ? "s" : "")));

		return InteractionResult.SUCCESS;
	}
}
