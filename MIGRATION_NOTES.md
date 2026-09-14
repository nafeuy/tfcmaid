# TFCMaid 1.21.1 NeoForge 迁移记录

## 最终结论

TFCMaid 已从 Minecraft 1.20.1 Forge 迁移至 Minecraft 1.21.1 NeoForge。模组能够完成构建，在开发客户端和专用服务器中加载，注册全部 18 个 TFC 女仆任务，正常重载数据，并通过自动化功能回归测试。

本文使用以下状态：

- `PASS`：已经编译，并验证了行为和结果。
- `PARTIAL`：已实现的路径可以工作，但仍有明确写出的验收细节位于 TFCMaid 控制范围之外，或尚未进行穷尽测试。
- `BLOCKED`：由于文中记录的原因，当前无法继续实现。
- `NOT TESTED`：尚未取得足够的验证证据。

TFCMaid 自身负责的迁移项目为 `PASS`。如果严格要求整个运行环境的专用服务器日志中完全没有 Dist 诊断，则该项为 `PARTIAL`：即使移除 TFCMaid，指定版本的 TFC/TLM 依赖组合仍会输出来自上游的 `RuntimeDistCleaner` 诊断。这些消息不会阻止服务器启动或重载，本文如实保留，没有隐藏。

## 范围与固定基线

| 项目 | 值 |
|---|---|
| 原始分支 | `master` |
| 原始 HEAD | `67feeec6d95da16169f151b29f8522ed1de09dbb` |
| 原始工作树状态 | 干净 |
| 迁移分支 | `port/1.21.1-neoforge` |
| 模组 ID | `tfcmaid` |
| 模组版本 | `1.1.0-neoforge+mc1.21.1` |
| Minecraft | `1.21.1` |
| Java | Oracle Java `21.0.11`；编译 release 为 `21` |
| NeoForge | `21.1.234` |
| Gradle Wrapper | `8.8` |
| ModDevGradle | `2.0.107` |
| Mapping | Minecraft `1.21.1` 对应的 Parchment `2024.11.17` |
| TerraFirmaCraft | `1.21.1-4.2.10`；CurseForge 文件 `8831715`；tag `v4.2.10`；commit `a45b81f9f22e2d9af79f5050bf0025697ea5b990` |
| Touhou Little Maid | Minecraft 1.21.1 NeoForge 版 `1.5.3`；CurseForge 文件 `8061852`；发布 commit `591ad55a222f0e7b0aff26b4553197127fe45a20` |
| Patchouli | `1.21.1-92-NEOFORGE` |
| 直接可选集成 | 无；TFCMaid 没有调用 JEI/Jade API，因此不要求安装它们 |

以上固定的上游源码 revision 是本次 API 迁移的权威依据：

- TerraFirmaCraft `v4.2.10`：https://github.com/TerraFirmaCraft/TerraFirmaCraft/tree/v4.2.10
- Touhou Little Maid `1.5.3` 发布源码：https://github.com/TartaricAcid/TouhouLittleMaid
- NeoForge 文档：https://docs.neoforged.net/
- ModDevGradle：https://github.com/neoforged/ModDevGradle

## Phase 0 初始盘点与最终复审

原始基线包含 55 个 Java 文件和 59 个资源文件，`src/generated/resources` 中没有生成内容。仓库还错误跟踪了 `bin/main` 下的 59 个 IDE 派生产物，其中包括旧 Forge 元数据和 1.20 时代的复数 datapack 路径。这些派生文件已经移除，`bin/` 也已加入忽略规则。

最终源码树包含 56 个 Java 文件；新增的一个文件是功能 GameTest 测试套件。59 个手写资源均已保留，并迁移到 1.21.1 目录结构。

### 初始搜索盘点

