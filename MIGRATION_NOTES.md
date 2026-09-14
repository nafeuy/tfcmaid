# TFCMaid 1.21.1 NeoForge Migration Notes

## Final result

TFCMaid has been migrated from Minecraft 1.20.1 Forge to Minecraft 1.21.1 NeoForge. The mod builds, loads on the client and dedicated server, registers all 18 TFC-aware maid tasks, reloads its data, and passes the automated functional regression suite.

Status vocabulary used below:

- `PASS`: compiled and behavior/result were verified.
- `PARTIAL`: the implemented path works, but a stated acceptance detail remains outside TFCMaid or was not exhaustively exercised.
- `BLOCKED`: implementation cannot proceed for the documented reason.
- `NOT TESTED`: no adequate evidence was collected.

The TFCMaid-owned migration is `PASS`. The strict whole-runtime requirement of having no dedicated-server Dist diagnostics is `PARTIAL`: the exact TFC/TLM dependency set logs upstream `RuntimeDistCleaner` diagnostics even when TFCMaid is removed. Those messages do not stop startup or reload, but they are retained below instead of being hidden.

## Scope and immutable baselines

| Item | Value |
|---|---|
| Original branch | `master` |
| Original HEAD | `67feeec6d95da16169f151b29f8522ed1de09dbb` |
| Original worktree | clean |
| Migration branch | `port/1.21.1-neoforge` |
| Mod id | `tfcmaid` |
| Mod version | `1.1.0-neoforge+mc1.21.1` |
| Minecraft | `1.21.1` |
| Java | Oracle Java `21.0.11`; compilation release `21` |
| NeoForge | `21.1.234` |
| Gradle wrapper | `8.8` |
| ModDevGradle | `2.0.107` |
| Mapping | Parchment `2024.11.17` for Minecraft `1.21.1` |
| TerraFirmaCraft | `1.21.1-4.2.10`; CurseForge file `8831715`; tag `v4.2.10`; commit `a45b81f9f22e2d9af79f5050bf0025697ea5b990` |
| Touhou Little Maid | `1.5.3` NeoForge for Minecraft 1.21.1; CurseForge file `8061852`; release commit `591ad55a222f0e7b0aff26b4553197127fe45a20` |
| Patchouli | `1.21.1-92-NEOFORGE` |
| Direct optional integrations | none; JEI/Jade are not required because TFCMaid does not call their APIs |

The pinned upstream source revisions above were the authoritative API references. The relevant upstream references are:

- TerraFirmaCraft `v4.2.10`: https://github.com/TerraFirmaCraft/TerraFirmaCraft/tree/v4.2.10
- Touhou Little Maid `1.5.3` release source: https://github.com/TartaricAcid/TouhouLittleMaid
- NeoForge documentation: https://docs.neoforged.net/
- ModDevGradle: https://github.com/neoforged/ModDevGradle

## Phase 0 inventory and final re-audit

The original baseline contained 55 Java files and 59 resource files. There was no generated output in `src/generated/resources`. It also tracked 59 duplicated IDE outputs under `bin/main`, including old Forge metadata and 1.20-era plural datapack paths. Those derived files were removed and `bin/` is now ignored. The final source tree contains 56 Java files because the migration adds one functional GameTest suite; the 59 hand-authored resources are retained in 1.21.1 layouts.

### Initial search inventory

| Search target | Occurrences | Files | Initial risk |
|---|---:|---:|---|
| `net.minecraftforge` | 48 | 23 | loader/API migration required |
| `ForgeRegistries` | 3 | 1 | registry bootstrap |
| `EventBusSubscriber` | 3 | 2 | event routing |
| Forge event packages | 3 | 2 | server and animal-product integration |
| Capability-related usage | 27 | 9 | inventory, fluid and wireless state |
| `CompoundTag` | 0 | 0 | no custom stack serializer |
| `ItemStack#getTag` | 1 | 1 | component-aware equality required |
| `getOrCreateTag` / `setTag` | 0 | 0 | no direct writes |
| `ResourceLocation` | 71 | 19 | constructor/factory changes |
| Registry APIs | 27 | 8 | Vanilla/deferred registration |
| TFCMaid-owned networking | 0 | 0 | no packet migration required |
| `@Mixin` | 7 | 7 | runtime-critical hooks |
| `@Accessor` / `@Invoker` | 11 | 2 | loom/quern internals |
| TFC imports | 109 | 32 | primary compatibility surface |
| TLM imports | 122 | 45 | task/brain/inventory surface |

