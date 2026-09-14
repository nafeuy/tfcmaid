# TFCMaid / 群峦女仆

> **这个项目由 AI 完成**
>
> 本仓库的代码、资源适配和文档均为 100% AI 生成，人工编写占比为 0%。维护者负责提出需求和决定项目方向，具体实现全部交给 AI。

TFCMaid 是 TerraFirmaCraft 与 Touhou Little Maid 的兼容附属模组。它为女仆提供 TFC 作物、牲畜、工具、机器与物品数据语义，并让 TLM 隙间在传输 TFC 食物、液体、温度和耐久物品时保持数据完整。

本项目已经正式迁移到 Minecraft 1.21.1 + NeoForge。详细的 API 决策、Mixin 审计、功能矩阵和实测证据见 [MIGRATION_NOTES.md](MIGRATION_NOTES.md)。

## 支持环境

| 组件 | 支持版本 |
|---|---|
| Minecraft | `1.21.1` |
| NeoForge | `21.1.234` 或同一 21.1 系列中满足元数据范围的版本 |
| Java | `21` |
| TerraFirmaCraft | 必须为 `1.21.1-4.2.10` |
| Touhou Little Maid | `1.5.3` NeoForge，且低于 1.6 |
| Patchouli | `1.21.1-92-NEOFORGE` |
| TFCMaid | `1.1.0-neoforge+mc1.21.1` |

TFCMaid 没有直接调用 JEI 或 Jade API，因此它们不是构建或运行本模组的核心依赖。

## 功能

### 女仆工作模式

| 工作模式 | TFC 兼容行为 |
|---|---|
| 剪毛 | 检查 TFC 动物年龄、熟悉度与产物冷却，生成正确毛料并损耗剪刀 |
| 拔毛 | 支持 TFC 禽类、拔毛冷却、动物伤害与羽毛产物 |
| 挤奶 | 支持 TFC 乳畜、产物事件、流体容器与冷却 |
| 喂养 | 使用 TFC 饲料、熟悉度、饥饿、性别和繁殖状态 |
| 屠夫 | 只选择符合年龄条件的动物，并将物品安全转移到隙间 |
| 普通种植 | TFC 普通/攀爬作物的播种、支架、施肥、成熟与收获 |
| 水生种植 | 水稻等水生作物 |
| 可采摘种植 | 辣椒等采摘后重置生长状态的作物 |
| 瓜类种植 | 南瓜等蔓生作物及其果实方块实体 |
| 浆果采集 | TFC 季节性浆果灌木 |
| 标枪攻击 | TFC 石质和金属标枪的投射物、耐久与拾取规则 |
| 淘金筛矿 | TFC 淘金盘、可筛沉积物、水源、工作时长与战利品流程 |
| 除草 | TFC 植物和野生作物标签 |
| 拾荒 | TFC 小树枝和松散石块标签 |
| 织布 | TFC 织机配方、原料、进度和成品 |
| 推磨 | TFC 手石、石磨输入、公开研磨生命周期和产物 |
| 拉风箱 | TFC 风箱公开操作生命周期 |
| 收集干草 | TFC 短草/高草、镰刀规则与稻草产物 |

### 其他兼容

- TLM 女仆可以按 TFC 蛋糕的方块状态正确取食。
- TLM 主人喂食任务会识别 TFC 可饮用流体，并按玩家口渴值设置优先级。
- 女仆的自愈、居家进食和工作进食不会选择已经腐烂的 TFC 食物。
- 资源包包含迁移到 1.21.1 格式的 TLM 配方、祭坛配方、进度、战利品表和物品标签。
- `farm_debug_wand` 保留为作物状态调试工具。

## 隙间行为

隙间仍由 TLM 1.5.3 的饰品、绑定位置、方向、过滤器和槽位设置控制。TFCMaid 在此基础上保证：

- 白名单模式只传输过滤器中列出的物品；黑名单模式传输未列出的物品。
- 箱子到女仆时会拒绝腐烂的 TFC 食物。
- 女仆到箱子时会优先清走腐烂食物，不因普通过滤规则把它留在背包。
- 受保护槽位只解释为女仆槽位，不会错误映射成箱子槽位。
- 挤奶和喂养任务可以从处于“箱子到女仆”方向的已绑定隙间即时取得容器或饲料。
- 屠夫模式可以把物品放入处于“女仆到箱子”方向的已绑定隙间。
- 箱子已打开、未绑定、超出女仆限制范围或目标已满时，操作安全失败，不删除或复制源物品。
- TFC 食物腐败、液体内容、温度、工具耐久和其他 Data Components 在传输中保持不变。