| 搜索目标 | 出现次数 | 涉及文件 | 初始风险 |
|---|---:|---:|---|
| `net.minecraftforge` | 48 | 23 | 必须迁移 Loader/API |
| `ForgeRegistries` | 3 | 1 | 注册表初始化 |
| `EventBusSubscriber` | 3 | 2 | 事件路由 |
| Forge 事件包 | 3 | 2 | 服务器与动物产物集成 |
| Capability 相关调用 | 27 | 9 | 物品栏、流体和隙间状态 |
| `CompoundTag` | 0 | 0 | 未发现自定义物品栈序列化器 |
| `ItemStack#getTag` | 1 | 1 | 必须改为组件感知的比较 |
| `getOrCreateTag` / `setTag` | 0 | 0 | 未发现直接写入 |
| `ResourceLocation` | 71 | 19 | 构造/工厂方法发生变化 |
| Registry API | 27 | 8 | Vanilla 与延迟注册 |
| TFCMaid 自有网络代码 | 0 | 0 | 不需要迁移数据包 |
| `@Mixin` | 7 | 7 | 运行时关键 hook |
| `@Accessor` / `@Invoker` | 11 | 2 | 织机和石磨内部成员 |
| TFC import | 109 | 32 | 主要兼容面 |
| TLM import | 122 | 45 | 任务、Brain 和物品栏兼容面 |

### 最终源码复审

| 搜索目标 | 最终结果 |
|---|---|
| `net.minecraftforge` | 0 处 |
| `ForgeRegistries` | 0 处 |
| `CompoundTag` / `getTag` / `getOrCreateTag` / `setTag` | 0 处 |
| TFCMaid 自有 packet/channel/codec 代码 | 0 处；本项目不需要 |
| NeoForge 物品/流体 capability | 包含测试在内，共 10 个文件、71 处 API 引用 |
| `ResourceLocation` | 19 个文件、83 处引用，均使用 1.21.1 有效的工厂或解析方法 |
| Mixin | 6 个文件中的 6 个必需 Mixin |
| Accessor/Invoker | 织机 accessor 中保留 5 个 accessor；已移除过时的石磨 accessor |
| TFC import | 包含回归测试在内，共 34 个文件、159 处引用 |
| TLM import | 包含回归测试在内，共 46 个文件、136 处引用 |

### 源码/模块风险矩阵

风险等级沿用 `LOW`、`MEDIUM`、`HIGH`、`CRITICAL`。