### Final source re-audit

| Search target | Final result |
|---|---|
| `net.minecraftforge` | 0 occurrences |
| `ForgeRegistries` | 0 occurrences |
| `CompoundTag` / `getTag` / `getOrCreateTag` / `setTag` | 0 occurrences |
| TFCMaid-owned packet/channel/codec code | 0 occurrences; none needed |
| NeoForge item/fluid capability surface | 71 matched API references across 10 files, including tests |
| `ResourceLocation` | 83 references across 19 files; all use valid 1.21.1 factories/parsing |
| Mixins | 6 required mixins across 6 files |
| Accessors/invokers | 5 accessors in the loom accessor; obsolete quern accessor removed |
| TFC imports | 159 references across 34 files, including regression tests |
| TLM imports | 136 references across 46 files, including regression tests |

### Source/module risk matrix

| File/module | Function | Minecraft API | Forge API | TFC API | TLM API | Mixin | Risk | Current status |
|---|---|---|---|---|---|---|---|---|
| `build.gradle`, wrapper, properties | 1.21.1 toolchain and runs | Java 21/mappings | NeoForge ModDevGradle | exact artifact | exact artifact | built-in processor | HIGH | PASS |
| `Tfcmaid` | bootstrap/debug item | item registration, client dist | event bus, deferred items | none | none | no | MEDIUM | PASS |
| `TfcmaidExtension` | registers 18 tasks and cake behavior | brain behavior types | none | cake/items | `ILittleMaid`, `TaskManager` | no | HIGH | PASS |
| `TfcmaidEvents` | wireless filters and rotten-food routing | component-aware stacks | NeoForge events/item handlers | `FoodCapability` | wireless events | no | CRITICAL | PASS |
| `config/*` | JSON configuration | paths/Gson | `FMLPaths` | none | task class names | no | LOW | PASS |
| `item/FarmDebugWand` | crop diagnostics | interaction/block entity | none | crop/calendar/growth | none | no | MEDIUM | PASS |
| abstract behavior bases | movement/work lifecycle | Brain memories, navigation, level | none | typed block entities | maid Brain/entity | no | CRITICAL | PASS |
| livestock behaviors | feed, milk, pluck and butcher | entity/product/item lifecycle | item/fluid capabilities | livestock/product APIs | maid Brain/inventory | no | CRITICAL | PASS |
| `MaidAttackJavelinTask` and target behavior | ranged combat | projectile, damage, durability | none | stone/metal javelins | ranged task/Brain | no | CRITICAL | PASS |
| `MaidPanningTask` | complete pan cycle | animation, sound, loot | none | pan/deposit component | maid inventory | no | HIGH | PASS |
| loom behaviors | supply, weave, collect | recipes/inventory | item handlers | loom recipe/entity | maid movement/inventory | accessor | CRITICAL | PASS |
| quern behaviors | handstone, grind, collect | recipes/inventory | item handlers | public quern lifecycle | maid movement/inventory | no | CRITICAL | PASS |
| bellows behaviors | timed operation | block entity/level | none | `onRightClick` | maid movement | no | HIGH | PASS |
| `TaskTFCFarmBase` | crop classification, climate, nutrients, fertilizer | blocks/tags/inventory | combined handlers | crop/farmland/fertilizer | `IFarmTask` | no | CRITICAL | PASS |
| four crop tasks | normal, water, pickable, spreading crops | block state/drops | item handlers | crop entities and yield | farm behaviors | no | CRITICAL | PASS |
| berry, debris, weed and hay tasks | gathering/harvest | block tags and drops | item handlers | exact TFC tags/blocks | farm behavior | no | HIGH | PASS |
| livestock task wrappers | task modes/predicates | items/Brain | capabilities | animal/tool/fluid types | task API | no | CRITICAL | PASS |
| machine/special task wrappers | modes and Brain composition | Brain/activity/memory | none | machines/tools | task API | no | CRITICAL | PASS |
| `MaidEquipmentHelper` | transactional equip | stack components | `IItemHandler` | none | maid inventory | no | CRITICAL | PASS |
| `WirelessIOHelper` | bound chest and filter rules | block positions/stacks | block capability | component preservation | TLM chest/wireless API | no | CRITICAL | PASS |
| `Utils` / `TfcCakeEdible` | rotten-food and cake compatibility | eating/block state | none | food/cake | meal/edible APIs | three redirects | CRITICAL | PASS |
| `mixin/*` | food, thirst, task filtering, loom state | exact JVM descriptors | fluid capability | food/loom | task internals | yes | CRITICAL | PASS |
| `assets/tfcmaid` | language/model | 1.21.1 resource format | none | none | task keys | no | LOW | PASS |
| `data/touhou_little_maid` | TFC-compatible TLM recipes/data | 1.21.1 data schemas | none | ingredients | TLM serializers/ids | no | HIGH | PASS |
| legacy `bin/main` outputs | duplicated IDE resource output | obsolete 1.20 paths | old Forge metadata | stale ranges | stale data | stale config | HIGH | PASS: removed and ignored |
| `META-INF/neoforge.mods.toml` | loader metadata/ranges | loader metadata | NeoForge dependencies | exact 4.2.10 | 1.5.3 to below 1.6 | declares mixin | HIGH | PASS |
| `TFCMaidGameTests` | functional regression | GameTest/world/entity | capabilities/events | real TFC objects | real TLM objects | exercises mixins | CRITICAL | PASS |

