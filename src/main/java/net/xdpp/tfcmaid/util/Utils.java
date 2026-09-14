package net.xdpp.tfcmaid.util;

import com.github.tartaricacid.touhoulittlemaid.api.task.meal.IMaidMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.IFood;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class Utils {
    public static boolean canEat(IMaidMeal meal, EntityMaid entityMaid, ItemStack stack, InteractionHand hand) {
        IFood food = FoodCapability.get(stack);
        if(food != null && food.isRotten()) {
            return false;
        }
        return meal.canMaidEat(entityMaid, stack, hand);
    }
}