| 文件/模块 | 功能 | Minecraft API | Forge/NeoForge API | TFC API | TLM API | Mixin | 风险 | 当前状态 |
|---|---|---|---|---|---|---|---|---|
| `build.gradle`、Wrapper、properties | 1.21.1 工具链和运行配置 | Java 21/mapping | NeoForge ModDevGradle | 精确依赖 | 精确依赖 | 内置处理器 | HIGH | PASS |
| `Tfcmaid` | 模组初始化与调试物品 | 物品注册、客户端 Dist | 事件总线、延迟注册 | 无 | 无 | 否 | MEDIUM | PASS |
| `TfcmaidExtension` | 注册 18 个任务和蛋糕行为 | Brain 行为类型 | 无 | 蛋糕/物品 | `ILittleMaid`、`TaskManager` | 否 | HIGH | PASS |
| `TfcmaidEvents` | 隙间过滤与腐烂食物处理 | 组件感知物品栈 | NeoForge 事件/物品处理器 | `FoodCapability` | 隙间事件 | 否 | CRITICAL | PASS |
| `config/*` | JSON 配置 | 路径/Gson | `FMLPaths` | 无 | 任务类名 | 否 | LOW | PASS |
| `item/FarmDebugWand` | 作物状态诊断 | 交互/方块实体 | 无 | 作物、日历、生长 | 无 | 否 | MEDIUM | PASS |
| 抽象行为基类 | 移动与工作生命周期 | Brain memory、寻路、Level | 无 | 类型化方块实体 | 女仆 Brain/实体 | 否 | CRITICAL | PASS |
| 牲畜行为 | 喂养、挤奶、拔毛和屠宰 | 实体、产物、物品生命周期 | 物品/流体 capability | 牲畜/产物 API | 女仆 Brain/物品栏 | 否 | CRITICAL | PASS |
| `MaidAttackJavelinTask` 与目标行为 | 远程战斗 | 投射物、伤害、耐久 | 无 | 石质/金属标枪 | 远程任务/Brain | 否 | CRITICAL | PASS |
| `MaidPanningTask` | 完整淘洗流程 | 动画、声音、战利品 | 无 | 淘金盘/沉积物组件 | 女仆物品栏 | 否 | HIGH | PASS |
| 织机行为 | 供料、织造、收取 | 配方/物品栏 | 物品处理器 | 织机配方/实体 | 女仆移动/物品栏 | accessor | CRITICAL | PASS |
| 石磨行为 | 手石、研磨、收取 | 配方/物品栏 | 物品处理器 | 公开石磨生命周期 | 女仆移动/物品栏 | 否 | CRITICAL | PASS |
| 风箱行为 | 定时操作 | 方块实体/Level | 无 | `onRightClick` | 女仆移动 | 否 | HIGH | PASS |
| `TaskTFCFarmBase` | 作物分类、气候、营养和施肥 | 方块、标签、物品栏 | 组合物品处理器 | 作物、耕地、肥料 | `IFarmTask` | 否 | CRITICAL | PASS |
| 四类作物任务 | 普通、水生、可采摘、蔓生作物 | 方块状态/掉落 | 物品处理器 | 作物实体和产量 | 农业行为 | 否 | CRITICAL | PASS |
| 浆果、拾荒、除草和干草任务 | 采集/收获 | 方块标签和掉落 | 物品处理器 | 精确 TFC 标签/方块 | 农业行为 | 否 | HIGH | PASS |
| 牲畜任务封装 | 工作模式和条件判断 | 物品/Brain | capability | 动物、工具、流体类型 | 任务 API | 否 | CRITICAL | PASS |
| 机器/特殊任务封装 | 工作模式与 Brain 组合 | Brain、Activity、Memory | 无 | 机器/工具 | 任务 API | 否 | CRITICAL | PASS |
| `MaidEquipmentHelper` | 事务式装备切换 | 物品栈组件 | `IItemHandler` | 无 | 女仆物品栏 | 否 | CRITICAL | PASS |
| `WirelessIOHelper` | 绑定箱子与过滤规则 | 方块位置/物品栈 | 方块 capability | 组件保存 | TLM 箱子/隙间 API | 否 | CRITICAL | PASS |
| `Utils` / `TfcCakeEdible` | 腐烂食物与蛋糕兼容 | 进食/方块状态 | 无 | 食物/蛋糕 | 进食/可食方块 API | 3 个 redirect | CRITICAL | PASS |
| `mixin/*` | 食物、口渴、任务过滤和织机状态 | 精确 JVM descriptor | 流体 capability | 食物/织机 | 任务内部实现 | 是 | CRITICAL | PASS |
| `assets/tfcmaid` | 语言与模型 | 1.21.1 资源格式 | 无 | 无 | 任务翻译键 | 否 | LOW | PASS |
| `data/touhou_little_maid` | 适配 TFC 的 TLM 配方/数据 | 1.21.1 数据 schema | 无 | 配方原料 | TLM serializer/ID | 否 | HIGH | PASS |
| 旧 `bin/main` 产物 | 重复的 IDE 资源输出 | 过时的 1.20 路径 | 旧 Forge 元数据 | 过时范围 | 过时数据 | 过时配置 | HIGH | PASS：已移除并忽略 |
| `META-INF/neoforge.mods.toml` | Loader 元数据与版本范围 | Loader 元数据 | NeoForge 依赖 | 精确 4.2.10 | 1.5.3 至 1.6 之前 | 声明 Mixin | HIGH | PASS |
| `TFCMaidGameTests` | 功能回归 | GameTest、世界、实体 | capability/事件 | 真实 TFC 对象 | 真实 TLM 对象 | 执行 Mixin 路径 | CRITICAL | PASS |

## 架构变更与 API 迁移账本

