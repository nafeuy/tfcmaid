package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.common.blocks.plant.fruit.SpreadingBushBlock;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.xdpp.tfcmaid.Tfcmaid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class TaskTFCBerryBush implements IFarmTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_berry_bush");

    public TaskTFCBerryBush() {
    }

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(@NotNull EntityMaid maid) {
        return Lists.newArrayList();
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return TFCItems.FOOD.get(Food.BLACKBERRY).get().getDefaultInstance();
    }

    @Override
    public boolean isSeed(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canHarvest(EntityMaid maid, BlockPos cropPos, @NotNull BlockState cropState) {
        Level level = maid.level();
        
        BlockPos basePos = cropPos.below();
        
        return iterateNearbyPositions(basePos, (pos) -> isPosFruiting(level, pos));
    }
    
    private boolean isPosFruiting(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        if (!(block instanceof SeasonalPlantBlock || block instanceof SpreadingBushBlock)) {
            return false;
        }
        
        if (state.hasProperty(SeasonalPlantBlock.LIFECYCLE)) {
            return state.getValue(SeasonalPlantBlock.LIFECYCLE) == Lifecycle.FRUITING;
        }
        
        return false;
    }

    @Override
    public void harvest(@NotNull EntityMaid maid, BlockPos cropPos, @NotNull BlockState cropState) {
        BlockPos basePos = cropPos.below();
        
        iterateNearbyPositions(basePos, (pos) -> {
            harvestPos(maid, pos);
            return false;
        });
    }
    
    /**
     * 遍历附近的所有位置（垂直和水平方向）
     * 对于每个位置，调用传入的函数
     * 如果函数返回 true，就立即停止遍历并返回 true
     * 否则遍历所有位置并返回 false
     */
    private boolean iterateNearbyPositions(BlockPos basePos, java.util.function.Function<BlockPos, Boolean> action) {
        for (int y = -2; y <= 5; y++) {
            BlockPos checkPos = basePos.above(y);
            if (action.apply(checkPos)) {
                return true;
            }
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos sidePos = checkPos.relative(d);
                if (action.apply(sidePos)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void harvestPos(EntityMaid maid, BlockPos pos) {
        Level level = maid.level();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        
        if (!(block instanceof SeasonalPlantBlock seasonalPlant)) {
            return;
        }
        
        if (!state.hasProperty(SeasonalPlantBlock.LIFECYCLE) || 
            state.getValue(SeasonalPlantBlock.LIFECYCLE) != Lifecycle.FRUITING) {
            return;
        }

        level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
            SoundSource.PLAYERS, 1.0f, level.getRandom().nextFloat() + 0.7f + 0.3f);
        
        ItemStack product = seasonalPlant.getProductItem(level.random);
        ItemsUtil.giveItemToMaid(maid, product);
        
        BlockState newState = seasonalPlant.stateAfterPicking(state);
        level.setBlockAndUpdate(pos, newState);
        
        maid.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public boolean canPlant(@NotNull EntityMaid maid, @NotNull BlockPos basePos, @NotNull BlockState baseState, @NotNull ItemStack seed) {
        return false;
    }

    @Override
    public @NotNull ItemStack plant(@NotNull EntityMaid maid, @NotNull BlockPos basePos, @NotNull BlockState baseState, @NotNull ItemStack seed) {
        return seed;
    }
    
    @Override
    public boolean checkCropPosAbove() {
        return false;
    }
    
    @Override
    public double getCloseEnoughDist() {
        return 3.0;
    }
}
