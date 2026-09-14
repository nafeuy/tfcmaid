package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IFarmTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.config.WeedConfigManager;
import org.jetbrains.annotations.NotNull;

public class TaskTFCWeed implements IFarmTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_weed");

    public static final TagKey<Block> TFC_PLANTS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("tfc:plants"));
    public static final TagKey<Block> TFC_WILD_CROPS = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("tfc:wild_crops"));

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

        if (cropState.is(TFC_PLANTS)) {
            return true;
        }

        if (cropState.is(TFC_WILD_CROPS)) {
            return true;
        }

        return false;
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