| 1.20.1 旧实现 | 已验证的 1.21.1 实现 | 影响范围 | 结果 |
|---|---|---|---|
| ForgeGradle/Mixingradle | ModDevGradle `2.0.107`，使用内置 Mixin 支持 | 构建 | PASS |
| Java 17 | Java 21 toolchain 与 `options.release = 21` | 构建 | PASS |
| Forge 47.x | NeoForge `21.1.234` | 全部 Loader 集成 | PASS |
| 旧 `mods.toml` | `META-INF/neoforge.mods.toml` | 元数据 | PASS |
| 宽泛依赖版本 | TFC 精确 `[4.2.10]`、TLM `[1.5.3,1.6)`、MC 精确 `[1.21.1]`、NeoForge `[21.1.234,)` | 元数据 | PASS |
| Forge `RegistryObject`/注册初始化 | NeoForge `DeferredRegister.Items` 与 `DeferredItem` | 调试物品 | PASS |
| Forge 事件 import | `net.neoforged.bus.api` 与 `net.neoforged.neoforge.event` | 初始化/隙间 | PASS |
| Forge capability | NeoForge `Capabilities.ItemHandler.BLOCK` 与 `Capabilities.FluidHandler.ITEM` | 隙间、挤奶、物品栏 | PASS |
| 原始 NBT 物品栈比较 | `ItemStack.isSameItemSameComponents` 与物品处理器插入语义 | 全部传输/机器 | PASS |
| 旧 `ResourceLocation` 构造方式 | `ResourceLocation.parse` 与注册表查询 | 任务 UID/图标 | PASS |
| TFC 3.x 食物 capability/NBT | TFC 4.2.10 `FoodCapability` 与 TFC 数据组件 | 进食、隙间、石磨 | PASS |
| 旧流体容器处理 | NeoForge `IFluidHandlerItem` 与 TFC `Drinkable` 查询 | 主人饮水/挤奶 | PASS |
| 直接复制和修改物品栈 | 先模拟、后提取/插入、失败时归还的事务流程 | 女仆、背包、箱子 | PASS |
| TFC 3.x 牲畜判断 | 4.2.10 `TFCAnimalProperties`、`Pluckable`、`DairyAnimal` 与 `AnimalProductEvent` | 牲畜任务 | PASS |
| 旧作物/营养 API | 4.2.10 `CropBlockEntity`、`FarmlandBlockEntity`、`Fertilizer` 与气候 API | 农业任务 | PASS |
| 淘金盘 NBT | `TFCComponents.DEPOSIT`/`ItemComponent` 与 TFC 淘洗生命周期 | 淘金筛矿 | PASS |
| 旧标枪构造方式 | 4.2.10 `JavelinItem` 投射物创建、发射和耐久 API | 远程任务 | PASS |
| 石磨私有计时器 accessor | 公开 `getInventory`、`isGrinding` 和 `startGrinding` 生命周期 | 石磨 | PASS；已移除 accessor |
| 对织机私有物品栏/配方的假设 | 公开物品栏/配方 getter，加 5 个范围严格受限的状态 accessor | 织机 | PASS |
| TLM 1.20 扩展/任务 API | TLM 1.5.3 `@LittleMaidExtension`、`ILittleMaid` 和任务/Brain API | 全部工作模式 | PASS |
| 旧隙间行为假设 | TLM 1.5.3 `ItemWirelessIO`、`ChestManager` 与可取消传输事件 | 隙间 I/O | PASS |
| 旧 datapack 复数目录/schema | 1.21.1 单数目录和 TLM 1.5.3 serializer | 资源 | PASS |
| TFCMaid refmap | 对已命名上游 target 使用 `remap = false`，不使用本地 refmap | Mixin | PASS |

本次迁移没有引入反射、`catch Throwable`、被改成可选的关键注入、占位任务或静默删除功能。

## Mixin 审计

所有 target 和 descriptor 均对照 TFC `v4.2.10` 与 TLM `1.5.3` 检查。Mixin 配置仍为 `required: true`，`defaultRequire: 1`；如果 target 或注入点不存在，会直接失败，不会静默跳过。

| Mixin | Target 类 | Target 方法/成员 | 注入点 | 用途 | 1.21.1 是否存在 | 是否仍需要 | 验证 |
|---|---|---|---|---|---|---|---|
| `LoomBlockEntityAccessor` | TFC `LoomBlockEntity` | `lastPushed` getter/setter；`needsProgressUpdate`、`needsRecipeUpdate`、`progress` setter | 字段 accessor | 推进与玩家操作相同的织机进度/同步状态 | 是 | 是；没有等价公开进度推进 hook | PASS：运行时应用并完成完整织造周期 |
| `MaidHealSelfTaskMixin` | TLM `MaidHealSelfTask` | `start(ServerLevel, EntityMaid, long)` | redirect `IMaidMeal.canMaidEat` 调用 | 拒绝腐烂的 TFC 食物 | 是 | 是；TLM 没有 TFC 腐烂 hook | PASS：使用新鲜/腐烂物品栈执行 redirect |
| `MaidHomeMealTaskMixin` | TLM `MaidHomeMealTask` | 相同的 `start` descriptor | 相同 redirect | 女仆在家时拒绝腐烂食物 | 是 | 是 | PASS：运行时应用并验证共享 guard |
| `MaidWorkMealTaskMixin` | TLM `MaidWorkMealTask` | 相同的 `start` descriptor | 相同 redirect | 女仆工作时拒绝腐烂食物 | 是 | 是 | PASS：使用新鲜/腐烂物品栈执行 redirect |
| `TaskFeedOwnerMixin` | TLM `TaskFeedOwner` | `isFood(ItemStack, Player)` 与 `getPriority(ItemStack, Player)` | 可取消的 HEAD inject | 识别 TFC 饮品并按口渴值确定优先级 | 是 | 是；没有公开口渴扩展点 | PASS：识别、优先级与实际饮用 |
| `TaskManagerMixin` | TLM `TaskManager` | `init()` HEAD/RETURN 与 `add(IMaidTask)` HEAD | 必需 inject；`add` 可取消 | 只在 TLM 初始化期间过滤不兼容内置任务 | 是 | 是；没有等价注册过滤器 | PASS：全部注入成功，所有自定义模式仍正常注册 |