## 安装

1. 使用 Java 21 启动 Minecraft 1.21.1 NeoForge。
2. 安装上表中的 TFC、TLM 和 Patchouli 版本。
3. 将 TFCMaid jar 放入实例的 `mods` 目录。
4. 不要把 1.20.1 Forge 版本与本分支混用；存档升级前请保留备份。

元数据约束为 Minecraft `[1.21.1]`、NeoForge `[21.1.234,)`、TFC `[4.2.10]`、TLM `[1.5.3,1.6)`。

## 配置

首次启动后，配置位于 `config/tfcmaid`：

| 文件 | 用途 |
|---|---|
| `task_shield.json` | 屏蔽与 TFC 环境冲突的 TLM 内置任务 |
| `feed_config.json` | 动物数量上限与超限喂养比例 |
| `weed_config.json` | 除草任务的方块黑名单 |

配置 JSON 无效时会记录警告并重建默认文件。

## 构建与验证

仓库使用 Gradle Wrapper、Java 21 和 ModDevGradle。Windows PowerShell 示例：

```powershell
.\gradlew.bat clean build --warning-mode all
.\gradlew.bat runClient
.\gradlew.bat runServer
.\gradlew.bat runData
.\gradlew.bat runGameTestServer
```

Linux/macOS 使用相同任务，将 `.\gradlew.bat` 换成 `./gradlew`。构建产物位于：

```text
build/libs/tfcmaid-1.1.0-neoforge+mc1.21.1.jar
```

当前回归证据：

- `clean build` 通过。
- 开发客户端进入真实 TFC 世界，生成女仆并在 GUI 三页中检查全部 18 个模式。
- dedicated server 到达 `Done`，`reload` 成功并正常停止。
- 数据生成任务通过；本项目当前没有需要生成的 provider。
- 最终 GameTest 为 21/21：20 项 TFCMaid 行为测试，加上 TLM 发布包自带的 1 项测试。
- 全仓不再包含 `net.minecraftforge`、`ForgeRegistries` 或旧式 ItemStack NBT 访问。

## 已知上游诊断

使用指定的 TFC/TLM 开发依赖启动 dedicated server 时，上游会产生 `RuntimeDistCleaner` 的 Button/ClientLevel 诊断以及上游开发 refmap 警告。移除 TFCMaid 后的依赖基线能够复现同样消息；TFC 还明确记录其 ClientLevel 初始重载消息为预期行为。服务器仍能启动、重载并停止，且 TFCMaid 没有 Mixin apply failure 或服务端客户端类误加载。

这项上游日志清洁度限制及其他测试边界在 [MIGRATION_NOTES.md](MIGRATION_NOTES.md) 中按 `PARTIAL` 如实记录。

## 许可证与来源

- [LICENSE-MIT](LICENSE-MIT) 适用于其所覆盖的项目代码。
- TFC 4.2.10 使用 EUPL-1.2。旧版项目曾声明存在 TFC 派生兼容代码；这些部分继续受原许可证约束，不能被宣称为纯 MIT 原创。
- [Third-party-License](Third-party-License) 包含 TFC `v4.2.10` 的来源、固定 commit 和完整 EUPL-1.2 文本。
- 构建 jar 会把两份许可证分别打包为 `META-INF/LICENSE-tfcmaid.txt` 和 `META-INF/THIRD-PARTY-LICENSES.txt`。
- 本次 1.21.1 迁移通过阅读固定版本源码来调用公开 API，没有刻意新增逐字复制的 TFC 实现代码。
- TLM 兼容使用 1.5.3 的公开扩展、任务和物品栏 API。

模组元数据使用 `MIT AND EUPL-1.2` 来显示组合分发中的许可证义务。具体来源记录见迁移文档；本节不是法律意见。

## 反馈

请在 GitHub 项目 `XiaoDengPiaoPiao/tfcmaid` 的 Issues 中提交问题。报告运行问题时请附上 Minecraft、NeoForge、TFC、TLM、TFCMaid 的精确版本以及 `latest.log`。