## Changed architecture and API migration ledger

| 1.20.1 implementation | Verified 1.21.1 implementation | Affected area | Result |
|---|---|---|---|
| ForgeGradle/Mixingradle | ModDevGradle `2.0.107` with built-in Mixin support | build | PASS |
| Java 17 | Java 21 toolchain and `options.release = 21` | build | PASS |
| Forge 47.x | NeoForge `21.1.234` | all loader integration | PASS |
| old `mods.toml` | `META-INF/neoforge.mods.toml` | metadata | PASS |
| broad dependency versions | TFC exactly `[4.2.10]`, TLM `[1.5.3,1.6)`, MC exactly `[1.21.1]`, NeoForge `[21.1.234,)` | metadata | PASS |
| Forge `RegistryObject`/registry bootstrap | NeoForge `DeferredRegister.Items` and `DeferredItem` | debug item | PASS |
| Forge event imports | `net.neoforged.bus.api` and `net.neoforged.neoforge.event` | bootstrap/wireless | PASS |
| Forge capabilities | NeoForge `Capabilities.ItemHandler.BLOCK` and `Capabilities.FluidHandler.ITEM` | wireless, milk, inventory | PASS |
| raw NBT stack comparison | `ItemStack.isSameItemSameComponents` and handler insertion semantics | all transfers/machines | PASS |
| legacy `ResourceLocation` construction | `ResourceLocation.parse` and registry lookups | task UIDs/icons | PASS |
| TFC 3.x food capability/NBT | TFC 4.2.10 `FoodCapability` and TFC data components | meals/wireless/quern | PASS |
| old fluid-item handling | NeoForge `IFluidHandlerItem` plus TFC `Drinkable` lookup | owner drink/milking | PASS |
| copied stack mutation | simulate/extract/insert transactions with restitution | maid/backpack/chest | PASS |
| TFC 3.x livestock checks | 4.2.10 `TFCAnimalProperties`, `Pluckable`, `DairyAnimal` and `AnimalProductEvent` | livestock tasks | PASS |
| old crop/nutrient APIs | 4.2.10 `CropBlockEntity`, `FarmlandBlockEntity`, `Fertilizer` and climate APIs | farm tasks | PASS |
| pan NBT | `TFCComponents.DEPOSIT`/`ItemComponent` and TFC pan lifecycle | panning | PASS |
| old javelin construction | 4.2.10 `JavelinItem` projectile creation, launch and durability APIs | ranged task | PASS |
| quern private timer accessor | public `getInventory`, `isGrinding` and `startGrinding` lifecycle | quern | PASS; accessor removed |
| loom private inventory/recipe assumptions | public inventory/recipe getters plus five narrowly scoped state accessors | loom | PASS |
| TLM 1.20 extension/task APIs | TLM 1.5.3 `@LittleMaidExtension`, `ILittleMaid`, task/Brain APIs | all modes | PASS |
| old wireless assumptions | TLM 1.5.3 `ItemWirelessIO`, `ChestManager` and cancellable transfer events | wireless I/O | PASS |
| old datapack plural directories/schema | 1.21.1 singular directories and 1.5.3 serializers | resources | PASS |
| TFCMaid refmap | exact named external targets with `remap = false`; no local refmap | mixins | PASS |

