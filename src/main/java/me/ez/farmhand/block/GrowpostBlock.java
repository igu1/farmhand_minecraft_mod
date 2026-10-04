package me.ez.farmhand.block;

import com.mojang.serialization.MapCodec;
import me.ez.farmhand.Init;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrowpostBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 16, 13);
    public GrowpostBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.POWER, 0));
    }
    protected MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(GrowpostBlock::new); }
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWER);
    }
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.FARMLAND) || below.isFaceSturdy(level, pos.below(), Direction.UP);
    }
    protected boolean isSignalSource(BlockState state) { return true; }
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) { return state.getValue(BlockStateProperties.POWER); }
    protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction side) { return state.getValue(BlockStateProperties.POWER); }
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new GrowpostBlockEntity(pos, state); }
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, Init.GROWPOST_BE.get(), GrowpostBlockEntity::tick);
    }
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GrowpostBlockEntity post && placer instanceof Player player) {
            post.claim(player);
            if (stack.has(DataComponents.CUSTOM_NAME)) post.setLabel(stack.get(DataComponents.CUSTOM_NAME).getString());
        }
    }
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) return InteractionResult.PASS;
        if (stack.is(Items.NAME_TAG) && stack.has(DataComponents.CUSTOM_NAME)
                && level.getBlockEntity(pos) instanceof GrowpostBlockEntity post) {
            if (!level.isClientSide() && post.canEdit(player)) {
                post.setLabel(stack.get(DataComponents.CUSTOM_NAME).getString());
                if (!player.isCreative()) stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return useWithoutItem(state, level, pos, player, hit);
    }
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GrowpostBlockEntity post) {
            if (post.owner() == null) post.claim(player);
            player.openMenu(post);
        }
        return InteractionResult.SUCCESS;
    }
}