原有的 `QuernBlockEntityAccessor` 已删除，因为 TFC 4.2.10 公开了所需的石磨物品栏和工作生命周期。TFCMaid 不再产生缺少 refmap 的警告。TFC、TLM 和 Patchouli 的开发构件会报告它们自己的 refmap 缺失警告；没有出现 `Mixin apply failed`、`InvalidInjectionException` 或属于 TFCMaid 的 `target not found`。

## 资源与元数据迁移

| 资源组 | 最终形式 | 验证 |
|---|---|---|
| TLM 祭坛配方 | 43 个文件，位于 `data/touhou_little_maid/recipe/altar_recipe`，使用 1.5.3 serializer | PASS：精确加载 43 个 |
| 其他 TLM 配方 | 6 个文件，位于单数 `recipe` 目录 | PASS：服务器完整重载 |
| 进度 | 单数 `advancement` 目录 | PASS：普通运行加载 1574 个进度 |
| 战利品表 | 单数 `loot_table` 目录 | PASS：成功加载并由游戏行为使用 |
| 物品标签 | 单数 `tags/item` 目录 | PASS |
| 语言文件 | `en_us.json` 与 `zh_cn.json` | PASS：客户端可见全部 18 个工作模式翻译键 |
| 物品模型 | 保留有效的 1.21.1 物品模型 | PASS：客户端资源加载 |
| `pack.mcmeta` | pack format `34` | PASS |
| 生成资源 | 不需要 provider 或生成输出 | PASS：`runData` 成功，生成文件数为 0 |
| 模组元数据 | `neoforge.mods.toml`，包含精确依赖范围和必需 Mixin 配置 | PASS |
| 打包许可证 | jar 的 `META-INF` 中包含 MIT 与完整第三方/EUPL 声明 | PASS |

普通服务器加载和 reload 会报告 7302 个配方、1574 个进度、43 个 TLM 祭坛配方，并通过 TFC datapack 自检。GameTest 按设计额外加入一个测试配方和一个测试进度，因此 GameTest 运行时显示 7303/1575。

## 女仆任务清单

GameTest 会断言全部 18 个任务的 UID、实现类、非空图标和 Brain 任务列表。客户端也已在女仆 GUI 的三页中逐一查看这 18 个模式。

| UID | 工作模式 | 主要行为 | 回归状态 |
|---|---|---|---|
| `tfcmaid:tfc_shears` | 剪毛 | 真实成年且达到熟悉度的 TFC 羊、产物状态和工具耐久 | PASS |
| `tfcmaid:tfc_feather` | 拔毛 | 真实 TFC 禽类、冷却、伤害和羽毛数量 | PASS |
| `tfcmaid:tfc_milk` | 挤奶 | 真实 TFC 乳畜、事件、冷却和流体容器 | PASS |
| `tfcmaid:tfc_feed` | 喂养 | 食物查找、饥饿/熟悉度与繁殖状态转换 | PASS |
| `tfcmaid:tfc_kill_old` | 屠夫 | 年龄选择、击杀与隙间存储路径 | PASS |
| `tfcmaid:tfc_normal_crop` | 普通/攀爬作物 | 播种、支架、施肥、成熟收获和产量 | PASS |
| `tfcmaid:tfc_water_crop` | 水生作物 | 水稻播种、收获和产量 | PASS |
| `tfcmaid:tfc_pickable_crop` | 可采摘作物 | 辣椒收获、产物和生长状态重置 | PASS |
| `tfcmaid:tfc_spreading_crop` | 蔓生作物 | 南瓜果实数据、瓜藤保留和收获 | PASS |
| `tfcmaid:tfc_berry_bush` | 浆果采集 | 黑莓生命周期和产物 | PASS |
| `tfcmaid:tfc_javelin_attack` | 标枪攻击 | 石质与锻铁投射物、速度和耐久 | PASS |
| `tfcmaid:tfc_panning` | 淘金筛矿 | 沉积物组件、水源、时长和空盘结果 | PASS |
| `tfcmaid:tfc_weed` | 除草 | TFC 植物/野生作物标签和掉落 | PASS |
| `tfcmaid:tfc_debris` | 拾荒 | TFC 树枝/松散石块标签和掉落 | PASS |
| `tfcmaid:tfc_loom` | 织布 | 原料分组、操作节奏、配方状态和产物 | PASS |
| `tfcmaid:tfc_quern` | 推磨 | 手石、输入、公开工作周期、食物产物和耐久 | PASS |
| `tfcmaid:tfc_bellows` | 拉风箱 | 公开推动生命周期和操作节奏 | PASS |
| `tfcmaid:tfc_hay` | 收集干草 | 短草/高草、镰刀规则和稻草掉落 | PASS |