No reflection, `catch Throwable`, optionalized critical injection, placeholder task, or silent feature removal was introduced.

## Mixin audit

The targets and descriptors were checked against TFC `v4.2.10` and TLM `1.5.3`. The mixin configuration remains `required: true` with `defaultRequire: 1`; a missing target or injection is therefore a hard failure.

| Mixin | Target class | Target method/member | Injection point | Purpose | Exists in 1.21.1 | Still required | Validation |
|---|---|---|---|---|---|---|---|
| `LoomBlockEntityAccessor` | TFC `LoomBlockEntity` | `lastPushed` getter/setter; setters for `needsProgressUpdate`, `needsRecipeUpdate`, `progress` | field accessor | drive the same loom progress/sync state a player action uses | yes | yes; no equivalent public progress push hook exists | PASS: runtime apply and full loom cycle |
| `MaidHealSelfTaskMixin` | TLM `MaidHealSelfTask` | `start(ServerLevel, EntityMaid, long)` | redirect invocation of `IMaidMeal.canMaidEat` | reject rotten TFC food | yes | yes; TLM has no TFC rot hook | PASS: applied redirect with fresh/rotten stacks |
| `MaidHomeMealTaskMixin` | TLM `MaidHomeMealTask` | same `start` descriptor | same redirect | reject rotten TFC food at home | yes | yes | PASS: runtime apply and shared guard |
| `MaidWorkMealTaskMixin` | TLM `MaidWorkMealTask` | same `start` descriptor | same redirect | reject rotten TFC food while working | yes | yes | PASS: applied redirect with fresh/rotten stacks |
| `TaskFeedOwnerMixin` | TLM `TaskFeedOwner` | `isFood(ItemStack, Player)` and `getPriority(ItemStack, Player)` | cancellable HEAD inject | recognize TFC drink fluids and prioritize thirst | yes | yes; no public thirst extension point exists | PASS: recognition, priority and actual consumption |
| `TaskManagerMixin` | TLM `TaskManager` | `init()` HEAD/RETURN and `add(IMaidTask)` HEAD | required injects; add is cancellable | filter incompatible built-in tasks only during TLM initialization | yes | yes; no equivalent registration filter exists | PASS: all injects apply and custom modes remain registered |

The former `QuernBlockEntityAccessor` was removed because TFC 4.2.10 exposes the necessary quern inventory and work lifecycle publicly. TFCMaid does not emit a missing-refmap warning. Development artifacts for TFC, TLM and Patchouli emit their own missing-refmap warnings; no `Mixin apply failed`, `InvalidInjectionException` or TFCMaid `target not found` occurred.

## Resource and metadata migration

| Resource group | Final form | Validation |
|---|---|---|
| TLM altar recipes | 43 files under `data/touhou_little_maid/recipe/altar_recipe` using the 1.5.3 serializer | PASS: exactly 43 loaded |
| Other TLM recipes | 6 files under singular `recipe` | PASS: complete server reload |
| Advancement | singular `advancement` | PASS: 1574 normal-run advancements loaded |
| Loot tables | singular `loot_table` | PASS: loaded; used by game behavior |
| Item tags | singular `tags/item` | PASS |
| Languages | `en_us.json` and `zh_cn.json` | PASS: all 18 mode keys visible in the client |
| Item model | 1.21.1 item model retained | PASS: client resource load |
| `pack.mcmeta` | pack format `34` | PASS |
| Generated resources | no providers/output are required | PASS: `runData` succeeds with zero generated files |
| Mod metadata | `neoforge.mods.toml` with exact dependency ranges and required Mixin config | PASS |
| Packaged notices | MIT and complete third-party/EUPL notices under jar `META-INF` | PASS |

A normal server load/reload reports 7302 recipes, 1574 advancements, 43 TLM altar recipes, and passing TFC datapack self-tests. GameTest adds one test recipe and one test advancement, so that run reports 7303/1575 by design.

