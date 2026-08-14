package justfatlard.piston_extension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonShaftBlock extends DirectionalBlock {
	private static final VoxelShape SHAPE_Z = Block.box(6, 6, 0, 10, 10, 16);
	private static final VoxelShape SHAPE_X = Block.box(0, 6, 6, 16, 10, 10);
	private static final VoxelShape SHAPE_Y = Block.box(6, 0, 6, 10, 16, 10);

	public PistonShaftBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return switch (state.getValue(FACING).getAxis()) {
			case X -> SHAPE_X;
			case Y -> SHAPE_Y;
			case Z -> SHAPE_Z;
		};
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockState behind = level.getBlockState(pos.relative(facing.getOpposite()));

		if (behind.getBlock() instanceof PistonBaseBlock
				&& behind.getValue(PistonBaseBlock.EXTENDED)
				&& behind.getValue(DirectionalBlock.FACING) == facing) {
			return true;
		}

		return behind.getBlock() == PistonExtension.PISTON_SHAFT
				&& behind.getValue(FACING) == facing;
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos,
			Block neighborBlock, Orientation orientation, boolean movedByPiston) {
		if (!level.isClientSide() && !state.canSurvive(level, pos)) {
			level.destroyBlock(pos, false);
		}
	}
}
