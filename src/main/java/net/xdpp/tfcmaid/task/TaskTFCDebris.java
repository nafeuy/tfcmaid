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
import org.jetbrains.annotations.NotNull;

public class TaskTFCDebris implements IFarmTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_debris");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return BuiltInRegistries.ITEM.getOptional(ResourceLocation.parse("tfc:rock/mossy_loose/conglomerate"))
                .map(ItemStack::new)
                .orElse(Items.FLINT.getDefaultInstance());
    }

    @Override
    public boolean isSeed(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public boolean canHarvest(@NotNull EntityMaid maid, @NotNull BlockPos cropPos, BlockState cropState) {
        ItemStack blockItem = cropState.getBlock().asItem().getDefaultInstance();
        return blockItem.is(TFCTags.Items.TWIGS) || cropState.is(TFCTags.Blocks.STONES_LOOSE);
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
        return "Collect nearby TFC twigs and loose rocks.";
    }
}