## Maid task inventory

All 18 task UIDs, implementation classes, non-empty icons and Brain task lists are asserted by GameTest. All 18 modes were also visually inspected in the maid UI across three pages.

| UID | Work mode | Main behavior | Regression status |
|---|---|---|---|
| `tfcmaid:tfc_shears` | shearing | real adult/familiar TFC sheep, readiness, product and tool wear | PASS |
| `tfcmaid:tfc_feather` | feather plucking | real TFC poultry, cooldown, damage and feather count | PASS |
| `tfcmaid:tfc_milk` | milking | real TFC dairy animal, event, cooldown and fluid container | PASS |
| `tfcmaid:tfc_feed` | livestock feeding | food lookup, hunger/familiarity and breeding transition | PASS |
| `tfcmaid:tfc_kill_old` | butcher | age selection, kill and wireless storage path | PASS |
| `tfcmaid:tfc_normal_crop` | normal/climbing crops | plant, support, fertilize, mature harvest and yield | PASS |
| `tfcmaid:tfc_water_crop` | aquatic crops | water rice plant/harvest/yield | PASS |
| `tfcmaid:tfc_pickable_crop` | pickable crops | pepper harvest, output and growth reset | PASS |
| `tfcmaid:tfc_spreading_crop` | spreading crops | pumpkin fruit payload, stem retention and harvest | PASS |
| `tfcmaid:tfc_berry_bush` | berries | blackberry lifecycle/output | PASS |
| `tfcmaid:tfc_javelin_attack` | ranged javelin | stone and wrought-iron projectiles, velocity and durability | PASS |
| `tfcmaid:tfc_panning` | panning | deposit component, water, timing and empty-pan result | PASS |
| `tfcmaid:tfc_weed` | weeding | TFC plant/wild-crop tags and drops | PASS |
| `tfcmaid:tfc_debris` | debris gathering | TFC twig and loose-stone tags/drops | PASS |
| `tfcmaid:tfc_loom` | weaving | ingredient grouping, cadence, recipe state and output | PASS |
| `tfcmaid:tfc_quern` | grinding | handstone, input, public lifecycle, food output and durability | PASS |
| `tfcmaid:tfc_bellows` | bellows | public push lifecycle/cadence | PASS |
| `tfcmaid:tfc_hay` | hay gathering | short/tall grass, scythe rules and straw drops | PASS |

## Functional regression matrix

| Required feature | Status | Evidence |
|---|---|---|
| Maid drinking/owner feeding compatibility | PASS | TFC water jug recognized; thirst priority and actual consumption verified |
| TFC animal shearing | PASS | real TFC sheep and TLM shear behavior; exact wool and durability |
| Poultry feather plucking | PASS | real TFC chicken; 15% damage, cooldown, 1-3 feathers |
| Milking | PASS | real TFC cow; exactly one bucket-volume of milk and cooldown |
| Wireless milking container | PASS | empty container retrieved from a real bound TLM wireless chest |
| TFC animal breeding | PASS | two opposite-sex adult TFC cows become eligible; offspring call fertilizes female |
| Wireless feed lookup | PASS | two TFC wheat grains retrieved and consumed from bound chest |
| Butcher | PASS | old animal selected/killed while adult non-old animal remains |
| Butcher drops to wireless storage | PASS | component-bearing stack moved to bound chest without loss |
| Normal crops | PASS | TFC wheat planted, matured, harvested and produced wheat |
| TFC crops | PASS | real TFC crop blocks/entities used throughout |
| Crop supports/racks | PASS | two-block tomato support/stick path verified |
| Aquatic crops | PASS | water rice planting, growth, yield and harvest result |
| Pepper | PASS | pickable pepper output and post-harvest reset |
| Melon/spreading crops | PASS | pumpkin block-entity fruit payload emitted; stem retained |
| Soil nutrition | PASS | real farmland nutrient read/update |
| Automatic fertilizing | PASS | fertilizer consumed and nitrogen increased by the expected 0.2 |
| Berry bushes | PASS | ripe blackberry bush harvest/output |
| Panning | PASS | full 120-tick lifecycle, deposit consumed, empty pan returned; randomized loot value is intentionally not pinned |
| Javelin throwing | PASS | stone and metal variants spawn exact TFC projectile and damage weapon |
| Weeding | PASS | actual TFC plant/wild-crop tag targets broken with results |
| Twigs | PASS | actual TFC twig target/tag and drop |
| Loose stones | PASS | actual TFC loose-stone target/tag and drop |
| Loom | PASS | full eight-step weave cycle and component-safe input/output |
| Quern | PASS | full grind lifecycle and component-bearing food result |
| Bellows | PASS | TFC public bellows push lifecycle |
| Hay | PASS | actual short/tall TFC grass and scythe/straw behavior |
| Maid cake | PASS | TFC cake bite lifecycle through TLM edible-block integration |
| Custom recipes | PASS | all resources parse; 43 altar recipes and full datapack reload |
| Wireless blacklist | PASS | allowed/blocked directions asserted with real TLM events |
| Wireless whitelist | PASS | allowed/blocked directions asserted with real TLM events |
| Wireless bidirectional I/O | PASS | both cancellable event directions plus direct request/storage paths |
| ItemStack/data-component preservation | PASS | food rot, fluid contents, heat, damage and machine ingredients remain exact |

