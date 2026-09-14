package net.xdpp.tfcmaid.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.ItemsUtil;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * 推磨工作任务类
 * 继承自 AbstractBlockEntityWorkTask，实现推磨操作的完整流程
 * 工作流程：
 * 1. 检查推磨是否有 handstone（手石），如果没有，从背包找一个放上去
 * 2. 检查输出槽，如果有物品，取出来放到女仆背包
 * 3. 按顺序找可以磨的物品：主手 → 副手 → 背包
 * 4. 将可磨物品放到推磨输入槽，调用 startGrinding() 开始推磨
 * 5. 等待推磨完成（90 tick），自动结束后收集输出
 * 功能特点：
 * - 支持从背包获取手石
 * - 按优先级顺序找可磨物品（主手 > 副手 > 背包）
 * - 距离检查：只有在推磨4格范围内才能工作
 */
public class MaidQuernWorkTask extends AbstractBlockEntityWorkTask<QuernBlockEntity> {
    /**
     * 推磨槽位定义
     */
    private static final int SLOT_HANDSTONE = 0;
    private static final int SLOT_INPUT = 1;
    private static final int SLOT_OUTPUT = 2;

    public MaidQuernWorkTask(double closeEnoughDist) {
        super();
    }

    @Override
    protected Class<QuernBlockEntity> getBlockEntityClass() {
        return QuernBlockEntity.class;
    }

    @Override
    protected void tickWork(ServerLevel world, EntityMaid maid, long gameTime, QuernBlockEntity quern) {
        var inventory = getQuernInventory(quern);

        /**
         * 1. 先检查输出槽，如果有东西就取出来给女仆
         */
        ItemStack outputStack = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!outputStack.isEmpty()) {
            ItemStack extracted = outputStack.copy();
            inventory.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
            ItemsUtil.giveItemToMaid(maid, extracted);
            quern.markForSync();
        }

        /**
         * 2. 检查推磨是否有 handstone（手石），如果没有，从女仆背包找一个放上去
         */
        ItemStack handstoneStack = inventory.getStackInSlot(SLOT_HANDSTONE);
        if (handstoneStack.isEmpty()) {
            ItemStack foundHandstone = findHandstoneInInventory(maid);
            if (!foundHandstone.isEmpty()) {
                inventory.setStackInSlot(SLOT_HANDSTONE, foundHandstone);
                quern.markForSync();
            }
        }

        /**
         * 3. 检查推磨是否正在工作，如果正在工作就什么都不做
         */
        if (quern.isGrinding()) {
            return;
        }

        /**
         * 4. 检查输入槽是否有物品可以磨
         */
        ItemStack inputStack = inventory.getStackInSlot(SLOT_INPUT);
        if (!inputStack.isEmpty()) {
            QuernRecipe recipe = QuernRecipe.getRecipe(inputStack);
            if (recipe != null && recipe.matches(inputStack)) {
                quern.startGrinding();
                return;
            }

            // Never overwrite a non-recipe stack already present in the input
            // slot. Return it first, preserving all of its data components.
            inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
            ItemsUtil.giveItemToMaid(maid, inputStack.copy());
            quern.markForSync();
            return;
        }

        /**
         * 5. 输入槽空或者没有合适配方，从女仆背包找可磨物品
         * 按顺序：主手 → 副手 → 背包
         */
        ItemStack itemToGrind = findGrindableItem(maid, world);
        if (!itemToGrind.isEmpty()) {
            inventory.setStackInSlot(SLOT_INPUT, itemToGrind);
            quern.markForSync();
            quern.startGrinding();
        }
    }

    /**
     * 获取推磨公开的内部物品栏视图。
     *
     * @param quern 推磨方块实体
     * @return 推磨的物品处理器
     */
    private IItemHandlerModifiable getQuernInventory(QuernBlockEntity quern) {
        return quern.getInventory();
    }

    /**
     * 从女仆背包找手石
     *
     * @param maid 女仆实体
     * @return 找到的手石，找不到返回空
     */
    private ItemStack findHandstoneInInventory(EntityMaid maid) {
        for (int i = 0; i < maid.getMaidInv().getSlots(); i++) {
            ItemStack stack = maid.getMaidInv().getStackInSlot(i);
            if (stack.is(TFCTags.Items.QUERN_HANDSTONES)) {
                return maid.getMaidInv().extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * 从女仆找可磨的物品，按优先级：主手 → 副手 → 背包
     * 找到后消耗1个物品
     *
     * @param maid 女仆实体
     * @param world 世界
     * @return 找到的可磨物品（1个），找不到返回空
     */
    private ItemStack findGrindableItem(EntityMaid maid, ServerLevel world) {
        // 先检查主手
        ItemStack mainHand = maid.getMainHandItem();
        if (!mainHand.isEmpty()) {
            QuernRecipe recipe = QuernRecipe.getRecipe(mainHand);
            if (recipe != null && recipe.matches(mainHand)) {
                ItemStack result = mainHand.copy();
                result.setCount(1);
                mainHand.shrink(1);
                return result;
            }
        }

        // 再检查副手
        ItemStack offHand = maid.getOffhandItem();
        if (!offHand.isEmpty()) {
            QuernRecipe recipe = QuernRecipe.getRecipe(offHand);
            if (recipe != null && recipe.matches(offHand)) {
                ItemStack result = offHand.copy();
                result.setCount(1);
                offHand.shrink(1);
                return result;
            }
        }

        // 最后检查背包
        for (int i = 0; i < maid.getMaidInv().getSlots(); i++) {
            ItemStack stack = maid.getMaidInv().getStackInSlot(i);
            if (!stack.isEmpty()) {
                QuernRecipe recipe = QuernRecipe.getRecipe(stack);
                if (recipe != null && recipe.matches(stack)) {
                    return maid.getMaidInv().extractItem(i, 1, false);
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
