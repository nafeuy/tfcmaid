package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.dries007.tfc.common.TFCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.config.WeedConfigManager;
import org.jetbrains.annotations.NotNull;

public class TaskTFCWeed implements IFarmTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_weed");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.SHORT_GRASS.getDefaultInstance();
    }

    @Override
    public boolean isSeed(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canHarvest(@NotNull EntityMaid maid, @NotNull BlockPos cropPos, BlockState cropState) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(cropState.getBlock());

        if (WeedConfigManager.isBlockBlacklisted(blockId.toString())) {
            return false;
        }

        ItemStack blockItem = cropState.getBlock().asItem().getDefaultInstance();
        return blockItem.is(TFCTags.Items.PLANTS) || blockItem.is(TFCTags.Items.WILD_CROPS);
    }

    @Override
    public void harvest(EntityMaid maid, @NotNull BlockPos cropPos, @NotNull BlockState cropState) {
        maid.destroyBlock(cropPos);
    }

    @Override
    public boolean canPlant(@NotNull EntityMaid maid, @NotNull BlockPos basePos, @NotNull BlockState baseState, @NotNull ItemStack seed) {
        return false;
    }

    @Override
    public @NotNull ItemStack plant(@NotNull EntityMaid maid, @NotNull BlockPos basePos, @NotNull BlockState baseState, @NotNull ItemStack seed) {
        return seed;
    }

    public @NotNull String getMaidActionSummary() {
        return "Clear nearby TFC plants, wild crops, and weeds.";
    }
}