### Component and transaction checks

| Stack/state | Verified invariant |
|---|---|
| fresh and rotten TFC food | rot/food components survive filtering and transfer; rotten food never returns to maid |
| filled TFC jug | fluid contents survive chest extraction/equip |
| heated metal ingot | heat component survives wireless insertion and failed/full-destination rollback |
| damaged javelin/tool | damage component survives transfer and is changed only by intended use |
| loom ingredients | tag-equivalent but component-different stacks are never merged |
| quern input/output | invalid input is returned; valid flour retains its food component |
| full backpack/chest | no source deletion or duplication; operations simulate before extraction and restore on failure |
| protected maid slot | slot configuration is applied to maid slots, never misapplied to chest indices |
| open/unbound/out-of-range chest | direct wireless access is refused |

## Tests executed

| Command/test | Result | Evidence |
|---|---|---|
| baseline `git status` and branch creation | PASS | clean `master` at `67feeec`; no history rewrite |
| exact upstream source/API audit | PASS | TFC `v4.2.10` and TLM `1.5.3` pinned commits |
| `compileJava --warning-mode all` | PASS | Java 21 compile; no deprecation/removal warnings from TFCMaid |
| `./gradlew clean build --warning-mode all` | PASS | clean artifact build |
| JSON parse over all resource JSON | PASS | every hand-authored JSON parsed |
| legacy Forge/NBT/reflection source scans | PASS | no old Forge imports or legacy stack-NBT calls |
| tracked build-output scan | PASS | stale `bin/main` Forge/resources removed; only source NeoForge metadata remains |
| `runData` | PASS | clean completion; zero providers expected |
| `runClient` | PASS | exit 0; NeoForge menu, six mods, real TFC world, maid spawn and GUI |
| client task UI inspection | PASS | all 18 custom modes visible across three pages |
| client Mixin inspection | PASS | all six TFCMaid mixins applied; no fatal TFCMaid Mixin error |
| `runServer` | PASS | reached `Done`, accepted console input, stopped cleanly |
| dedicated `reload` | PASS | 7302 recipes, 1574 advancements, 43 altar recipes; TFC self-tests pass |
| `runGameTestServer` | PASS | 21/21 required tests in the final run: 20 TFCMaid tests plus TLM's published test |
| dependency-only server baseline | PASS as diagnostic control | the same Button/ClientLevel Dist messages occur without TFCMaid |

The functional suite is `src/main/java/net/xdpp/tfcmaid/gametest/TFCMaidGameTests.java`. It uses the exact `touhou_little_maid:game_test` structure published by TLM 1.5.3. Behavior entry points are exercised with real TFC/TLM objects. A narrow test-only maid subclass overrides only entity path reachability so the suite is deterministic; production navigation code is not replaced.

## Dedicated-server diagnostics

The dedicated server starts, reloads and shuts down normally. TFCMaid itself does not load a client renderer or client package on the physical server. The following diagnostics remain in the exact dependency environment:

