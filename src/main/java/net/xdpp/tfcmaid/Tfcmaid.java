package net.xdpp.tfcmaid;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.xdpp.tfcmaid.config.FeedConfigManager;
import net.xdpp.tfcmaid.config.TaskConfigManager;
import net.xdpp.tfcmaid.config.WeedConfigManager;
import net.xdpp.tfcmaid.item.FarmDebugWand;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Tfcmaid.MODID)
public class Tfcmaid {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "tfcmaid";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 注册调试工具物品
    public static final DeferredItem<Item> FARM_DEBUG_WAND = ITEMS.register("farm_debug_wand", () ->
            new FarmDebugWand(new Item.Properties().stacksTo(1)));

    public Tfcmaid(IEventBus modEventBus) {
        // 初始化任务配置管理器
        TaskConfigManager.initialize();
        // 初始化喂养配置管理器
        FeedConfigManager.initialize();
        // 初始化杂草配置管理器
        WeedConfigManager.initialize();
        // 注册延迟注册器
        ITEMS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    // SubscribeEvent 注解，让事件总线自动发现并调用方法
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // 在服务器启动时执行操作
        LOGGER.info("HELLO from server starting");
    }

    // 使用 EventBusSubscriber 自动注册类中所有使用 @SubscribeEvent 注解的静态方法
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // 客户端设置，在客户端初始化时执行
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("你好，群峦");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
