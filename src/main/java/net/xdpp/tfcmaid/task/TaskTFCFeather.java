package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.FunctionCallSwitchResult;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitSounds;
import com.github.tartaricacid.touhoulittlemaid.util.SoundUtil;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.behavior.MaidPluckFeatherTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class TaskTFCFeather implements IMaidTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_feather");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.FEATHER.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(@NotNull EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_FEED_ANIMAL.get(), 0.5f);
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5, new MaidPluckFeatherTask(0.6f)));
    }

    @Override
    public @NotNull FunctionCallSwitchResult onFunctionCallSwitch(@NotNull EntityMaid maid) {
        return FunctionCallSwitchResult.NO_CHANGE;
    }

    @Override
    public @NotNull List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(@NotNull EntityMaid maid) {
        return Lists.newArrayList();
    }
}