- TLM triggers a `RuntimeDistCleaner` message for `net.minecraft.client.gui.components.Button`.
- TFC triggers two `RuntimeDistCleaner` messages for `net.minecraft.client.multiplayer.ClientLevel` and immediately logs that this is expected during its initial physical-server recipe reload.
- Development artifacts for TFC, TLM and Patchouli warn that their own refmaps are absent.
- TLM's development mixin debug output reports a Java-class-version capability warning, but its mixins apply and its upstream GameTest passes.

A server run with TFCMaid removed reproduced the same Dist/refmap diagnostics. They are therefore not masked, downgraded, or attributed to this project. Fixing them would require changing the pinned upstream artifacts, which is outside this migration and would violate the exact-version requirement.

## Licensing and provenance

- TFC `v4.2.10` is licensed under EUPL-1.2.
- The migration consulted its pinned source to identify and call public APIs; it did not intentionally introduce verbatim copies of TFC implementation code.
- The pre-migration README declared that some existing compatibility code was derived from TFC. Any such portion remains governed by its original EUPL-1.2 terms and is not represented as MIT-original work.
- `Third-party-License` now contains the TFC source/revision notice and the complete EUPL-1.2 text reproduced from TFC `v4.2.10`.
- `LICENSE-MIT` is retained unchanged for the material to which that license applies.
- The jar packages both notices as `META-INF/LICENSE-tfcmaid.txt` and `META-INF/THIRD-PARTY-LICENSES.txt`.
- Metadata reports `MIT AND EUPL-1.2` so packaged distributions do not hide the third-party obligation.
- TLM integration uses its 1.5.3 public extension/task/inventory APIs; no TLM implementation was copied during this migration.

This section records repository provenance and is not legal advice.

## Definition of Done

| Requirement | Status |
|---|---|
| Java 21 | PASS |
| Minecraft 1.21.1 | PASS |
| NeoForge 21.1.234 baseline | PASS |
| clean build | PASS |
| development client starts | PASS |
| dedicated server starts | PASS |
| TFC 4.2.10 loads | PASS |
| TLM 1.5.3 NeoForge loads | PASS |
| TFCMaid registers | PASS |
| no fatal TFCMaid Mixin error | PASS |
| no old Forge API remains | PASS |
| datapack/resources load | PASS |
| dedicated reload succeeds | PASS |
| all original major tasks regressions | PASS |
| wireless functions regressions | PASS |
| critical stack/components preserved | PASS |
| migration notes complete | PASS |
| README updated | PASS |
| metadata ranges correct | PASS |
| final jar produced under `build/libs` | PASS |
| zero Dist diagnostics across all dependencies | PARTIAL: upstream-only messages reproduced without TFCMaid |

## Known limitations and follow-up boundary

- The exact supported dependency set is the one recorded above. Later TFC 4.2.x/TLM 1.5.x/NeoForge builds were not assumed compatible.
- Automated behavior tests make reachability deterministic. The real client smoke test validates world entry, maid spawn, GUI and registration, but is not a many-hour autonomous-navigation soak test across arbitrary terrain.
- Panning loot is intentionally randomized by TFC. The test verifies the real deposit component, water requirement, complete duration, loot path execution, deposit consumption and returned empty pan rather than pinning one random ore result.
- JEI and Jade were not added or tested because TFCMaid has no direct API dependency on them.
- The upstream dedicated-server diagnostics described above remain the only strict acceptance exception. No TFCMaid feature is blocked.

## Migration commits

| Commit | Milestone |
|---|---|
| `03cff52` | audit migration surface |
| `07778be` | migrate build to Minecraft 1.21.1 NeoForge |
| `217fde0` | port core and TLM task integration |
| `b28889f` | port inventory and wireless integration |
| `3293298` | port farming and fertilizing |
| `55bb216` | port livestock and feeding |
| `7535410` | port tools and machines |
| `add2422` | migrate recipes and datapack resources |
| `84f6dfb` | preserve livestock products and inventory state |
| `665b03d` | validate mixins and event subscribers |
| `952f805` | restore exact gathering tags and wireless requests |
| `b3a089d` | preserve machine inputs and state |
| `6fa8372` | enable interactive dedicated-server smoke tests |
| `f77004d` | add functional NeoForge regression coverage |
| `a6ff28d` | package license notices and remove stale IDE outputs |
| documentation commit | finalize README, license notice and migration ledger |
