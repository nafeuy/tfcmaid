package net.xdpp.tfcmaid.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.function.Predicate;

/**
 * 女仆装备物品的工具类
 * <p>
 * 封装了从女仆背包中查找并装备物品的通用逻辑
 */
public class MaidEquipmentHelper {

    /**
     * 从女仆背包中查找并装备符合条件的物品
     * 查找顺序：主手 -> 背包
     * 如果主手已经有符合条件的物品，直接返回 true
     * 否则从背包中查找，找到后装备到主手
     *
     * @param maid 女仆实体
     * @param predicate 物品是否符合条件的谓词
     * @param count 要提取的物品数量
     * @return 是否成功装备了物品
     */
    public static boolean findAndEquipItem(EntityMaid maid, Predicate<ItemStack> predicate, int count) {
        ItemStack mainHand = maid.getMainHandItem();
        
        if (!mainHand.isEmpty() && predicate.test(mainHand)) {
            return true;
        }

        IItemHandler backpack = maid.getAvailableBackpackInv();
        for (int i = 0; i < backpack.getSlots(); i++) {
            ItemStack stack = backpack.getStackInSlot(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                ItemStack extracted = backpack.extractItem(i, count, false);
                if (!mainHand.isEmpty()) {
                    if (!ItemHandlerHelper.insertItemStacked(backpack, mainHand.copy(), true).isEmpty()) {
                        backpack.insertItem(i, extracted, false);
                        continue;
                    }
                    ItemHandlerHelper.insertItemStacked(backpack, mainHand.copy(), false);
                }
                maid.setItemInHand(InteractionHand.MAIN_HAND, extracted);
                return true;
            }
        }

        return false;
    }

    /**
     * 从女仆背包中查找并装备符合条件的物品（默认提取1个）
     *
     * @param maid 女仆实体
     * @param predicate 物品是否符合条件的谓词
     * @return 是否成功装备了物品
     */
    public static boolean findAndEquipItem(EntityMaid maid, Predicate<ItemStack> predicate) {
        return findAndEquipItem(maid, predicate, 1);
    }

    /**
     * Extracts and equips one matching item from an external handler. The maid's
     * current main-hand stack is moved to her backpack first; if it cannot fit,
     * the source is left untouched.
     */
    public static boolean findAndEquipItemFromHandler(EntityMaid maid, IItemHandler source,
                                                       Predicate<ItemStack> predicate) {
        IItemHandler backpack = maid.getAvailableBackpackInv();
        ItemStack mainHand = maid.getMainHandItem();

        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack simulated = source.extractItem(i, 1, true);
            if (simulated.isEmpty() || !predicate.test(simulated)) {
                continue;
            }
            if (!mainHand.isEmpty()
                    && !ItemHandlerHelper.insertItemStacked(backpack, mainHand.copy(), true).isEmpty()) {
                continue;
            }

            ItemStack extracted = source.extractItem(i, 1, false);
            if (extracted.isEmpty() || !predicate.test(extracted)) {
                if (!extracted.isEmpty()) {
                    ItemStack remainder = source.insertItem(i, extracted, false);
                    if (!remainder.isEmpty()) {
                        ItemsUtil.giveItemToMaid(maid, remainder);
                    }
                }
                continue;
            }

            if (!mainHand.isEmpty()) {
                ItemStack remainingHand = ItemHandlerHelper.insertItemStacked(backpack, mainHand.copy(), false);
                if (!remainingHand.isEmpty()) {
                    // A normal item handler is stable between the simulation and
                    // execution above. Restore both stacks if a custom handler is not.
                    maid.setItemInHand(InteractionHand.MAIN_HAND, remainingHand);
                    ItemStack remainder = source.insertItem(i, extracted, false);
                    if (!remainder.isEmpty()) {
                        ItemsUtil.giveItemToMaid(maid, remainder);
                    }
                    return false;
                }
            }

            maid.setItemInHand(InteractionHand.MAIN_HAND, extracted);
            return true;
        }

        return false;
    }
}
