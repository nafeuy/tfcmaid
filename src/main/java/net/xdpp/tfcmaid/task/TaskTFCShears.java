package net.xdpp.tfcmaid.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.FunctionCallSwitchResult;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidShearTask;
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
import net.neoforged.neoforge.common.ItemAbilities;
import net.xdpp.tfcmaid.Tfcmaid;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate; // 添加这个导入

public class TaskTFCShears implements IMaidTask {
    public static final ResourceLocation UID = ResourceLocation.parse(Tfcmaid.MODID + ":tfc_shears");

    @Override
    public @NotNull ResourceLocation getUid() {
        return UID;
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return Items.SHEARS.getDefaultInstance();
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound(@NotNull EntityMaid maid) {
        return SoundUtil.environmentSound(maid, InitSounds.MAID_SHEARS.get(), 0.5f);
    }

    @Override
    public @NotNull List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of(5, new MaidShearTask(0.6f)));
    }

    @Override
    public @NotNull FunctionCallSwitchResult onFunctionCallSwitch(EntityMaid maid) {
        if (maid.getMainHandItem().canPerformAction(ItemAbilities.SHEARS_HARVEST)) {
            return FunctionCallSwitchResult.NO_CHANGE;
        }
        return FunctionCallSwitchResult.MISSING_REQUIRED_ITEM;
    }
    
    // 剪刀条件检查方法
    private boolean hasShears(EntityMaid maid) {
        return maid.getMainHandItem().canPerformAction(ItemAbilities.SHEARS_HARVEST);
    }
    
    // 条件描述方法
    @Override
    public @NotNull List<Pair<String, Predicate<EntityMaid>>> getConditionDescription(@NotNull EntityMaid maid) {
        return Lists.newArrayList(Pair.of("has_shears", this::hasShears));
    }
}
