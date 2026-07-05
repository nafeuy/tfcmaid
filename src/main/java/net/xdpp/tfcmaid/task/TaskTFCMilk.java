package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.FunctionCallSwitchResult;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.behavior.MaidMilkTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class TaskTFCMilk implements IMaidTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_milk");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.MILK_BUCKET.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(@NotNull EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_FEED_ANIMAL.get(), 0.5f);
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5, new MaidMilkTask(0.6f)));
    }

    @Override
    public @NotNull FunctionCallSwitchResult onFunctionCallSwitch(@NotNull EntityMaid maid) {
        if (hasContainer(maid)) {
            return FunctionCallSwitchResult.NO_CHANGE;
        }
        return FunctionCallSwitchResult.MISSING_REQUIRED_ITEM;
    }

    private boolean hasContainer(EntityMaid maid) {
        ItemStack mainHand = maid.getMainHandItem();
        
        if (!mainHand.isEmpty()) {
            ItemStack singleStack = mainHand.copyWithCount(1);
            IFluidHandlerItem handler = Helpers.getCapability(singleStack, Capabilities.FLUID_ITEM);
            if (handler != null && canAcceptMoreMilk(handler)) {
                return true;
            }
        }

        var backpack = maid.getAvailableBackpackInv();
        for (int i = 0; i < backpack.getSlots(); i++) {
            ItemStack stack = backpack.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemStack singleStack = stack.copyWithCount(1);
                IFluidHandlerItem handler = Helpers.getCapability(singleStack, Capabilities.FLUID_ITEM);
                if (handler != null && canAcceptMoreMilk(handler)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAcceptMoreMilk(IFluidHandlerItem handler) {
        int simulatedFill = handler.fill(new FluidStack(net.minecraftforge.common.ForgeMod.MILK.get(), FluidHelpers.BUCKET_VOLUME), IFluidHandlerItem.FluidAction.SIMULATE);
        return simulatedFill > 0;
    }

    @Override
    public @NotNull List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_container", this::hasContainer));
    }
}
