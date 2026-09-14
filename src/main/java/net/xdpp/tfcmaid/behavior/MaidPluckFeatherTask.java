package net.xdpp.tfcmaid.behavior;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.common.collect.ImmutableMap;
import net.dries007.tfc.common.TFCDamageTypes;
import net.dries007.tfc.common.entities.Pluckable;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.events.AnimalProductEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class MaidPluckFeatherTask extends MaidCheckRateTask {
    private static final int MAX_DELAY_TIME = 12;
    private final float speedModifier;
    private LivingEntity pluckableEntity = null;

    public MaidPluckFeatherTask(float speedModifier) {
        super(ImmutableMap.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
        this.speedModifier = speedModifier;
        this.setMaxCheckRate(MAX_DELAY_TIME);
    }

    @Override
    protected void start(ServerLevel worldIn, EntityMaid maid, long gameTimeIn) {
        pluckableEntity = null;

        java.util.List<LivingEntity> candidates = this.getEntities(maid)
                .find(e -> maid.isWithinRestriction(e.blockPosition()))
                .filter(LivingEntity::isAlive)
                .filter(e -> e instanceof Pluckable)
                .filter(e -> e instanceof TFCAnimalProperties && ((TFCAnimalProperties) e).getAgeType() == Age.ADULT)
                .filter(maid::canPathReach)
                .toList();
        
        if (!candidates.isEmpty()) {
            int index = maid.getRandom().nextInt(candidates.size());
            pluckableEntity = candidates.get(index);
            BehaviorUtils.setWalkAndLookTargetMemories(maid, pluckableEntity, this.speedModifier, 0);
        }

        if (pluckableEntity != null && pluckableEntity.closerThan(maid, 2)) {
            Pluckable pluckable = (Pluckable) pluckableEntity;
            long currentTick = Calendars.get(worldIn).getTicks();
            long lastPluckedTick = pluckable.getLastPluckedTick();
            if (lastPluckedTick <= 0 || lastPluckedTick + Pluckable.PLUCKING_COOLDOWN <= currentTick) {
                if (pluckableEntity.getHealth() / pluckableEntity.getMaxHealth() > 0.15001f) {
                    TFCAnimalProperties properties = (TFCAnimalProperties) pluckableEntity;
                    if (properties.getUses() < properties.getUsesToElderly()) {
                        ItemStack feather = new ItemStack(Items.FEATHER, maid.getRandom().nextInt(3) + 1);
                        if (AnimalProductEvent.produce(worldIn, pluckableEntity.blockPosition(), properties,
                                feather, ItemStack.EMPTY, 1)) {
                            TFCDamageTypes.pluck(pluckableEntity, pluckableEntity.getMaxHealth() * 0.15f, null);
                            pluckable.setLastPluckedTick(currentTick);
                            maid.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                }
            }
            pluckableEntity = null;
        }
    }

    private NearestVisibleLivingEntities getEntities(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
    }
}
