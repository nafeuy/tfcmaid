package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.FunctionCallSwitchResult;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.behavior.MaidFeedTask;
import net.xdpp.tfcmaid.util.WirelessIOHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class TaskTFCFeed implements IMaidTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_feed");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.WHEAT.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(@NotNull EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_FEED_ANIMAL.get(), 0.5f);
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5, new MaidFeedTask(0.6f)));
    }

    @Override
    public @NotNull FunctionCallSwitchResult onFunctionCallSwitch(@NotNull EntityMaid maid) {
        if (hasFood(maid)) {
            return FunctionCallSwitchResult.NO_CHANGE;
        }
        return FunctionCallSwitchResult.MISSING_REQUIRED_ITEM;
    }

    private boolean hasFood(EntityMaid maid) {
        ItemStack mainHand = maid.getMainHandItem();
        
        if (!mainHand.isEmpty() && isValidFood(mainHand)) {
            return true;
        }

        var backpack = maid.getAvailableBackpackInv();
        for (int i = 0; i < backpack.getSlots(); i++) {
            ItemStack stack = backpack.getStackInSlot(i);
            if (!stack.isEmpty() && isValidFood(stack)) {
                return true;
            }
        }
        return WirelessIOHelper.hasMatchingItemInChest(maid, this::isValidFood);
    }

    private boolean isValidFood(ItemStack stack) {
        return !FoodCapability.isRotten(stack);
    }

    @Override
    public @NotNull List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_food", this::hasFood));
    }
}