## 功能回归矩阵

| 必测功能 | 状态 | 验证证据 |
|---|---|---|
| 女仆饮水/主人喂食兼容 | PASS | 识别 TFC 水壶；验证口渴优先级和实际饮用 |
| TFC 动物剪毛 | PASS | 真实 TFC 羊和 TLM 剪毛行为；精确检查羊毛和耐久 |
| 禽类拔毛 | PASS | 真实 TFC 鸡；15% 伤害、冷却和 1-3 根羽毛 |
| 挤奶 | PASS | 真实 TFC 牛；精确装入一个桶容量的奶并进入冷却 |
| 隙间挤奶容器 | PASS | 从真实绑定的 TLM 隙间箱子取得空容器 |
| TFC 动物繁殖 | PASS | 两只异性成年 TFC 牛达到繁殖条件；生成后代调用使雌性受孕 |
| 隙间寻找饲料 | PASS | 从绑定箱子取得并消耗两份 TFC 小麦谷物 |
| 屠夫 | PASS | 选择并击杀老年动物，同时保留普通成年动物 |
| 屠夫物品进入隙间 | PASS | 带组件物品完整移动到绑定箱子 |
| 普通作物 | PASS | TFC 小麦完成播种、成熟、收获并产出小麦 |
| TFC 作物 | PASS | 全程使用真实 TFC 作物方块和方块实体 |
| 作物架/支架 | PASS | 验证两格番茄支架与木棍路径 |
| 水生作物 | PASS | 水稻播种、生长、产量和收获结果 |
| 辣椒 | PASS | 可采摘辣椒产物和采后生长状态重置 |
| 瓜类/蔓生作物 | PASS | 南瓜方块实体果实数据正确掉落，瓜藤保留 |
| 土壤营养 | PASS | 读取并修改真实耕地营养 |
| 自动施肥 | PASS | 消耗肥料，氮增加预期的 0.2 |
| 浆果灌木 | PASS | 成熟黑莓灌木和收获产物 |
| 筛矿 | PASS | 完整 120 tick 流程、消耗沉积物、返回空盘；随机战利品值不固定 |
| 投矛 | PASS | 石质和金属标枪均生成精确 TFC 投射物并损耗武器 |
| 除草 | PASS | 实际破坏 TFC 植物/野生作物标签目标并检查结果 |
| 树枝 | PASS | 实际 TFC 树枝目标、标签和掉落 |
| 小石子 | PASS | 实际 TFC 松散石块目标、标签和掉落 |
| 织布 | PASS | 完整八步织造周期，输入输出保持组件 |
| 石磨 | PASS | 完整研磨周期和带食物组件的产物 |
| 风箱 | PASS | TFC 公开风箱推动生命周期 |
| 干草 | PASS | 真实 TFC 短草/高草、镰刀和稻草行为 |
| 女仆蛋糕 | PASS | 通过 TLM 可食方块集成完成 TFC 蛋糕进食周期 |
| 自定义配方 | PASS | 所有资源可解析；43 个祭坛配方和完整 datapack reload |
| 隙间黑名单 | PASS | 使用真实 TLM 事件断言允许和阻止的方向 |
| 隙间白名单 | PASS | 使用真实 TLM 事件断言允许和阻止的方向 |
| 隙间双向 I/O | PASS | 两种可取消事件方向及任务直接请求/存储路径 |
| ItemStack/Data Components 保存 | PASS | 食物腐烂、流体内容、温度、耐久和机器原料均保持精确一致 |

