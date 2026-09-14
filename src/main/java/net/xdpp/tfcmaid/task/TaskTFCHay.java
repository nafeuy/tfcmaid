package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.plant.ShortGrassBlock;
import net.dries007.tfc.common.blocks.plant.TFCTallGrassBlock;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.config.WeedConfigManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class TaskTFCHay implements IFarmTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_hay");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("tfc:straw"))
                .map(ItemStack::new)
                .orElse(Items.WHEAT.getDefaultInstance());
    }

    @Override
    public boolean isSeed(@NotNull ItemStack stack) {
        return false;
    }

    private boolean hasRequiredTools(EntityMaid maid) {
        ItemStack mainHand = maid.getMainHandItem();
        return mainHand.getItem() instanceof HoeItem 
                || mainHand.is(TFCTags.Items.TOOLS_KNIFE)
                || mainHand.is(TFCTags.Items.TOOLS_SCYTHE);
    }

    private boolean hasInventorySpace(EntityMaid maid) {
        CombinedInvWrapper inv = maid.getAvailableInv(true);
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty() || stack.getCount() < stack.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canHarvest(@NotNull EntityMaid maid, @NotNull BlockPos cropPos, @NotNull BlockState cropState) {
        if (!hasRequiredTools(maid)) {
            return false;
        }
        
        if (!hasInventorySpace(maid)) {
            return false;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(cropState.getBlock());

        if (WeedConfigManager.isBlockBlacklisted(blockId.toString())) {
            return false;
        }

        Block block = cropState.getBlock();
        return block instanceof ShortGrassBlock || block instanceof TFCTallGrassBlock;
    }

    @Override
    public void harvest(EntityMaid maid, @NotNull BlockPos cropPos, @NotNull BlockState cropState) {
        if (maid.level() instanceof ServerLevel serverLevel) {
            if (maid.canDestroyBlock(cropPos)) {
                BlockEntity blockEntity = cropState.hasBlockEntity() ? serverLevel.getBlockEntity(cropPos) : null;
                maid.dropResourcesToMaidInv(cropState, serverLevel, cropPos, blockEntity, maid, maid.getMainHandItem());
                serverLevel.setBlock(cropPos, cropState.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public boolean canPlant(@NotNull EntityMaid maid,@NotNull BlockPos basePos, @NotNull BlockState baseState, @NotNull ItemStack seed) {
        return false;
    }

    @Override
    public @NotNull ItemStack plant(@NotNull EntityMaid maid, @NotNull BlockPos basePos, @NotNull BlockState baseState, @NotNull ItemStack seed) {
        return seed;
    }

    @Override
    public @NotNull List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(@NotNull EntityMaid maid) {
        return Lists.newArrayList(
                Pair.of("has_hoe_or_knife", this::hasRequiredTools)
        );
    }
}
