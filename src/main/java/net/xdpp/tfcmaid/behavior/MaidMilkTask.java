package net.xdpp.tfcmaid.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.dries007.tfc.common.entities.livestock.DairyAnimal;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.xdpp.tfcmaid.util.MaidEquipmentHelper;

/**
 * 女仆挤奶任务 - 核心是处理容器、堆叠这些细节
 */
public class MaidMilkTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 12;
    private final float speedModifier;
    private LivingEntity dairyAnimal = null;

    public MaidMilkTask(float speedModifier) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = speedModifier;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        dairyAnimal = null;

        if (!findAndEquipMilkContainer(maid)) {
            return;
        }

        java.util.List<LivingEntity> candidates = this.getEntities(maid)
                .find(e -> maid.isWithinRestriction(e.blockPosition()))
                .filter(LivingEntity::isAlive)
                .filter(e -> e instanceof DairyAnimal)
                .filter(maid::canPathReach)
                .toList();
        
        if (!candidates.isEmpty()) {
            int index = maid.getRandom().nextInt(candidates.size());
            dairyAnimal = candidates.get(index);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, dairyAnimal, this.speedModifier, 0);
        }

        if (dairyAnimal != null && dairyAnimal.closerThan(maid, 2)) {
            DairyAnimal animal = (DairyAnimal) dairyAnimal;
            ItemStack held = maid.getMainHandItem();
            
            if (!held.isEmpty()) {
                ItemStack singleBucket = held.copyWithCount(1);
                IFluidHandlerItem destFluidItemHandler = singleBucket.getCapability(Capabilities.FluidHandler.ITEM);

                if (destFluidItemHandler != null) {
                    if (animal.isReadyForAnimalProduct()) {
                        final FluidStack milk = new FluidStack(animal.getMilkFluid(), FluidHelpers.BUCKET_VOLUME);
                        final AnimalProductEvent event = new AnimalProductEvent(worldIn, animal.blockPosition(), null, animal, milk, singleBucket, 1);

                        if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
                            FluidStack eventMilk = event.getFluidProduct();
                            int filled = destFluidItemHandler.fill(eventMilk, IFluidHandlerItem.FluidAction.SIMULATE);
                            if (filled > 0) {
                                destFluidItemHandler.fill(eventMilk, IFluidHandlerItem.FluidAction.EXECUTE);
                                ItemStack filledBucket = destFluidItemHandler.getContainer();
                                
                                if (held.getCount() == 1) {
                                    maid.setItemInHand(InteractionHand.MAIN_HAND, filledBucket);
                                } else {
                                    var backpack = maid.getAvailableBackpackInv();
                                    // Do not consume a container or put the animal on cooldown when the
                                    // filled result cannot be stored. The simulated and real insert run
                                    // consecutively on the server thread, so the real insert is atomic
                                    // with respect to other inventory mutations.
                                    if (!ItemHandlerHelper.insertItemStacked(backpack, filledBucket, true).isEmpty()) {
                                        dairyAnimal = null;
                                        return;
                                    }
                                    held.shrink(1);
                                    ItemHandlerHelper.insertItemStacked(backpack, filledBucket, false);
                                }
                                
                                animal.setProductsCooldown();
                                animal.addUses(event.getUses());
                                maid.playSound(SoundEvents.COW_MILK, 1.0f, 1.0f);
                                maid.swing(InteractionHand.MAIN_HAND);
                            }
                        }
                    }
                }
            }
            dairyAnimal = null;
        }
    }

    private boolean findAndEquipMilkContainer(EntityMaid maid) {
        return MaidEquipmentHelper.findAndEquipItemWithValidation(maid, stack -> {
            ItemStack singleStack = stack.copyWithCount(1);
            IFluidHandlerItem handler = singleStack.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler != null && canAcceptMoreMilk(handler)) {
                return singleStack;
            }
            return null;
        });
    }

    private boolean canAcceptMoreMilk(IFluidHandlerItem handler) {
        int simulatedFill = handler.fill(new FluidStack(NeoForgeMod.MILK.get(), FluidHelpers.BUCKET_VOLUME), IFluidHandlerItem.FluidAction.SIMULATE);
        return simulatedFill > 0;
    }

    private NearestVisibleLivingEntities getEntities(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
    }
}
