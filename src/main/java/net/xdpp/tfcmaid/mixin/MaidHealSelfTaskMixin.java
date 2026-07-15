package net.xdpp.tfcmaid.mixin;

import com.github.tartaricacid.touhoulittlemaid.api.task.meal.IMaidMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidHealSelfTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.xdpp.tfcmaid.util.Utils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MaidHealSelfTask.class)
public class MaidHealSelfTaskMixin {

    @Redirect(method = "start(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V", at = @At(value = "INVOKE", target = "Lcom/github/tartaricacid/touhoulittlemaid/api/task/meal/IMaidMeal;canMaidEat(Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Z"), remap = false)
    private boolean canEat(IMaidMeal instance, EntityMaid entityMaid, ItemStack stack, InteractionHand hand) {
        return Utils.canEat(instance, entityMaid, stack, hand);
    }
}