### 组件与事务检查

| 物品栈/状态 | 已验证的不变量 |
|---|---|
| 新鲜和腐烂的 TFC 食物 | 过滤和传输后保留腐烂/食物组件；腐烂食物不会传回女仆 |
| 装满的 TFC 水壶 | 从箱子提取并装备后保留流体内容 |
| 加热的金属锭 | 隙间插入及目标已满回滚后保留热量组件 |
| 损耗的标枪/工具 | 传输后保留耐久组件，只在预期使用时变化 |
| 织机原料 | 标签等价但组件不同的物品栈绝不合并 |
| 石磨输入/输出 | 无效输入会归还；有效面粉保留食物组件 |
| 已满的背包/箱子 | 不删除或复制源物品；先模拟，再提取，失败时恢复 |
| 受保护女仆槽位 | 槽位配置只应用于女仆槽位，不会错误套用到箱子索引 |
| 打开、未绑定或超出范围的箱子 | 拒绝直接隙间访问 |

## 已执行测试

| 命令/测试 | 结果 | 证据 |
|---|---|---|
| 基线 `git status` 与创建分支 | PASS | `master` 在 `67feeec` 时工作树干净；未重写历史 |
| 精确上游源码/API 审计 | PASS | 固定 TFC `v4.2.10` 和 TLM `1.5.3` commit |
| `compileJava --warning-mode all` | PASS | 使用 Java 21 编译；TFCMaid 没有 deprecated/removal 警告 |
| `./gradlew clean build --warning-mode all` | PASS | 干净构建并生成产物 |
| 解析全部资源 JSON | PASS | 所有手写 JSON 均成功解析 |
| 旧 Forge/NBT/反射源码扫描 | PASS | 没有旧 Forge import 或旧式物品栈 NBT 调用 |
| 跟踪构建产物扫描 | PASS | 已移除 `bin/main` 中的旧 Forge/资源副本；只保留源码中的 NeoForge 元数据 |
| `runData` | PASS | 干净完成；预期 provider 数量为 0 |
| `runClient` | PASS | 退出码 0；进入 NeoForge 菜单和真实 TFC 世界，生成女仆并打开 GUI |
| 客户端任务 UI 检查 | PASS | 三页中全部 18 个自定义模式可见 |
| 客户端 Mixin 检查 | PASS | 6 个 TFCMaid Mixin 全部应用，无致命 TFCMaid Mixin 错误 |
| `runServer` | PASS | 到达 `Done`，接受控制台输入并正常停止 |
| 专用服务器 `reload` | PASS | 7302 个配方、1574 个进度、43 个祭坛配方；TFC 自检通过 |
| `runGameTestServer` | PASS | 最终 21/21：20 个 TFCMaid 测试加 TLM 发布包自带的 1 个测试 |
| 仅依赖服务器基线 | PASS（诊断对照） | 移除 TFCMaid 后仍出现相同的 Button/ClientLevel Dist 消息 |

功能测试套件位于 `src/main/java/net/xdpp/tfcmaid/gametest/TFCMaidGameTests.java`。它使用 TLM 1.5.3 发布包中的精确 `touhou_little_maid:game_test` 结构。行为入口使用真实 TFC/TLM 对象执行。为了让测试结果稳定，一个范围严格受限的测试女仆子类只覆盖实体寻路可达性；生产环境的导航代码没有被替换。

## 专用服务器诊断

专用服务器可以正常启动、reload 并关闭。TFCMaid 自身不会在物理服务器上加载客户端 renderer 或 client package。指定依赖环境中仍有以下上游诊断：

- TLM 会针对 `net.minecraft.client.gui.components.Button` 触发一条 `RuntimeDistCleaner` 消息。
- TFC 会针对 `net.minecraft.client.multiplayer.ClientLevel` 触发两条 `RuntimeDistCleaner` 消息，随后立即说明这是物理服务器初始配方重载期间的预期情况。
- TFC、TLM 和 Patchouli 的开发构件会警告各自的 refmap 不存在。
- TLM 的开发 Mixin debug 输出会报告 Java class version 支持警告，但其 Mixin 能正常应用，上游 GameTest 也通过。

