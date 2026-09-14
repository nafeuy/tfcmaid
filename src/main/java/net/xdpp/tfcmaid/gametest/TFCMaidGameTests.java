package net.xdpp.tfcmaid.gametest;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidWirelessIOEvent;
import com.github.tartaricacid.touhoulittlemaid.api.task.IFeedTask;
import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidHealSelfTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidShearTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidWorkMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskFeedOwner;
import com.github.tartaricacid.touhoulittlemaid.entity.task.meal.DefaultMaidHomeMeal;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO;
import net.dries007.tfc.common.blockentities.BellowsBlockEntity;
import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.OreDeposit;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.LoomBlockEntity;
import net.dries007.tfc.common.blockentities.QuernBlockEntity;
import net.dries007.tfc.common.blocks.crop.Crop;
import net.dries007.tfc.common.blocks.crop.ClimbingCropBlock;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.common.blocks.crop.PickableCropBlock;
import net.dries007.tfc.common.blocks.plant.Plant;
import net.dries007.tfc.common.blocks.plant.fruit.FruitBlocks;
import net.dries007.tfc.common.blocks.plant.fruit.Lifecycle;
import net.dries007.tfc.common.blocks.plant.fruit.SeasonalPlantBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.item.ItemComponent;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.entities.livestock.Age;
import net.dries007.tfc.common.entities.livestock.DairyAnimal;
import net.dries007.tfc.common.entities.livestock.Gender;
import net.dries007.tfc.common.entities.livestock.OviparousAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.entities.livestock.WoolyAnimal;
import net.dries007.tfc.common.entities.misc.ThrownJavelin;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.player.IPlayerInfo;
import net.dries007.tfc.common.player.PlayerInfo;
import net.dries007.tfc.common.recipes.QuernRecipe;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.xdpp.tfcmaid.Tfcmaid;
import net.xdpp.tfcmaid.behavior.MaidBellowsWorkTask;
import net.xdpp.tfcmaid.behavior.MaidFeedTask;
import net.xdpp.tfcmaid.behavior.MaidKillOldTask;
import net.xdpp.tfcmaid.behavior.MaidLoomWorkTask;
import net.xdpp.tfcmaid.behavior.MaidMilkTask;
import net.xdpp.tfcmaid.behavior.MaidPanningTask;
import net.xdpp.tfcmaid.behavior.MaidPluckFeatherTask;
import net.xdpp.tfcmaid.behavior.MaidQuernWorkTask;
import net.xdpp.tfcmaid.task.TaskTFCBellows;
import net.xdpp.tfcmaid.task.TaskTFCBerryBush;
import net.xdpp.tfcmaid.task.TaskTFCDebris;
import net.xdpp.tfcmaid.task.TaskTFCFeather;
import net.xdpp.tfcmaid.task.TaskTFCFeed;
import net.xdpp.tfcmaid.task.TaskTFCHay;
import net.xdpp.tfcmaid.task.TaskTFCJavelinAttack;
import net.xdpp.tfcmaid.task.TaskTFCKillOld;
import net.xdpp.tfcmaid.task.TaskTFCLoom;
import net.xdpp.tfcmaid.task.TaskTFCMilk;
import net.xdpp.tfcmaid.task.TaskTFCNormalCrop;
import net.xdpp.tfcmaid.task.TaskTFCPanning;
import net.xdpp.tfcmaid.task.TaskTFCPickableCrop;
import net.xdpp.tfcmaid.task.TaskTFCQuern;
import net.xdpp.tfcmaid.task.TaskTFCShears;
import net.xdpp.tfcmaid.task.TaskTFCSpreadingCrop;
import net.xdpp.tfcmaid.task.TaskTFCWaterCrop;
import net.xdpp.tfcmaid.task.TaskTFCWeed;
import net.xdpp.tfcmaid.util.MaidEquipmentHelper;
import net.xdpp.tfcmaid.util.TfcCakeEdible;
import net.xdpp.tfcmaid.util.Utils;
import net.xdpp.tfcmaid.util.WirelessIOHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * In-world regression tests for the cross-mod contracts most likely to regress
 * during the 1.21.1 migration. The template is shipped by the exact TLM 1.5.3
 * dependency and intentionally referenced by its full resource location.
 */
@GameTestHolder(Tfcmaid.MODID)
@PrefixGameTestTemplate(false)
public final class TFCMaidGameTests {
    private static final String TEMPLATE = "game_test";
    private static final String TEMPLATE_NAMESPACE = "touhou_little_maid";