在移除 TFCMaid 后运行服务器，可以复现相同的 Dist/refmap 诊断。因此，本项目没有隐藏或降级这些消息，也没有把它们误归因于 TFCMaid。若要消除它们，必须修改固定的上游构件；这超出本次迁移范围，也会违反精确版本要求。

## 许可证与来源

- TFC `v4.2.10` 使用 EUPL-1.2。
- 本次迁移通过固定版本源码确认并调用公开 API，没有刻意加入逐字复制的 TFC 实现代码。
- 迁移前的 README 声明部分既有兼容代码源自 TFC。任何这类内容继续受原 EUPL-1.2 条款约束，不会被表述为 MIT 原创代码。
- `Third-party-License` 包含 TFC 来源/revision，以及从 TFC `v4.2.10` 复制的完整 EUPL-1.2 许可证文本。
- `LICENSE-MIT` 保持不变，适用于其所覆盖的内容。
- jar 将两份声明打包为 `META-INF/LICENSE-tfcmaid.txt` 和 `META-INF/THIRD-PARTY-LICENSES.txt`。
- 模组元数据使用 `MIT AND EUPL-1.2`，不会在分发产物中隐藏第三方许可义务。
- TLM 集成只使用 1.5.3 公开扩展、任务和物品栏 API；本次迁移没有复制 TLM 实现代码。

本节仅记录仓库来源，不构成法律意见。

## 完成标准

| 要求 | 状态 |
|---|---|
| Java 21 | PASS |
| Minecraft 1.21.1 | PASS |
| NeoForge 21.1.234 基线 | PASS |
| 干净构建 | PASS |
| 开发客户端启动 | PASS |
| 专用服务器启动 | PASS |
| TFC 4.2.10 加载 | PASS |
| TLM 1.5.3 NeoForge 加载 | PASS |
| TFCMaid 注册 | PASS |
| 没有致命 TFCMaid Mixin 错误 | PASS |
| 没有遗留旧 Forge API | PASS |
| datapack/资源加载 | PASS |
| 专用服务器 reload 成功 | PASS |
| 所有原有主要任务完成回归 | PASS |
| 隙间功能完成回归 | PASS |
| 关键物品栈/组件保持完整 | PASS |
| 迁移记录完成 | PASS |
| README 已更新 | PASS |
| 元数据版本范围正确 | PASS |
| 最终 jar 位于 `build/libs` | PASS |
| 所有依赖均无 Dist 诊断 | PARTIAL：移除 TFCMaid 后仍可复现上游消息 |

## 已知限制与后续边界

- 只支持本文记录的精确依赖组合；没有假定后续 TFC 4.2.x、TLM 1.5.x 或 NeoForge 版本兼容。
- 自动化行为测试固定了路径可达性。真实客户端 smoke test 验证了进入世界、生成女仆、GUI 和任务注册，但没有在任意复杂地形上执行数小时的自主寻路压力测试。
- TFC 的淘金战利品刻意使用随机结果。测试验证真实沉积物组件、水源要求、完整工作时长、战利品路径执行、沉积物消耗和空盘返回，而没有把某一种随机矿物写死。
- 没有添加或测试 JEI/Jade，因为 TFCMaid 不直接依赖其 API。
- 上述专用服务器上游诊断是唯一严格验收例外；没有任何 TFCMaid 功能被阻塞。

## 迁移提交记录

| Commit | 里程碑 |
|---|---|
| `03cff52` | 审计迁移范围 |
| `07778be` | 将构建迁移到 Minecraft 1.21.1 NeoForge |
| `217fde0` | 迁移核心和 TLM 任务集成 |
| `b28889f` | 迁移物品栏和隙间集成 |
| `3293298` | 迁移农业与施肥 |
| `55bb216` | 迁移牲畜与喂养 |
| `7535410` | 迁移工具与机器 |
| `add2422` | 迁移配方和 datapack 资源 |
| `84f6dfb` | 保留牲畜产物和物品栏状态 |
| `665b03d` | 验证 Mixin 和事件订阅 |
| `952f805` | 恢复精确采集标签和隙间直接请求 |
| `b3a089d` | 保留机器输入和状态 |
| `6fa8372` | 支持交互式专用服务器 smoke test |
| `f77004d` | 添加 NeoForge 功能回归测试 |
| `a6ff28d` | 打包许可证声明并移除旧 IDE 产物 |
| `dff915e` | 完成 README、许可证与迁移记录 |
| `f5c286c` | 在 README 声明项目由 AI 完成 |