    private TFCMaidGameTests() {
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void taskRegistrationAndCropClassification(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        List<IMaidTask> expectedTasks = List.of(
                new TaskTFCShears(),
                new TaskTFCFeather(),
                new TaskTFCMilk(),
                new TaskTFCFeed(),
                new TaskTFCKillOld(),
                new TaskTFCNormalCrop(),
                new TaskTFCWaterCrop(),
                new TaskTFCPickableCrop(),
                new TaskTFCSpreadingCrop(),
                new TaskTFCBerryBush(),
                new TaskTFCJavelinAttack(),
                new TaskTFCPanning(),
                new TaskTFCWeed(),
                new TaskTFCDebris(),
                new TaskTFCLoom(),
                new TaskTFCQuern(),
                new TaskTFCBellows(),
                new TaskTFCHay()
        );

        Set<Object> uniqueIds = new HashSet<>();
        for (IMaidTask expected : expectedTasks) {
            check(uniqueIds.add(expected.getUid()), "Duplicate TFCMaid task id: " + expected.getUid());
            IMaidTask registered = TaskManager.findTask(expected.getUid())
                    .orElseThrow(() -> new AssertionError("Task was not registered: " + expected.getUid()));
            check(registered.getClass() == expected.getClass(),
                    "Wrong implementation registered for " + expected.getUid());
            check(!registered.getIcon().isEmpty(), "Task has an empty icon: " + expected.getUid());
            check(!registered.createBrainTasks(maid).isEmpty(),
                    "Task has no brain behaviors: " + expected.getUid());
        }
        check(expectedTasks.size() == 18, "The regression list must cover all 18 TFCMaid tasks");

        TaskTFCNormalCrop normal = new TaskTFCNormalCrop();
        TaskTFCWaterCrop water = new TaskTFCWaterCrop();
        TaskTFCPickableCrop pickable = new TaskTFCPickableCrop();
        TaskTFCSpreadingCrop spreading = new TaskTFCSpreadingCrop();
        ItemStack wheat = seed(Crop.WHEAT);
        ItemStack tomato = seed(Crop.TOMATO);
        ItemStack rice = seed(Crop.RICE);
        ItemStack redPepper = seed(Crop.RED_BELL_PEPPER);
        ItemStack yellowPepper = seed(Crop.YELLOW_BELL_PEPPER);
        ItemStack pumpkin = seed(Crop.PUMPKIN);
        ItemStack melon = seed(Crop.MELON);

        check(normal.isSeed(wheat) && normal.isSeed(tomato), "Normal crops rejected a supported seed");
        check(!normal.isSeed(rice) && !normal.isSeed(redPepper) && !normal.isSeed(pumpkin),
                "Normal crop task overlaps a specialized crop task");
        check(water.isSeed(rice) && !water.isSeed(wheat), "Water crop classification is incorrect");
        check(pickable.isSeed(redPepper) && pickable.isSeed(yellowPepper) && !pickable.isSeed(rice),
                "Pickable crop classification is incorrect");
        check(spreading.isSeed(pumpkin) && spreading.isSeed(melon) && !spreading.isSeed(tomato),
                "Spreading crop classification is incorrect");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void gatheringUsesTfc4210Tags(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos probe = helper.absolutePos(new BlockPos(4, 2, 4));
        TaskTFCDebris debris = new TaskTFCDebris();
        TaskTFCWeed weed = new TaskTFCWeed();
        TaskTFCHay hay = new TaskTFCHay();

        check(debris.canHarvest(maid, probe,
                        TFCBlocks.WOODS.get(Wood.ACACIA).get(Wood.BlockType.TWIG).get().defaultBlockState()),
                "TFC twig was not recognized as debris");
        check(debris.canHarvest(maid, probe,
                        TFCBlocks.ROCK_BLOCKS.get(Rock.GRANITE).get(Rock.BlockType.LOOSE).get().defaultBlockState()),
                "TFC loose rock was not recognized as debris");
        check(!debris.canHarvest(maid, probe, net.minecraft.world.level.block.Blocks.COBBLESTONE.defaultBlockState()),
                "Vanilla cobblestone was incorrectly recognized as debris");

        ItemStack hoe = Items.IRON_HOE.getDefaultInstance();
        maid.setItemInHand(InteractionHand.MAIN_HAND, hoe);
        check(weed.canHarvest(maid, probe, TFCBlocks.PLANTS.get(Plant.BLUEGRASS).get().defaultBlockState()),
                "TFC plant item tag was not recognized by the weed task");
        check(weed.canHarvest(maid, probe, TFCBlocks.WILD_CROPS.get(Crop.WHEAT).get().defaultBlockState()),
                "TFC wild crop item tag was not recognized by the weed task");
        check(hay.canHarvest(maid, probe, TFCBlocks.PLANTS.get(Plant.BLUEGRASS).get().defaultBlockState()),
                "TFC short grass was not recognized by the hay task");
        check(!hay.canHarvest(maid, probe, net.minecraft.world.level.block.Blocks.DANDELION.defaultBlockState()),
                "Non-grass plant was incorrectly recognized by the hay task");

        BlockPos twigPos = helper.absolutePos(new BlockPos(2, 2, 5));
        Block twig = TFCBlocks.WOODS.get(Wood.ACACIA).get(Wood.BlockType.TWIG).get();
        helper.getLevel().setBlock(twigPos, twig.defaultBlockState(), Block.UPDATE_ALL);
        debris.harvest(maid, twigPos, helper.getLevel().getBlockState(twigPos));
        check(helper.getLevel().isEmptyBlock(twigPos), "Debris task did not remove a TFC twig");
        check(countItem(maid.getAvailableInv(false), twig.asItem()) > 0,
                "Debris task did not collect the twig drop");

        BlockPos weedPos = helper.absolutePos(new BlockPos(4, 2, 5));
        helper.getLevel().setBlock(weedPos,
                TFCBlocks.PLANTS.get(Plant.BLUEGRASS).get().defaultBlockState(), Block.UPDATE_ALL);
        weed.harvest(maid, weedPos, helper.getLevel().getBlockState(weedPos));
        check(helper.getLevel().isEmptyBlock(weedPos), "Weed task did not remove the TFC plant");

        BlockPos hayPos = helper.absolutePos(new BlockPos(6, 2, 5));
        helper.getLevel().setBlock(hayPos,
                TFCBlocks.PLANTS.get(Plant.BLUEGRASS).get().defaultBlockState(), Block.UPDATE_ALL);
        hay.harvest(maid, hayPos, helper.getLevel().getBlockState(hayPos));
        check(helper.getLevel().isEmptyBlock(hayPos), "Hay task did not remove cut grass");
        check(countItem(maid.getAvailableInv(false), TFCItems.STRAW.get()) > 0,
                "Hay task did not collect TFC straw");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void wirelessEventsPreserveComponentsAndRules(GameTestHelper helper) {
        ItemStack freshFood = TFCItems.FOOD.get(Food.RED_APPLE).get().getDefaultInstance();
        freshFood.setCount(2);
        FoodCapability.setCreatedNow(freshFood);
        check(!FoodCapability.isRotten(freshFood), "Test food was already rotten during setup");
        ItemStack rottenFood = freshFood.copyWithCount(1);
        FoodCapability.setRotten(rottenFood);

        ItemStack damagedTool = TFCItems.ROCK_TOOLS.get(RockCategory.IGNEOUS_EXTRUSIVE)
                .get(RockCategory.ItemType.JAVELIN)
                .get().getDefaultInstance();
        damagedTool.setDamageValue(7);

        ItemStack heatedIngot = TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.INGOT).get().getDefaultInstance();
        HeatCapability.setTemperature(heatedIngot, 750f);
        check(HeatCapability.getTemperature(heatedIngot) > 0f, "Test ingot did not acquire heat data");

        ItemStack filledJug = filledWaterJug();

        ItemStackHandler chest = new ItemStackHandler(8);
        chest.setStackInSlot(0, freshFood.copy());
        chest.setStackInSlot(1, rottenFood.copy());
        chest.setStackInSlot(2, damagedTool.copy());
        chest.setStackInSlot(3, heatedIngot.copy());
        chest.setStackInSlot(4, filledJug.copy());
        ItemStackHandler maid = new ItemStackHandler(8);
        ItemStackHandler whitelist = filter(freshFood, damagedTool, heatedIngot, filledJug);
        List<Boolean> protectedSlots = new ArrayList<>(List.of(true, false, false, false, false, false, false, false));

        MaidWirelessIOEvent.ChestToMaid inbound = new MaidWirelessIOEvent.ChestToMaid(
                null, maid, chest, whitelist, false, protectedSlots);
        NeoForge.EVENT_BUS.post(inbound);
        check(inbound.isCanceled(), "TFCMaid did not take ownership of the inbound wireless event");
        check(maid.getStackInSlot(0).isEmpty(), "Protected maid slot accepted an inbound item");
        check(containsExact(maid, freshFood), "Fresh food or its creation-date component was lost");
        check(containsExact(maid, damagedTool), "Tool damage component was lost");
        check(containsExact(maid, heatedIngot), "TFC heat component was lost");
        check(containsExact(maid, filledJug), "Fluid-container component was lost");
        check(ItemStack.isSameItemSameComponents(chest.getStackInSlot(1), rottenFood),
                "Rotten food was incorrectly imported into the maid inventory");
        check(chest.getStackInSlot(0).isEmpty() && chest.getStackInSlot(2).isEmpty()
                        && chest.getStackInSlot(3).isEmpty() && chest.getStackInSlot(4).isEmpty(),
                "An allowed inbound item was not extracted from the chest");

        ItemStackHandler blacklistChest = new ItemStackHandler(3);
        blacklistChest.setStackInSlot(0, freshFood.copy());
        blacklistChest.setStackInSlot(1, heatedIngot.copy());
        ItemStackHandler blacklistDest = new ItemStackHandler(3);
        MaidWirelessIOEvent.ChestToMaid blacklistInbound = new MaidWirelessIOEvent.ChestToMaid(
                null, blacklistDest, blacklistChest, filter(freshFood), true, List.of(false, false, false));
        NeoForge.EVENT_BUS.post(blacklistInbound);
        check(containsExact(blacklistChest, freshFood), "Blacklisted item was imported");
        check(containsExact(blacklistDest, heatedIngot), "Unlisted item was blocked in blacklist mode");

        ItemStackHandler maidSource = new ItemStackHandler(4);
        maidSource.setStackInSlot(0, rottenFood.copy());
        maidSource.setStackInSlot(1, freshFood.copy());
        maidSource.setStackInSlot(2, rottenFood.copy());
        ItemStackHandler outputChest = new ItemStackHandler(4);
        MaidWirelessIOEvent.MaidToChest outbound = new MaidWirelessIOEvent.MaidToChest(
                null, maidSource, outputChest, new ItemStackHandler(9), false,
                List.of(true, false, false, false));
        NeoForge.EVENT_BUS.post(outbound);
        check(outbound.isCanceled(), "TFCMaid did not take ownership of the outbound wireless event");
        check(containsExact(outputChest, rottenFood), "Rotten food was not force-exported");
        check(ItemStack.isSameItemSameComponents(maidSource.getStackInSlot(0), rottenFood),
                "Protected maid slot was exported");
        check(ItemStack.isSameItemSameComponents(maidSource.getStackInSlot(1), freshFood),
                "Fresh food bypassed an empty whitelist");

        ItemStackHandler fullDestination = new ItemStackHandler(1);
        fullDestination.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 64));
        ItemStackHandler retainedSource = new ItemStackHandler(1);
        retainedSource.setStackInSlot(0, heatedIngot.copy());
        MaidWirelessIOEvent.ChestToMaid fullEvent = new MaidWirelessIOEvent.ChestToMaid(
                null, fullDestination, retainedSource, new ItemStackHandler(9), true, List.of(false));
        NeoForge.EVENT_BUS.post(fullEvent);
        check(containsExact(retainedSource, heatedIngot), "Full destination deleted or extracted the source item");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void directEquipmentTransferIsTransactional(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        ItemStack oldHand = TFCItems.METAL_ITEMS.get(Metal.WROUGHT_IRON)
                .get(Metal.ItemType.KNIFE).get().getDefaultInstance();
        oldHand.setDamageValue(4);
        ItemStack filledJug = filledWaterJug();
        maid.setItemInHand(InteractionHand.MAIN_HAND, oldHand.copy());
        ItemStackHandler source = new ItemStackHandler(1);
        source.setStackInSlot(0, filledJug.copy());

        check(MaidEquipmentHelper.findAndEquipItemFromHandler(
                        maid, source, stack -> stack.is(TFCItems.JUG.get())),
                "External item request did not equip a matching item");
        check(ItemStack.isSameItemSameComponents(maid.getMainHandItem(), filledJug),
                "Requested container lost its fluid component");
        check(containsExact(maid.getAvailableBackpackInv(), oldHand),
                "The previous main-hand tool or its damage component was lost");
        check(source.getStackInSlot(0).isEmpty(), "Equipped item remained duplicated in the source");

        EntityMaid fullMaid = spawnMaid(helper, new BlockPos(5, 2, 3));
        IItemHandler available = fullMaid.getAvailableBackpackInv();
        for (int i = 0; i < available.getSlots(); i++) {
            fullMaid.getMaidInv().setStackInSlot(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        fullMaid.setItemInHand(InteractionHand.MAIN_HAND, oldHand.copy());
        ItemStackHandler retained = new ItemStackHandler(1);
        retained.setStackInSlot(0, filledJug.copy());
        check(!MaidEquipmentHelper.findAndEquipItemFromHandler(
                        fullMaid, retained, stack -> stack.is(TFCItems.JUG.get())),
                "Transfer succeeded despite a full backpack and occupied hand");
        check(ItemStack.isSameItemSameComponents(fullMaid.getMainHandItem(), oldHand),
                "Failed transfer changed the maid's hand");
        check(ItemStack.isSameItemSameComponents(retained.getStackInSlot(0), filledJug),
                "Failed transfer changed the external source");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void boundWirelessChestSupportsDirectTaskRequests(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos chestPos = helper.absolutePos(new BlockPos(5, 2, 3));
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        IItemHandler chest = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, chestPos, null);
        check(chest != null, "Vanilla chest did not expose the NeoForge item capability");

        ItemStack wireless = InitItems.WIRELESS_IO.get().getDefaultInstance();
        ItemWirelessIO.setBindingPos(wireless, chestPos);
        ItemWirelessIO.setMode(wireless, false);
        ItemWirelessIO.setFilterMode(wireless, true);
        ItemWirelessIO.setSlotConfig(wireless,
                java.util.Collections.nCopies(maid.getMaidInv().getSlots() + 2, false));
        check(maid.getMaidBauble().insertItem(0, wireless, false).isEmpty(),
                "Wireless I/O bauble could not be equipped");
        check(WirelessIOHelper.hasWirelessIOAndBound(maid),
                "Equipped wireless I/O did not resolve its bound chest");

        ItemStack jug = filledWaterJug();
        check(chest.insertItem(0, jug.copy(), false).isEmpty(), "Could not seed bound chest fixture");
        check(WirelessIOHelper.hasMatchingItemInChest(maid, stack -> stack.is(TFCItems.JUG.get())),
                "Read-only bound-chest lookup missed the filled jug");
        check(WirelessIOHelper.findAndEquipItemFromChest(maid, stack -> stack.is(TFCItems.JUG.get())),
                "Direct task request did not retrieve a filled jug from the bound chest");
        check(ItemStack.isSameItemSameComponents(maid.getMainHandItem(), jug),
                "Bound-chest retrieval lost the jug fluid component");
        check(chest.getStackInSlot(0).isEmpty(), "Bound-chest retrieval duplicated its source item");

        ItemStack heatedIngot = TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.INGOT).get().getDefaultInstance();
        HeatCapability.setTemperature(heatedIngot, 650f);
        ItemStack remainder = WirelessIOHelper.tryInsertToChest(maid, heatedIngot.copy());
        check(remainder.isEmpty(), "Direct bound-chest insertion returned an unexpected remainder");
        check(containsExact(chest, heatedIngot), "Bound-chest insertion lost the ingot heat component");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void tfcCakeBiteLifecycle(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos cakePos = helper.absolutePos(new BlockPos(4, 2, 4));
        TfcCakeEdible edible = new TfcCakeEdible();
        helper.getLevel().setBlock(cakePos, TFCBlocks.CAKE.get().defaultBlockState(), Block.UPDATE_ALL);

        for (int expectedBites = 1; expectedBites <= CakeBlock.MAX_BITES; expectedBites++) {
            check(edible.consume(maid, cakePos, helper.getLevel().getBlockState(cakePos)),
                    "Cake consumption returned false");
            check(helper.getLevel().getBlockState(cakePos).is(TFCBlocks.CAKE.get()),
                    "Cake was removed before its final bite");
            check(helper.getLevel().getBlockState(cakePos).getValue(CakeBlock.BITES) == expectedBites,
                    "Cake bite property did not advance to " + expectedBites);
        }
        edible.consume(maid, cakePos, helper.getLevel().getBlockState(cakePos));
        check(helper.getLevel().isEmptyBlock(cakePos), "Fully eaten TFC cake was not removed");
        check(edible.canPlaceAsFood(maid, TFCBlocks.CAKE.get().asItem().getDefaultInstance(), 0),
                "TFC cake item was not recognized as maid food");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void ownerFeedTaskRecognizesAndConsumesTfcDrinks(GameTestHelper helper) {
        Player owner = helper.makeMockPlayer(GameType.DEFAULT_MODE);
        IPlayerInfo playerInfo = IPlayerInfo.get(owner);
        TaskFeedOwner task = new TaskFeedOwner();
        ItemStack waterJug = filledWaterJug();

        playerInfo.setThirst(20f);
        check(task.isFood(waterJug, owner),
                "TLM owner-feed task did not recognize a TFC drinkable fluid container");
        check(task.getPriority(waterJug, owner) == IFeedTask.Priority.HIGH,
                "TFC drink did not receive high priority below 25 thirst");

        playerInfo.setThirst(30f);
        check(task.getPriority(waterJug, owner) == IFeedTask.Priority.LOW,
                "TFC drink did not receive low priority from 25 through 49 thirst");

        playerInfo.setThirst(PlayerInfo.MAX_THIRST);
        check(!task.isFood(waterJug, owner),
                "Owner-feed task offered a TFC drink to a fully hydrated player");
        check(!task.isFood(TFCItems.JUG.get().getDefaultInstance(), owner),
                "Owner-feed task treated an empty TFC jug as drinkable");

        playerInfo.setThirst(20f);
        ItemStack returned = task.feed(waterJug.copy(), owner);
        check(playerInfo.getThirst() > 20f,
                "TLM owner-feed execution did not apply the TFC drink's thirst value");
        check(!ItemStack.isSameItemSameComponents(returned, waterJug),
                "Drinking did not update the TFC fluid-container components");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void maidMealMixinsRejectRottenTfcFood(GameTestHelper helper) {
        ItemStack fresh = TFCItems.FOOD.get(Food.RED_APPLE).get().getDefaultInstance();
        FoodCapability.setCreatedNow(fresh);
        ItemStack rotten = fresh.copy();
        FoodCapability.setRotten(rotten);
        check(fresh.getFoodProperties(null) != null,
                "TFC apple fixture did not expose vanilla food properties to TLM");
        check(!FoodCapability.isRotten(fresh) && FoodCapability.isRotten(rotten),
                "Fresh/rotten TFC meal fixtures have incorrect food components");

        EntityMaid healMaid = spawnMaid(helper, new BlockPos(3, 2, 3));
        healMaid.setItemInHand(InteractionHand.MAIN_HAND, rotten.copy());
        new TestHealMealTask().run(helper.getLevel(), healMaid);
        check(!healMaid.isUsingItem(),
                "Heal-meal Mixin allowed a maid to start eating rotten TFC food");
        healMaid.setItemInHand(InteractionHand.MAIN_HAND, fresh.copy());
        new TestHealMealTask().run(helper.getLevel(), healMaid);
        check(healMaid.isUsingItem(),
                "Heal-meal Mixin blocked fresh TFC food accepted by TLM");

        EntityMaid workMaid = spawnMaid(helper, new BlockPos(5, 2, 3));
        workMaid.setItemInHand(InteractionHand.MAIN_HAND, rotten.copy());
        new TestWorkMealTask().run(helper.getLevel(), workMaid);
        check(!workMaid.isUsingItem(),
                "Work-meal Mixin allowed a maid to start eating rotten TFC food");

        check(!Utils.canEat(new DefaultMaidHomeMeal(),
                        healMaid, rotten, InteractionHand.MAIN_HAND),
                "Shared home/heal/work meal guard accepted rotten TFC food");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void tfcShearingUsesAnimalProductLifecycle(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        WoolyAnimal sheep = spawnMob(helper, TFCEntities.SHEEP.get(), new BlockPos(4, 2, 3));
        makeAdult(sheep);
        sheep.setFamiliarity(1f);
        sheep.setProducedTick(0L);
        maid.setItemInHand(InteractionHand.MAIN_HAND, Items.SHEARS.getDefaultInstance());
        setVisibleAnimals(maid, sheep);

        check(sheep.getAgeType() == Age.ADULT && sheep.isReadyForAnimalProduct(),
                "TFC sheep fixture was not ready to shear");
        int usesBefore = sheep.getUses();
        new TestShearTask().run(helper.getLevel(), maid);

        check(sheep.getUses() == usesBefore + 1,
                "Shearing did not add the exact TFC animal-product use");
        check(sheep.getProductsCooldown() > 0,
                "Shearing did not start the TFC wool cooldown");
        check(maid.getMainHandItem().getDamageValue() == 1,
                "Shearing did not damage the shears exactly once");
        check(countWorldItem(helper, TFCItems.WOOL.get()) == 2,
                "Max-familiarity sheep did not produce exactly two TFC wool");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void tfcFeatherPluckingAppliesCooldownDamageAndDrops(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        OviparousAnimal chicken = spawnMob(helper, TFCEntities.CHICKEN.get(), new BlockPos(4, 2, 3));
        makeAdult(chicken);
        chicken.setUses(0);
        chicken.setLastPluckedTick(Long.MIN_VALUE);
        setVisibleAnimals(maid, chicken);

        float healthBefore = chicken.getHealth();
        long calendarBefore = chicken.calendar().getTicks();
        new TestPluckFeatherTask().run(helper.getLevel(), maid);

        check(chicken.getUses() == 1, "Plucking did not add one TFC animal use");
        check(chicken.getLastPluckedTick() >= calendarBefore,
                "Plucking did not store the TFC calendar cooldown tick");
        check(Math.abs(chicken.getHealth() - (healthBefore - chicken.getMaxHealth() * 0.15f)) < 0.001f,
                "Plucking did not apply the exact 15% TFC health cost");
        int feathers = countWorldItem(helper, Items.FEATHER);
        check(feathers >= 1 && feathers <= 3,
                "Plucking produced an item count outside TFC's inclusive 1-3 range: " + feathers);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void tfcMilkingRetrievesWirelessContainerAndFillsMilk(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        DairyAnimal cow = spawnMob(helper, TFCEntities.COW.get(), new BlockPos(4, 2, 3));
        makeAdult(cow);
        cow.setGender(Gender.FEMALE);
        cow.setFamiliarity(1f);
        cow.setProducedTick(-1_000_000L);
        setVisibleAnimals(maid, cow);

        IItemHandler chest = bindWirelessChest(helper, maid, new BlockPos(6, 2, 3));
        check(chest.insertItem(0, Items.BUCKET.getDefaultInstance(), false).isEmpty(),
                "Could not seed the bound chest with an empty bucket");
        check(cow.isReadyForAnimalProduct(), "TFC cow fixture was not ready to milk");

        int usesBefore = cow.getUses();
        new TestMilkTask().run(helper.getLevel(), maid);

        check(chest.getStackInSlot(0).isEmpty(),
                "Milking did not retrieve its empty container from the bound chest");
        IFluidHandlerItem milkHandler = maid.getMainHandItem().getCapability(Capabilities.FluidHandler.ITEM);
        check(milkHandler != null && milkHandler.getTanks() > 0,
                "Milking result did not retain an item fluid capability");
        FluidStack milk = milkHandler.getFluidInTank(0);
        check(!milk.isEmpty() && milk.getFluid() == cow.getMilkFluid()
                        && milk.getAmount() == FluidHelpers.BUCKET_VOLUME,
                "Milking result did not contain one exact bucket of the cow's TFC milk fluid");
        check(cow.getUses() == usesBefore + 1 && cow.getProductsCooldown() > 0,
                "Milking did not update TFC use and product cooldown state");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void tfcFeedingUsesWirelessFoodAndEnablesMating(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        DairyAnimal female = spawnMob(helper, TFCEntities.COW.get(), new BlockPos(4, 2, 3));
        DairyAnimal male = spawnMob(helper, TFCEntities.COW.get(), new BlockPos(3, 2, 4));
        prepareBreedingAnimal(female, Gender.FEMALE);
        prepareBreedingAnimal(male, Gender.MALE);
        setVisibleAnimals(maid, female, male);

        ItemStack grain = new ItemStack(TFCItems.FOOD.get(Food.WHEAT_GRAIN).get(), 2);
        FoodCapability.setCreatedNow(grain);
        check(female.isFood(grain) && male.isFood(grain),
                "Exact TFC cow food tag rejected wheat grain");
        IItemHandler chest = bindWirelessChest(helper, maid, new BlockPos(6, 2, 3));
        check(chest.insertItem(0, grain.copy(), false).isEmpty(),
                "Could not seed wireless breeding-food fixture");

        TestFeedTask task = new TestFeedTask();
        task.run(helper.getLevel(), maid);
        task.run(helper.getLevel(), maid);

        check(chest.getStackInSlot(0).isEmpty() && maid.getMainHandItem().isEmpty(),
                "Wireless feeding did not consume exactly two units of grain");
        check(!female.isHungry() && !male.isHungry(),
                "Feeding did not update both animals' TFC last-fed state");
        check(female.getFamiliarity() >= TFCAnimalProperties.READY_TO_MATE_FAMILIARITY
                        && male.getFamiliarity() >= TFCAnimalProperties.READY_TO_MATE_FAMILIARITY,
                "Feeding did not raise both adults to the TFC mating familiarity threshold");
        check(female.canMate(male) && male.canMate(female),
                "Opposite-sex fed TFC adults did not become valid mates");

        int maleUsesBefore = male.getUses();
        check(female.getBreedOffspring(helper.getLevel(), male) == null,
                "TFC mammal mating unexpectedly spawned an immediate child");
        check(female.isFertilized() && male.getUses() == maleUsesBefore + 5,
                "TFC mating did not fertilize the female and wear the male by five uses");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void tfcButcherSelectsOldAnimalAndClearsBackpackToWireless(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        DairyAnimal adult = spawnMob(helper, TFCEntities.COW.get(), new BlockPos(3, 2, 4));
        DairyAnimal old = spawnMob(helper, TFCEntities.COW.get(), new BlockPos(4, 2, 3));
        makeAdult(adult);
        makeAdult(old);
        old.setOldTick(Long.MIN_VALUE + 1);
        check(adult.getAgeType() == Age.ADULT && old.getAgeType() == Age.OLD,
                "Butcher age fixtures were not adult/old");
        setVisibleAnimals(maid, adult, old);

        maid.setItemInHand(InteractionHand.MAIN_HAND,
                TFCItems.METAL_ITEMS.get(Metal.WROUGHT_IRON)
                        .get(Metal.ItemType.KNIFE).get().getDefaultInstance());
        ItemStack heatedIngot = TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.INGOT).get().getDefaultInstance();
        HeatCapability.setTemperature(heatedIngot, 700f);
        maid.getMaidInv().setStackInSlot(0, heatedIngot.copy());
        IItemHandler chest = bindWirelessChest(helper, maid, new BlockPos(6, 2, 3));

        new TestKillOldTask().run(helper.getLevel(), maid);

        check(adult.isAlive(), "Butcher selected a non-old TFC animal");
        check(!old.isAlive(), "Butcher did not kill its old TFC animal target");
        check(containsExact(chest, heatedIngot),
                "Butcher pre-drop clearing lost the ingot's TFC heat component");
        check(!containsExact(maid.getAvailableBackpackInv(), heatedIngot),
                "Butcher left an allowed item in the maid backpack");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void normalCropPlantingFertilizesAndBuildsSupports(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos wheatFarmlandPos = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos tomatoFarmlandPos = helper.absolutePos(new BlockPos(6, 2, 4));
        Block farmland = TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)
                .get(SoilBlockType.Variant.ENTISOL).get();
        helper.getLevel().setBlock(wheatFarmlandPos, farmland.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(tomatoFarmlandPos, farmland.defaultBlockState(), Block.UPDATE_ALL);

        FarmlandBlockEntity wheatFarmland = requireBlockEntity(
                helper, wheatFarmlandPos, FarmlandBlockEntity.class);
        check(wheatFarmland.getNutrient(FarmlandBlockEntity.NutrientType.NITROGEN) == 0f,
                "Fresh farmland unexpectedly contained nitrogen");

        maid.getMaidInv().setStackInSlot(0, TFCItems.COMPOST.get().getDefaultInstance());
        maid.getMaidInv().setStackInSlot(1, Items.STICK.getDefaultInstance());
        TaskTFCNormalCrop task = new TaskTFCNormalCrop();

        ItemStack wheatSeed = seed(Crop.WHEAT);
        task.plant(maid, wheatFarmlandPos, helper.getLevel().getBlockState(wheatFarmlandPos), wheatSeed);
        check(wheatSeed.isEmpty(), "Planting wheat did not consume its seed");
        check(helper.getLevel().getBlockState(wheatFarmlandPos.above())
                        .is(TFCBlocks.CROPS.get(Crop.WHEAT).get()),
                "Wheat seed did not place the TFC wheat crop");
        check(wheatFarmland.getNutrient(FarmlandBlockEntity.NutrientType.NITROGEN) == 0.2f,
                "Compost did not add the exact TFC 4.2.10 nitrogen value");
        check(maid.getMaidInv().getStackInSlot(0).isEmpty(),
                "Successful fertilization did not consume one compost");

        BlockPos wheatPos = wheatFarmlandPos.above();
        CropBlock wheatCrop = (CropBlock) TFCBlocks.CROPS.get(Crop.WHEAT).get();
        helper.getLevel().setBlock(wheatPos,
                wheatCrop.defaultBlockState().setValue(wheatCrop.getAgeProperty(), wheatCrop.getMaxAge()),
                Block.UPDATE_ALL);
        CropBlockEntity wheatEntity = requireBlockEntity(helper, wheatPos, CropBlockEntity.class);
        wheatEntity.setGrowth(1f);
        wheatEntity.setYield(1f);
        check(task.canHarvest(maid, wheatPos, helper.getLevel().getBlockState(wheatPos)),
                "Mature TFC wheat was not harvestable");
        task.harvest(maid, wheatPos, helper.getLevel().getBlockState(wheatPos));
        check(helper.getLevel().isEmptyBlock(wheatPos),
                "Normal-crop harvest left the mature wheat block intact");
        check(countItem(maid.getAvailableInv(false), TFCItems.FOOD.get(Food.WHEAT).get()) > 0,
                "Mature wheat product did not reach the maid inventory");

        ItemStack tomatoSeed = seed(Crop.TOMATO);
        task.plant(maid, tomatoFarmlandPos, helper.getLevel().getBlockState(tomatoFarmlandPos), tomatoSeed);
        BlockStateAssertions.assertClimbingCrop(helper, tomatoFarmlandPos.above(), tomatoSeed);
        check(maid.getMaidInv().getStackInSlot(1).isEmpty(),
                "Climbing-crop support did not consume one wooden rod");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void cropVariantsPlantAndHarvestCorrectly(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        Block farmland = TFCBlocks.SOIL.get(SoilBlockType.FARMLAND)
                .get(SoilBlockType.Variant.ENTISOL).get();

        BlockPos waterBase = helper.absolutePos(new BlockPos(2, 2, 6));
        helper.getLevel().setBlock(waterBase, farmland.defaultBlockState(), Block.UPDATE_ALL);
        helper.getLevel().setBlock(waterBase.above(), Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        TaskTFCWaterCrop waterTask = new TaskTFCWaterCrop();
        ItemStack riceSeed = seed(Crop.RICE);
        waterTask.plant(maid, waterBase, helper.getLevel().getBlockState(waterBase), riceSeed);
        check(riceSeed.isEmpty(), "Water-crop planting did not consume the rice seed");
        check(helper.getLevel().getBlockState(waterBase.above()).is(TFCBlocks.CROPS.get(Crop.RICE).get()),
                "Rice was not planted into the water block");
        check(helper.getLevel().getFluidState(waterBase.above()).is(Fluids.WATER),
                "Planted rice did not retain its water state");

        Block riceBlock = TFCBlocks.CROPS.get(Crop.RICE).get();
        CropBlock riceCrop = (CropBlock) riceBlock;
        helper.getLevel().setBlock(waterBase.above(),
                riceBlock.defaultBlockState().setValue(riceCrop.getAgeProperty(), riceCrop.getMaxAge()),
                Block.UPDATE_ALL);
        CropBlockEntity riceEntity = requireBlockEntity(
                helper, waterBase.above(), CropBlockEntity.class);
        riceEntity.setGrowth(1f);
        riceEntity.setYield(1f);
        check(waterTask.canHarvest(maid, waterBase.above(), helper.getLevel().getBlockState(waterBase.above())),
                "Mature flooded rice was not harvestable");
        waterTask.harvest(maid, waterBase.above(), helper.getLevel().getBlockState(waterBase.above()));
        check(!helper.getLevel().getBlockState(waterBase.above()).is(riceBlock),
                "Water-crop harvest left the mature rice block intact");
        check(countItem(maid.getAvailableInv(false), TFCItems.FOOD.get(Food.RICE).get()) > 0,
                "Mature flooded rice product did not reach the maid inventory");

        BlockPos pepperBase = helper.absolutePos(new BlockPos(4, 2, 6));
        helper.getLevel().setBlock(pepperBase, farmland.defaultBlockState(), Block.UPDATE_ALL);
        TaskTFCPickableCrop pickableTask = new TaskTFCPickableCrop();
        ItemStack pepperSeed = seed(Crop.RED_BELL_PEPPER);
        pickableTask.plant(maid, pepperBase, helper.getLevel().getBlockState(pepperBase), pepperSeed);
        BlockPos pepperPos = pepperBase.above();
        PickableCropBlock pepperBlock = (PickableCropBlock) TFCBlocks.CROPS.get(Crop.RED_BELL_PEPPER).get();
        helper.getLevel().setBlock(pepperPos,
                pepperBlock.defaultBlockState().setValue(pepperBlock.getAgeProperty(), pepperBlock.getMaxAge()),
                Block.UPDATE_ALL);
        CropBlockEntity pepper = requireBlockEntity(helper, pepperPos, CropBlockEntity.class);
        pepper.setGrowth(1f);
        pepper.setYield(1f);
        check(pickableTask.canHarvest(maid, pepperPos, helper.getLevel().getBlockState(pepperPos)),
                "Mature pepper was not harvestable");
        pickableTask.harvest(maid, pepperPos, helper.getLevel().getBlockState(pepperPos));
        check(helper.getLevel().getBlockState(pepperPos).is(pepperBlock),
                "Pickable pepper plant was destroyed during harvest");
        check(helper.getLevel().getBlockState(pepperPos).getValue(pepperBlock.getAgeProperty())
                        < pepperBlock.getMaxAge(),
                "Pickable pepper age was not reset after harvest");
        check(pepper.getYield() == 0f, "Pickable pepper yield was not reset");
        check(countItem(maid.getAvailableInv(false), TFCItems.FOOD.get(Food.RED_BELL_PEPPER).get()) > 0,
                "Pickable pepper harvest did not reach the maid inventory");

        BlockPos spreadingBase = helper.absolutePos(new BlockPos(6, 2, 6));
        helper.getLevel().setBlock(spreadingBase, farmland.defaultBlockState(), Block.UPDATE_ALL);
        TaskTFCSpreadingCrop spreadingTask = new TaskTFCSpreadingCrop();
        ItemStack pumpkinSeed = seed(Crop.PUMPKIN);
        spreadingTask.plant(maid, spreadingBase,
                helper.getLevel().getBlockState(spreadingBase), pumpkinSeed);
        BlockPos stemPos = spreadingBase.above();
        BlockPos fruitPos = stemPos.east();
        helper.getLevel().setBlock(fruitPos, TFCBlocks.PUMPKIN.get().defaultBlockState(), Block.UPDATE_ALL);
        requireBlockEntity(helper, fruitPos, DecayingBlockEntity.class)
                .setStack(TFCBlocks.PUMPKIN.get().asItem().getDefaultInstance());
        check(spreadingTask.canHarvest(maid, stemPos, helper.getLevel().getBlockState(stemPos)),
                "Pumpkin stem did not recognize adjacent fruit");
        spreadingTask.harvest(maid, stemPos, helper.getLevel().getBlockState(stemPos));
        check(helper.getLevel().isEmptyBlock(fruitPos), "Spreading-crop harvest did not remove pumpkin fruit");
        check(helper.getLevel().getBlockState(stemPos).is(TFCBlocks.CROPS.get(Crop.PUMPKIN).get()),
                "Spreading-crop harvest destroyed its stem");
        check(helper.getEntities(EntityType.ITEM).stream()
                        .map(ItemEntity::getItem)
                        .anyMatch(stack -> stack.is(TFCBlocks.PUMPKIN.get().asItem())),
                "TFC decaying-block pumpkin payload was lost during harvest");

        BlockPos berryPos = helper.absolutePos(new BlockPos(7, 2, 3));
        Block berryBlock = TFCBlocks.SPREADING_BUSHES.get(FruitBlocks.SpreadingBush.BLACKBERRY).get();
        helper.getLevel().setBlock(berryPos,
                berryBlock.defaultBlockState().setValue(SeasonalPlantBlock.LIFECYCLE, Lifecycle.FRUITING),
                Block.UPDATE_ALL);
        TaskTFCBerryBush berryTask = new TaskTFCBerryBush();
        check(berryTask.canHarvest(maid, berryPos, helper.getLevel().getBlockState(berryPos)),
                "Fruiting TFC blackberry bush was not harvestable");
        berryTask.harvest(maid, berryPos, helper.getLevel().getBlockState(berryPos));
        check(helper.getLevel().getBlockState(berryPos).getValue(SeasonalPlantBlock.LIFECYCLE)
                        != Lifecycle.FRUITING,
                "Berry bush remained fruiting after harvest");
        check(countItem(maid.getAvailableInv(false), TFCItems.FOOD.get(Food.BLACKBERRY).get()) > 0,
                "Berry harvest did not reach the maid inventory");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE, timeoutTicks = 130)
    public static void quernCyclePreservesFoodAndMachineState(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos quernPos = helper.absolutePos(new BlockPos(5, 2, 4));
        helper.getLevel().setBlock(quernPos, TFCBlocks.QUERN.get().defaultBlockState(), Block.UPDATE_ALL);
        QuernBlockEntity quern = requireBlockEntity(helper, quernPos, QuernBlockEntity.class);
        TestQuernWorkTask task = new TestQuernWorkTask();

        ItemStack rejectedInput = TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.INGOT).get().getDefaultInstance();
        HeatCapability.setTemperature(rejectedInput, 750f);
        quern.getInventory().setStackInSlot(QuernBlockEntity.SLOT_INPUT, rejectedInput.copy());
        maid.getMaidInv().setStackInSlot(0, TFCItems.HANDSTONE.get().getDefaultInstance());

        ItemStack grain = TFCItems.FOOD.get(Food.WHEAT_GRAIN).get().getDefaultInstance();
        FoodCapability.setCreatedNow(grain);
        QuernRecipe recipe = QuernRecipe.getRecipe(grain);
        check(recipe != null && recipe.matches(grain), "Exact TFC wheat-grain quern recipe was not loaded");
        ItemStack expectedFlour = recipe.assemble(grain.copy());
        maid.getMaidInv().setStackInSlot(1, grain.copy());

        task.run(helper.getLevel(), maid, quern);
        check(containsExact(maid.getAvailableBackpackInv(), rejectedInput),
                "Invalid quern input or its heat component was not returned to the maid");
        check(quern.getInventory().getStackInSlot(QuernBlockEntity.SLOT_INPUT).isEmpty(),
                "Invalid quern input remained in the machine");
        task.run(helper.getLevel(), maid, quern);

        check(quern.isGrinding(), "Maid did not start the TFC quern");
        check(quern.getInventory().getStackInSlot(QuernBlockEntity.SLOT_HANDSTONE)
                        .is(TFCItems.HANDSTONE.get()),
                "Maid did not install the handstone");
        check(ItemStack.isSameItemSameComponents(
                        quern.getInventory().getStackInSlot(QuernBlockEntity.SLOT_INPUT), grain),
                "Quern input lost its food component");

        helper.runAfterDelay(QuernBlockEntity.MANUAL_TICKS + 5, () -> {
            task.run(helper.getLevel(), maid, quern);
            check(!quern.isGrinding(), "Quern was still grinding after the manual cycle");
            check(quern.getInventory().getStackInSlot(QuernBlockEntity.SLOT_INPUT).isEmpty(),
                    "Completed quern cycle retained its input");
            check(quern.getInventory().getStackInSlot(QuernBlockEntity.SLOT_OUTPUT).isEmpty(),
                    "Maid did not collect the completed quern output");
            check(containsExact(maid.getAvailableBackpackInv(), expectedFlour),
                    "Flour output or copied food component was lost");
            check(quern.getInventory().getStackInSlot(QuernBlockEntity.SLOT_HANDSTONE).getDamageValue() == 1,
                    "Completed quern cycle did not damage the handstone exactly once");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE, timeoutTicks = 260)
    public static void loomCycleRejectsMixedComponentsAndCollectsOutput(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos loomPos = helper.absolutePos(new BlockPos(5, 2, 4));
        helper.getLevel().setBlock(loomPos,
                TFCBlocks.WOODS.get(Wood.ACACIA).get(Wood.BlockType.LOOM).get().defaultBlockState(),
                Block.UPDATE_ALL);
        LoomBlockEntity loom = requireBlockEntity(helper, loomPos, LoomBlockEntity.class);
        TestLoomWorkTask task = new TestLoomWorkTask();

        maid.setItemInHand(InteractionHand.MAIN_HAND, TFCItems.UNREFINED_PAPER.get().getDefaultInstance());
        maid.setItemInHand(InteractionHand.OFF_HAND, Items.STICK.getDefaultInstance());
        maid.getBrain().setMemory(InitEntities.TARGET_POS.get(), new BlockPosTracker(loomPos));

        ItemStack firstComponentGroup = new ItemStack(TFCItems.SOAKED_PAPYRUS_STRIP.get(), 2);
        firstComponentGroup.set(DataComponents.CUSTOM_NAME, Component.literal("group-a"));
        ItemStack secondComponentGroup = new ItemStack(TFCItems.SOAKED_PAPYRUS_STRIP.get(), 2);
        secondComponentGroup.set(DataComponents.CUSTOM_NAME, Component.literal("group-b"));
        maid.getMaidInv().setStackInSlot(0, firstComponentGroup.copy());
        maid.getMaidInv().setStackInSlot(1, secondComponentGroup.copy());

        ItemStack rejectedInput = TFCItems.METAL_ITEMS.get(Metal.COPPER)
                .get(Metal.ItemType.INGOT).get().getDefaultInstance();
        HeatCapability.setTemperature(rejectedInput, 500f);
        loom.getInventory().setStackInSlot(0, rejectedInput.copy());

        check(task.begin(helper.getLevel(), maid), "Loom task rejected a valid TFC loom target");
        task.step(helper.getLevel(), maid);
        check(containsExact(maid.getAvailableBackpackInv(), rejectedInput),
                "Invalid loom input or its heat component was not returned");
        check(loom.getInventory().getStackInSlot(0).isEmpty(),
                "Invalid loom input remained in the machine");

        task.step(helper.getLevel(), maid);
        check(loom.getInventory().getStackInSlot(0).isEmpty(),
                "Loom merged tag-compatible but component-different ingredient stacks");
        check(containsExact(maid.getMaidInv(), firstComponentGroup)
                        && containsExact(maid.getMaidInv(), secondComponentGroup),
                "Rejected component groups were mutated");

        maid.getMaidInv().setStackInSlot(0, new ItemStack(TFCItems.SOAKED_PAPYRUS_STRIP.get(), 4));
        maid.getMaidInv().setStackInSlot(1, ItemStack.EMPTY);
        task.step(helper.getLevel(), maid);
        task.step(helper.getLevel(), maid);
        check(loom.getInventory().getStackInSlot(0).getCount() == 4,
                "Loom task did not insert the exact recipe input count");

        ItemStack expectedOutput = TFCItems.UNREFINED_PAPER.get().getDefaultInstance();
        helper.onEachTick(() -> {
            task.step(helper.getLevel(), maid);
            // One sheet is the recipe selector already held in the main hand;
            // successful collection raises the component-identical total to two.
            if (countExact(maid.getAvailableInv(false), expectedOutput) == 2) {
                check(loom.getProgress() == 0, "Collected loom did not reset progress");
                check(loom.getInventory().getStackInSlot(0).isEmpty(),
                        "Collected loom retained recipe input");
                check(loom.getInventory().getStackInSlot(1).isEmpty(),
                        "Collected loom retained duplicate output");
                check(maid.getOffhandItem().is(Items.STICK), "Loom operation consumed its stick tool");
                helper.succeed();
            }
        });
        helper.runAfterDelay(240, () -> {
            if (countExact(maid.getAvailableInv(false), expectedOutput) != 2) {
                helper.fail("Loom diagnostic: progress=" + loom.getProgress()
                        + ", recipe=" + (loom.getRecipe() == null ? "null" : "loaded")
                        + ", input=" + loom.getInventory().getStackInSlot(0)
                        + ", output=" + loom.getInventory().getStackInSlot(1)
                        + ", mainHand=" + maid.getMainHandItem());
            }
        });
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE, timeoutTicks = 50)
    public static void bellowsTaskUsesPublicPushLifecycle(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        BlockPos bellowsPos = helper.absolutePos(new BlockPos(5, 2, 4));
        helper.getLevel().setBlock(bellowsPos, TFCBlocks.BELLOWS.get().defaultBlockState(), Block.UPDATE_ALL);
        BellowsBlockEntity bellows = requireBlockEntity(helper, bellowsPos, BellowsBlockEntity.class);
        TestBellowsWorkTask task = new TestBellowsWorkTask();

        task.run(helper.getLevel(), maid, bellows);
        helper.runAfterDelay(10, () -> {
            check(bellows.getExtensionLength(0f) >= BellowsBlockEntity.MAX_EXTENSION - 0.001f,
                    "Maid bellows action did not drive the TFC push animation lifecycle");
            task.run(helper.getLevel(), maid, bellows);
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE, timeoutTicks = 170)
    public static void panningConsumesDepositAndReturnsEmptyPan(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(3, 2, 3));
        helper.getLevel().setBlock(helper.absolutePos(new BlockPos(3, 2, 4)),
                Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);

        ItemStack deposit = TFCBlocks.ORE_DEPOSITS.get(Rock.GRANITE)
                .get(OreDeposit.NATIVE_COPPER).get().asItem().getDefaultInstance();
        check(net.dries007.tfc.util.data.Deposit.get(deposit) != null,
                "Exact TFC native-copper deposit data was not loaded");
        maid.setItemInHand(InteractionHand.MAIN_HAND, TFCItems.EMPTY_PAN.get().getDefaultInstance());
        maid.getMaidInv().setStackInSlot(0, deposit.copy());

        TestPanningTask task = new TestPanningTask();
        check(task.begin(helper.getLevel(), maid), "Panning behavior rejected the empty TFC pan");
        ItemStack filledPan = maid.getMainHandItem();
        check(filledPan.is(TFCItems.FILLED_PAN.get()), "Deposit did not fill the maid's pan");
        check(ItemStack.isSameItemSameComponents(
                        filledPan.getOrDefault(TFCComponents.DEPOSIT, ItemComponent.EMPTY).stack(), deposit),
                "Filled pan did not store the exact deposit component");
        check(maid.getMaidInv().getStackInSlot(0).isEmpty(), "Filling the pan did not consume one deposit");

        helper.onEachTick(() -> {
            task.step(helper.getLevel(), maid);
            if (maid.getMainHandItem().is(TFCItems.EMPTY_PAN.get())) {
                check(task.canContinue(helper.getLevel(), maid),
                        "Panning behavior cannot continue from its returned empty pan");
                check(maid.getMainHandItem().getOrDefault(TFCComponents.DEPOSIT, ItemComponent.EMPTY)
                                .stack().isEmpty(),
                        "Returned empty pan retained stale deposit data");
                helper.succeed();
            }
        });
    }

    @GameTest(template = TEMPLATE, templateNamespace = TEMPLATE_NAMESPACE)
    public static void javelinAttackSpawnsExactProjectileAndDamagesWeapon(GameTestHelper helper) {
        EntityMaid maid = spawnMaid(helper, new BlockPos(2, 2, 3));
        Zombie target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 3));
        ItemStack javelin = TFCItems.ROCK_TOOLS.get(RockCategory.IGNEOUS_EXTRUSIVE)
                .get(RockCategory.ItemType.JAVELIN).get().getDefaultInstance();
        javelin.setDamageValue(7);
        maid.setItemInHand(InteractionHand.MAIN_HAND, javelin.copy());

        TaskTFCJavelinAttack task = new TaskTFCJavelinAttack();
        check(task.isWeapon(maid, maid.getMainHandItem()), "TFC javelin was rejected as a ranged weapon");
        task.performRangedAttack(maid, target, 1f);

        check(maid.getMainHandItem().getDamageValue() == 8,
                "Throwing a javelin did not consume exactly one durability");
        List<ThrownJavelin> projectiles = helper.getEntities(TFCEntities.THROWN_JAVELIN.get());
        check(projectiles.size() == 1, "Javelin attack did not spawn exactly one TFC projectile");
        ThrownJavelin projectile = projectiles.get(0);
        check(ItemStack.isSameItemSameComponents(projectile.getItem(), javelin),
                "Projectile did not retain the pre-damage javelin components");
        check(projectile.isNoGravity(), "Maid javelin projectile unexpectedly has gravity");
        check(projectile.pickup == AbstractArrow.Pickup.CREATIVE_ONLY,
                "Maid javelin projectile has an unsafe pickup mode");
        check(projectile.getDeltaMovement().lengthSqr() > 0,
                "Javelin projectile was spawned without velocity");

        EntityMaid metalMaid = spawnMaid(helper, new BlockPos(2, 2, 5));
        Zombie metalTarget = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(7, 2, 5));
        ItemStack metalJavelin = TFCItems.METAL_ITEMS.get(Metal.WROUGHT_IRON)
                .get(Metal.ItemType.JAVELIN).get().getDefaultInstance();
        metalJavelin.setDamageValue(3);
        metalMaid.setItemInHand(InteractionHand.MAIN_HAND, metalJavelin.copy());
        check(task.isWeapon(metalMaid, metalMaid.getMainHandItem()),
                "TFC metal javelin was rejected as a ranged weapon");
        task.performRangedAttack(metalMaid, metalTarget, 1f);
        check(metalMaid.getMainHandItem().getDamageValue() == 4,
                "Throwing a metal javelin did not consume exactly one durability");
        List<ThrownJavelin> bothProjectiles = helper.getEntities(TFCEntities.THROWN_JAVELIN.get());
        check(bothProjectiles.size() == 2
                        && bothProjectiles.stream().anyMatch(entity ->
                        ItemStack.isSameItemSameComponents(entity.getItem(), metalJavelin)),
                "Metal javelin projectile did not preserve its pre-damage components");
        helper.succeed();
    }

    private static EntityMaid spawnMaid(GameTestHelper helper, BlockPos relativePos) {
        EntityMaid maid = new ReachableTestMaid(helper.getLevel());
        // Tests invoke the relevant behavior entry points deterministically. Keep
        // unrelated TLM navigation from mutating fixtures or producing upstream
        // path-cache diagnostics while delayed machine assertions are pending.
        maid.setNoAi(true);
        BlockPos pos = helper.absolutePos(relativePos);
        maid.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
        check(helper.getLevel().addFreshEntity(maid), "Could not spawn maid for regression test");
        return maid;
    }

    private static <T extends LivingEntity> T spawnMob(
            GameTestHelper helper, EntityType<T> type, BlockPos relativePos) {
        T entity = type.create(helper.getLevel());
        check(entity != null, "Could not construct " + type.getDescriptionId());
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        BlockPos pos = helper.absolutePos(relativePos);
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0f, 0f);
        check(helper.getLevel().addFreshEntity(entity),
                "Could not spawn " + type.getDescriptionId() + " for regression test");
        return entity;
    }

    private static void makeAdult(TFCAnimalProperties animal) {
        animal.setBirthTick(animal.calendar().getTicks()
                - ((long) animal.getDaysToAdulthood() + 1L) * ICalendar.PLAYER_TICKS_IN_DEFAULT_DAY);
        animal.setOldTick(-1L);
        check(animal.getAgeType() == Age.ADULT, "Could not configure an adult TFC animal fixture");
    }

    private static void prepareBreedingAnimal(TFCAnimalProperties animal, Gender gender) {
        makeAdult(animal);
        animal.setGender(gender);
        animal.setFertilized(false);
        animal.setFamiliarity(TFCAnimalProperties.READY_TO_MATE_FAMILIARITY - 0.01f);
        long now = animal.calendar().getTicks();
        animal.getEntityData().set(animal.animalData().lastFedTick(),
                now - ICalendar.CALENDAR_TICKS_IN_DAY - 1);
        animal.getEntityData().set(animal.animalData().lastMateTick(),
                now - TFCAnimalProperties.MATING_COOLDOWN_DEFAULT_TICKS - 1);
        check(animal.isHungry(), "Could not configure a hungry TFC animal fixture");
    }

    private static void setVisibleAnimals(EntityMaid maid, LivingEntity... animals) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getBrain().setMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                new NearestVisibleLivingEntities(maid, List.of(animals)));
    }

    private static IItemHandler bindWirelessChest(
            GameTestHelper helper, EntityMaid maid, BlockPos relativePos) {
        BlockPos chestPos = helper.absolutePos(relativePos);
        helper.getLevel().setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        IItemHandler chest = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, chestPos, null);
        check(chest != null, "Bound chest did not expose the NeoForge item capability");

        ItemStack wireless = InitItems.WIRELESS_IO.get().getDefaultInstance();
        ItemWirelessIO.setBindingPos(wireless, chestPos);
        ItemWirelessIO.setMode(wireless, false);
        ItemWirelessIO.setFilterMode(wireless, true);
        ItemWirelessIO.setSlotConfig(wireless,
                java.util.Collections.nCopies(maid.getMaidInv().getSlots() + 2, false));
        check(maid.getMaidBauble().insertItem(0, wireless, false).isEmpty(),
                "Wireless I/O bauble could not be equipped");
        check(WirelessIOHelper.hasWirelessIOAndBound(maid),
                "Wireless I/O bauble did not resolve its bound chest");
        return chest;
    }

    private static <T> T requireBlockEntity(
            GameTestHelper helper, BlockPos absolutePos, Class<T> expectedType) {
        Object blockEntity = helper.getLevel().getBlockEntity(absolutePos);
        check(expectedType.isInstance(blockEntity),
                "Expected " + expectedType.getSimpleName() + " at " + absolutePos);
        return expectedType.cast(blockEntity);
    }

    private static ItemStack seed(Crop crop) {
        return TFCItems.CROP_SEEDS.get(crop).get().getDefaultInstance();
    }

    private static ItemStackHandler filter(ItemStack... stacks) {
        ItemStackHandler handler = new ItemStackHandler(9);
        for (int i = 0; i < stacks.length; i++) {
            handler.setStackInSlot(i, stacks[i].copyWithCount(1));
        }
        return handler;
    }

    private static ItemStack filledWaterJug() {
        ItemStack jug = TFCItems.JUG.get().getDefaultInstance();
        IFluidHandlerItem handler = jug.getCapability(Capabilities.FluidHandler.ITEM);
        check(handler != null, "TFC jug did not expose an item fluid capability");
        int filled = handler.fill(new FluidStack(Fluids.WATER, 100), IFluidHandler.FluidAction.EXECUTE);
        check(filled > 0, "TFC jug rejected water during test setup");
        return handler.getContainer().copy();
    }

    private static boolean containsExact(IItemHandler handler, ItemStack expected) {
        return countExact(handler, expected) == expected.getCount();
    }

    private static int countExact(IItemHandler handler, ItemStack expected) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack actual = handler.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(actual, expected)) {
                count += actual.getCount();
            }
        }
        return count;
    }

    private static int countItem(IItemHandler handler, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int countWorldItem(GameTestHelper helper, net.minecraft.world.item.Item item) {
        return helper.getEntities(EntityType.ITEM).stream()
                .map(ItemEntity::getItem)
                .filter(stack -> stack.is(item))
                .mapToInt(ItemStack::getCount)
                .sum();
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class ReachableTestMaid extends EntityMaid {
        private ReachableTestMaid(net.minecraft.world.level.Level level) {
            super(level);
        }

        @Override
        public boolean canPathReach(Entity entity) {
            return true;
        }
    }

    private static final class TestHealMealTask extends MaidHealSelfTask {
        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestWorkMealTask extends MaidWorkMealTask {
        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestShearTask extends MaidShearTask {
        private TestShearTask() {
            super(0.6f);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestPluckFeatherTask extends MaidPluckFeatherTask {
        private TestPluckFeatherTask() {
            super(0.6f);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestMilkTask extends MaidMilkTask {
        private TestMilkTask() {
            super(0.6f);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestFeedTask extends MaidFeedTask {
        private TestFeedTask() {
            super(0.6f);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestKillOldTask extends MaidKillOldTask {
        private TestKillOldTask() {
            super(0.6f);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            start(level, maid, level.getGameTime());
        }
    }

    private static final class TestQuernWorkTask extends MaidQuernWorkTask {
        private TestQuernWorkTask() {
            super(4);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid, QuernBlockEntity quern) {
            tickWork(level, maid, level.getGameTime(), quern);
        }
    }

    private static final class TestLoomWorkTask extends MaidLoomWorkTask {
        private TestLoomWorkTask() {
            super(4);
        }

        private boolean begin(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            if (!checkExtraStartConditions(level, maid)) {
                return false;
            }
            start(level, maid, level.getGameTime());
            return true;
        }

        private void step(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            tick(level, maid, level.getGameTime());
        }
    }

    private static final class TestBellowsWorkTask extends MaidBellowsWorkTask {
        private TestBellowsWorkTask() {
            super(4);
        }

        private void run(net.minecraft.server.level.ServerLevel level, EntityMaid maid, BellowsBlockEntity bellows) {
            tickWork(level, maid, level.getGameTime(), bellows);
        }
    }

    private static final class TestPanningTask extends MaidPanningTask {
        private TestPanningTask() {
            super(0.6f);
        }

        private boolean begin(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            if (!checkExtraStartConditions(level, maid)) {
                return false;
            }
            start(level, maid, level.getGameTime());
            return true;
        }

        private void step(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            tick(level, maid, level.getGameTime());
        }

        private boolean canContinue(net.minecraft.server.level.ServerLevel level, EntityMaid maid) {
            return canStillUse(level, maid, level.getGameTime());
        }
    }

    private static final class BlockStateAssertions {
        private static void assertClimbingCrop(
                GameTestHelper helper, BlockPos cropPos, ItemStack consumedSeed) {
            check(consumedSeed.isEmpty(), "Planting tomato did not consume its seed");
            check(helper.getLevel().getBlockState(cropPos).is(TFCBlocks.CROPS.get(Crop.TOMATO).get()),
                    "Tomato seed did not place the TFC climbing crop");
            check(helper.getLevel().getBlockState(cropPos).getValue(ClimbingCropBlock.STICK),
                    "Tomato bottom block was not marked as supported");
            check(helper.getLevel().getBlockState(cropPos.above()).is(TFCBlocks.CROPS.get(Crop.TOMATO).get()),
                    "Tomato support did not place the upper crop block");
            check(helper.getLevel().getBlockState(cropPos.above()).getValue(ClimbingCropBlock.PART)
                            == ClimbingCropBlock.Part.TOP,
                    "Tomato upper crop block has the wrong part state");
        }
    }
}
