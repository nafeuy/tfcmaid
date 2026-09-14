# TFCMaid 1.21.1 NeoForge Migration Notes

## Scope and immutable baselines

- Migration branch: `port/1.21.1-neoforge`
- Original branch: `master`
- Original HEAD: `67feeec6d95da16169f151b29f8522ed1de09dbb`
- Original worktree state: clean
- Minecraft: `1.21.1`
- Java: `21`
- NeoForge: `21.1.234`
- ModDevGradle: `2.0.107`
- Parchment: `2024.11.17` for Minecraft `1.21.1`
- TerraFirmaCraft: `1.21.1-4.2.10`, tag `v4.2.10`, commit `a45b81f9f22e2d9af79f5050bf0025697ea5b990`
- Touhou Little Maid: `1.5.3-neoforge+mc1.21.1`, release commit `591ad55`

The TFC and TLM commits above are the authoritative API references for this port. TFC source is EUPL-1.2. This project reads that source to identify public APIs; no newly copied TFC implementation is to be represented as MIT-original code. Any direct source-derived implementation added later must be identified in this document and retain the required attribution/license treatment.

## Phase 0 inventory

The initial repository contains 55 Java files and 59 resource files. There is no `src/generated/resources` directory at the migration baseline.

| Search target | Occurrences | Files | Initial interpretation |
|---|---:|---:|---|
| `net.minecraftforge` | 48 | 23 | Must be eliminated or mapped to verified NeoForge APIs |
| `ForgeRegistries` | 3 | 1 | Core registry bootstrap |
| `EventBusSubscriber` | 3 | 2 | Main/game event subscribers |
| Forge event packages | 3 | 2 | Server and animal-product event handling |
| Capability-related usage | 27 | 9 | Item, fluid, chest, maid inventory, and wireless I/O |
| `CompoundTag` | 0 | 0 | No direct custom ItemStack NBT serializer found |
| `ItemStack#getTag` | 1 | 1 | Stack-comparison helper; must become component-aware |
| `getOrCreateTag` / `setTag` | 0 | 0 | No direct writes found |
| `ResourceLocation` | 71 | 19 | Constructor/factory migration and identity checks |
| Registry APIs | 27 | 8 | Vanilla and deferred registrations |
| Custom networking APIs | 0 | 0 | No TFCMaid-owned packets found |
| `@Mixin` | 7 | 7 | Runtime-critical compatibility hooks |
| `@Accessor` / `@Invoker` | 11 | 2 | Loom and quern internals |
| TFC imports | 109 | 32 | Primary upstream compatibility surface |
| TLM imports | 122 | 45 | Task/brain/inventory/extension compatibility surface |

## Source/module risk matrix

Status meanings: `AUDITED` means the 1.20.1 role and dependency surface are inventoried; it does not mean ported or tested. `PORTING`, `COMPILES`, and `TESTED` are only assigned after the corresponding evidence exists.

| File/module | Function | Minecraft API | Forge API | TFC API | TLM API | Mixin | Risk | Status |
|---|---|---|---|---|---|---|---|---|
| `Tfcmaid` | Mod bootstrap and debug item registration | registries, item | event bus, deferred register | none | none | no | MEDIUM | AUDITED |
| `TfcmaidExtension` | Registers all maid tasks and TFC cake edible behavior | brain behaviors | none | cake blocks indirectly | `ILittleMaid`, `TaskManager`, extension discovery | no | HIGH | AUDITED |
| `TfcmaidEvents` | Wireless-I/O filters and rotten-food routing | ItemStack equality/components | event bus, item handler | `FoodCapability` | wireless-I/O cancellable events | no | CRITICAL | AUDITED |
| `config/*` | JSON-backed task/feed/weed configuration | paths and JSON | `FMLPaths` | none | task class names | no | LOW | AUDITED |
| `item/FarmDebugWand` | Crop growth/debug inspection | interaction result, level/block entity | none | crop calendar/growth APIs | none | no | MEDIUM | AUDITED |
| `behavior/AbstractBlockEntityMoveTask` | Typed block-entity navigation | block entities | none | generic | maid move behavior | no | HIGH | AUDITED |
| `behavior/AbstractBlockEntityWorkTask` | Long-running machine work lifecycle | Brain memories, particles, level | none | generic | maid entity/brain memories | no | CRITICAL | AUDITED |
| `behavior/MaidLongRunningTask` | Base long-running brain behavior | Behavior lifecycle | none | none | maid entity | no | CRITICAL | AUDITED |
| `behavior/MaidFeedTask` | Locate and feed TFC livestock | entity/brain/inventory | item handler | food and animal properties | maid check-rate behavior | no | CRITICAL | AUDITED |
| `behavior/MaidMilkTask` | Milk animals into maid/wireless containers | entity interaction | fluid/item handlers, event bus | dairy animal, fluids, product event | maid inventory and brain | no | CRITICAL | AUDITED |
| `behavior/MaidPluckFeatherTask` | Pluck poultry with cooldown/product rules | entity interaction | none | `Pluckable`, animal properties | maid brain | no | HIGH | AUDITED |
| `behavior/MaidKillOldTask` | Select/attack old livestock and store drops | combat, entities, items | block/item capabilities | animal properties | wireless I/O, chest manager, maid brain | no | CRITICAL | AUDITED |
| `behavior/MaidAttackJavelinTask` | Execute ranged javelin attack | projectile/combat/durability | none | javelin item/projectile | maid entity | no | CRITICAL | AUDITED |
| `behavior/MaidJavelinTargetTask` | Acquire and maintain ranged targets | Brain memories/attributes | none | javelin item | maid entity/attributes | no | CRITICAL | AUDITED |
| `behavior/MaidPanningTask` | Run pan lifecycle and drops | use animation, particles, sounds | none | pannable data, pan components | maid inventory utilities | no | HIGH | AUDITED |
| `behavior/MaidLoomMoveTask` | Navigate to loom | block entities | none | loom | maid movement | no | HIGH | AUDITED |
| `behavior/MaidLoomWorkTask` | Insert yarn, progress loom, collect output | recipe/inventory/level | item handler | loom and recipe internals | maid inventory utilities | accessor | CRITICAL | AUDITED |
| `behavior/MaidQuernMoveTask` | Navigate to quern | block entities | none | quern | maid movement | no | HIGH | AUDITED |
| `behavior/MaidQuernWorkTask` | Insert input, drive quern, collect output | recipe/inventory/level | item handler | quern/recipe internals | maid inventory utilities | accessor | CRITICAL | AUDITED |
| `behavior/MaidBellowsMoveTask` | Navigate to bellows | block entities | none | bellows | maid movement base | no | HIGH | AUDITED |
| `behavior/MaidBellowsWorkTask` | Operate bellows at controlled cadence | block entity/level | none | bellows | maid work base | no | HIGH | AUDITED |
| `task/BaseTfcMaidTask` | Common task icon/sound/function-call integration | items/components, ResourceLocation | none | none | `IMaidTask` | no | HIGH | AUDITED |
| `task/TaskTFCFarmBase` | Shared crop search, climate, nutrition and fertilizer logic | blocks/tags/Brain/inventory | tags, combined handlers | crops, farmland, fertilizer/climate | `IFarmTask` | no | CRITICAL | AUDITED |
| `task/TaskTFCNormalCrop` | Standard and climbing TFC crops | block state/growth | tags, handlers | crop/farmland/nutrition | farm task | no | CRITICAL | AUDITED |
| `task/TaskTFCWaterCrop` | Aquatic crops and custom movement/planting | water/block state/Brain | combined handlers | crop/farmland/nutrition | farm move/plant behaviors | no | CRITICAL | AUDITED |
| `task/TaskTFCPickableCrop` | Pickable crops (including peppers) | block state/drops | combined handlers | pickable crop/farmland/nutrition | farm task/items utility | no | CRITICAL | AUDITED |
| `task/TaskTFCSpreadingCrop` | Spreading and vine/melon-like crops | block state/drops | combined handlers | spreading crop/farmland/nutrition | farm task | no | CRITICAL | AUDITED |
| `task/TaskTFCBerryBush` | Seasonal/spreading berry harvesting | block state/drops | none | lifecycle/fruit/food registry | farm task/items utility | no | HIGH | AUDITED |
| `task/TaskTFCDebris` | Gather ground sticks and loose rocks | block search/breaking/tags | none | datapack-defined blocks indirectly | farm task | no | MEDIUM | AUDITED |
| `task/TaskTFCWeed` | Remove configured weeds | block search/breaking | none | configured TFC plants indirectly | farm task | no | MEDIUM | AUDITED |
| `task/TaskTFCHay` | Cut short/tall grass into straw | tool actions/block drops | combined handlers | TFC grass/tags | farm task | no | HIGH | AUDITED |
| `task/TaskTFCShears` | Register TFC-aware shearing mode | tool actions/brain | tool actions | livestock behavior indirectly | shear task | no | HIGH | AUDITED |
| `task/TaskTFCFeather` | Register poultry-plucking mode | brain | none | behavior implementation | task/sound APIs | no | HIGH | AUDITED |
| `task/TaskTFCMilk` | Register milking mode and container predicate | fluids/items/brain | fluid capability | TFC fluids/containers | task/sound APIs | no | CRITICAL | AUDITED |
| `task/TaskTFCFeed` | Register livestock-feeding mode | brain/items | none | rotten food predicate | task/sound APIs | no | HIGH | AUDITED |
| `task/TaskTFCKillOld` | Register butcher mode and weapon predicate | combat/items/brain | tool actions | TFC metal knives | task/sound APIs | no | CRITICAL | AUDITED |
| `task/TaskTFCJavelinAttack` | Register ranged task and construct projectile behaviors | combat/projectile/brain | none | javelin variants/projectile | ranged task API/config/brain | no | CRITICAL | AUDITED |
| `task/TaskTFCPanning` | Register panning mode | items/brain | none | pans/pannable data | task/sound APIs | no | HIGH | AUDITED |
| `task/TaskTFCLoom` | Register loom mode and behaviors | Brain activity/memory | none | loom | task/arrival behaviors | no | CRITICAL | AUDITED |
| `task/TaskTFCQuern` | Register quern mode and behaviors | Brain activity/memory | none | quern | task/arrival behaviors | no | CRITICAL | AUDITED |
| `task/TaskTFCBellows` | Register bellows mode and behaviors | Brain activity/memory | none | bellows | task/arrival behaviors | no | CRITICAL | AUDITED |
| `util/WirelessIOHelper` | Bidirectional wireless chest lookup and item insertion | block position/ItemStack components | block/item capabilities | food/components indirectly | wireless I/O/chest APIs | no | CRITICAL | AUDITED |
| `util/MaidEquipmentHelper` | Search/extract/insert maid equipment | ItemStack component equality | item handler | none | maid inventory | no | CRITICAL | AUDITED |
| `util/TfcCakeEdible` | Make TFC cake edible by maids | block state/eating | none | cake blocks | edible-block API | no | HIGH | AUDITED |
| `util/Utils` | Prevent rotten food consumption | ItemStack food data | none | `FoodCapability`, `IFood` | meal API | no | HIGH | AUDITED |
| `mixin/*` | Meal, thirst, task filtering, loom/quern access | descriptors/Brain/items | fluid capability | food/machines | task internals | yes | CRITICAL | AUDITED |
| `resources/assets` | Language and debug-wand model | 1.21 model/resource format | none | none | none | no | LOW | AUDITED |
| `resources/data/touhou_little_maid` | Overrides TLM recipes, altar recipes, loot, advancement and tags | 1.21 datapack schemas and singular directories | none | TFC ingredients may appear | TLM serializers/IDs | no | HIGH | AUDITED |
| `META-INF/mods.toml` | Forge metadata | loader metadata | Forge-only metadata | dependency range | dependency range | declares mixin separately | MEDIUM | AUDITED |
| `tfcmaid.mixins.json` | Mixin registration/refmap | Java compatibility | Mixin service | target classes | target classes | yes | CRITICAL | AUDITED |

## Maid task inventory

| Task | Behavior restored by old implementation | Shared infrastructure | Risk | Port status | Runtime status |
|---|---|---|---|---|---|
| TFC shearing | Find and shear eligible livestock | maid inventory/tool predicate | HIGH | AUDITED | NOT TESTED |
| Feather plucking | Find eligible poultry, apply cooldown, emit product | entity targeting | HIGH | AUDITED | NOT TESTED |
| Milking | Select dairy animal, acquire/fill container, store output | maid/wireless inventory and fluid components | CRITICAL | AUDITED | NOT TESTED |
| Livestock feeding | Choose non-rotten food and breed/feed eligible animal | maid/wireless inventory | CRITICAL | AUDITED | NOT TESTED |
| Butcher old animals | Select eligible old livestock, attack, store drops | combat and wireless inventory | CRITICAL | AUDITED | NOT TESTED |
| Normal crops | Plant, fertilize, harvest normal/climbing crops | farm base and nutrition | CRITICAL | AUDITED | NOT TESTED |
| Water crops | Plant/fertilize/harvest aquatic crops | farm base and custom movement | CRITICAL | AUDITED | NOT TESTED |
| Pickable crops | Harvest multi-pick crops such as peppers | farm base and nutrition | CRITICAL | AUDITED | NOT TESTED |
| Spreading crops | Handle spreading/vine crops and produce | farm base and nutrition | CRITICAL | AUDITED | NOT TESTED |
| Berry bushes | Harvest seasonal fruit bushes | farm movement/inventory | HIGH | AUDITED | NOT TESTED |
| Javelin attack | Select and throw stone/metal javelins | ranged Brain/combat | CRITICAL | AUDITED | NOT TESTED |
| Panning | Pan valid gravel/sand and collect result | long-running behavior | HIGH | AUDITED | NOT TESTED |
| Weeding | Break configured weed blocks | farm movement | MEDIUM | AUDITED | NOT TESTED |
| Debris gathering | Gather sticks and loose rocks | farm movement | MEDIUM | AUDITED | NOT TESTED |
| Loom | Supply loom, weave, collect result | machine inventory/accessor | CRITICAL | AUDITED | NOT TESTED |
| Quern | Supply quern, grind, collect result | machine inventory/accessor | CRITICAL | AUDITED | NOT TESTED |
| Bellows | Find and operate bellows | machine movement/timing | HIGH | AUDITED | NOT TESTED |
| Hay | Cut valid grasses and collect straw | farm inventory/tool rules | HIGH | AUDITED | NOT TESTED |

## Mixin audit

The target-existence column is verified against TFC `v4.2.10` and TLM commit `591ad55`; descriptors and injection cardinality still require compile/runtime validation.

| Mixin | Target class | Target method/member and injection | Original purpose | 1.21.1 target exists | Still needed | Status |
|---|---|---|---|---|---|---|
| `LoomBlockEntityAccessor` | TFC `LoomBlockEntity` | Accessors for `lastPushed`, `needsProgressUpdate`, `needsRecipeUpdate`, `recipe`, `progress` | Drive weaving and synchronize machine state | Yes; names verified at TFC 4.2.10 | Pending public-hook review | AUDITED |
| `QuernBlockEntityAccessor` | TFC `QuernBlockEntity` | Accessor for private `recipeTimer` | Drive/observe manual grinding | Yes; name verified at TFC 4.2.10 | Pending public-hook review | AUDITED |
| `MaidHealSelfTaskMixin` | TLM `MaidHealSelfTask` | Redirect `IMaidMeal.canMaidEat` from `start` | Reject rotten TFC food | Class and calls exist; multiple calls now require cardinality review | Yes unless TLM meal manager exposes equivalent hook | AUDITED |
| `MaidHomeMealTaskMixin` | TLM `MaidHomeMealTask` | Redirect `IMaidMeal.canMaidEat` from `start` | Reject rotten TFC food | Class and calls exist; multiple calls now require cardinality review | Yes unless public hook exists | AUDITED |
| `MaidWorkMealTaskMixin` | TLM `MaidWorkMealTask` | Redirect `IMaidMeal.canMaidEat` from `start` | Reject rotten TFC food | Class and calls exist; multiple calls now require cardinality review | Yes unless public hook exists | AUDITED |
| `TaskFeedOwnerMixin` | TLM `TaskFeedOwner` | Inject `isFood` and `getPriority` at HEAD | Treat drinkable TFC fluids as owner feed and prioritize thirst | Methods/signatures exist | Yes; no TLM thirst extension point found in initial audit | AUDITED |
| `TaskManagerMixin` | TLM `TaskManager` | Inject `init` HEAD/RETURN and `add(IMaidTask)` HEAD | Filter incompatible built-in tasks from configuration | Methods/signatures exist | Pending safer registration-hook review | AUDITED |

Critical mixins remain `required`; they will not be converted to `require = 0` as a substitute for migration.

## API migration ledger

| Old API/behavior | Verified 1.21.1 direction | Affected modules | Status |
|---|---|---|---|
| ForgeGradle + Mixingradle | NeoForge ModDevGradle 2.0.107, built-in Mixin compilation | build | VERIFIED, NOT APPLIED |
| Java 17 | Java 21 toolchain/release | build | VERIFIED, NOT APPLIED |
| Forge 47.x | NeoForge 21.1.234 | build/runtime | VERIFIED, NOT APPLIED |
| Parchment `2023.06.26-1.20.1` | Parchment `2024.11.17` for 1.21.1 | build/mappings | VERIFIED, NOT APPLIED |
| `mods.toml` Forge dependency keys | `neoforge.mods.toml`, `type = "required"`, NeoForge dependency | metadata | VERIFIED, NOT APPLIED |
| `RegistryObject` | NeoForge `DeferredItem`/`DeferredHolder` as appropriate | bootstrap | PENDING |
| Forge item/fluid capability queries | NeoForge capability API and TFC 4.2.10 public component/helper APIs | inventory, milk, thirst, wireless I/O | PENDING |
| NBT-based stack equivalence | `ItemStack.isSameItemSameComponents` or handler insertion semantics | events/utilities | PENDING |
| `new ResourceLocation(namespace, path)` | `ResourceLocation.fromNamespaceAndPath` | task IDs/icons | PENDING |
| TFC 3.x food capability | TFC 4.2.10 food component API | food, events, task predicates | PENDING |
| TFC 3.x crop/nutrition APIs | TFC 4.2.10 crop/farmland APIs | farm tasks/debug wand | PENDING |
| TFC 3.x fluid item capability | NeoForge/TFC 4.2.10 fluid component API | milk/thirst | PENDING |
| TLM 1.20 task/Brain APIs | TLM 1.5.3 release source | all tasks/behaviors | PENDING |

## Resource audit

The baseline uses 1.20-era plural datapack paths: `recipes`, `advancements`, `loot_tables`, and `tags/items`. Minecraft 1.21.1 requires the singular registry/data directories where applicable (`recipe`, `advancement`, `loot_table`, `tags/item`). Every JSON also requires schema-level verification because item stack results and advancement item predicates changed with data components.

| Resource group | Baseline | Required work | Status |
|---|---:|---|---|
| TLM altar and crafting recipes | 43 files | Move to `recipe`, validate serializer/result schemas against TLM 1.5.3 | AUDITED |
| Advancements | 1 file | Move to `advancement`, replace legacy `nbt` item predicate with component-aware form | AUDITED |
| Loot tables | 2 files | Move to `loot_table`, validate random sequence and item stack functions | AUDITED |
| Item tags | 1 file | Move to `tags/item`, validate entries | AUDITED |
| Language files | 2 files | Validate every registered task/item key | AUDITED |
| Item model | 1 file | Validate 1.21.1 model format | AUDITED |
| `pack.mcmeta` | pack format 15 | Change to the 1.21.1 resource/data pack format and validate at runtime | AUDITED |

## Functional regression matrix

No item below is a pass until its behavior and resulting stacks/components have been observed in a running 1.21.1 environment.

| Feature | Compile | Client | Dedicated server | Behavior | Status |
|---|---|---|---|---|---|
| Maid drinking/owner feeding | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| TFC animal shearing | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Poultry feather plucking | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Milking and wireless container acquisition | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| TFC animal breeding and wireless feed lookup | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Butcher and wireless drop storage | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Normal/TFC/rack/water/pickable/spreading crops | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Soil nutrition and automatic fertilizing | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Berry bushes | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Panning | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Javelin attack | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Weed/stick/loose-rock gathering | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Loom/quern/bellows/hay | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| TFC cake | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Custom recipes and `/reload` | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Wireless blacklist/whitelist/two-way I/O | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |
| Component preservation: food/fluids/temperature/tools | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED | NOT TESTED |

## Known blockers and unresolved decisions

None at the end of Phase 0. Dependency artifacts and matching upstream source revisions are available. Runtime behavior remains unverified until the build/API port is complete.

## Test log

| Command/test | Result | Evidence/notes |
|---|---|---|
| Baseline `git status --short --branch` | PASS | Clean `master` at `67feeec`; migration branch created |
| Source/API inventory | PASS | 55 Java and 59 resource files scanned; counts recorded above |
| `clean build` | NOT RUN | Scheduled after Phase 1/2 skeleton migration |
| `runClient` | NOT RUN | Requires compiled port |
| `runServer` | NOT RUN | Requires compiled port |
| datagen | NOT RUN | Requires compiled port |
| `/reload` | NOT RUN | Requires running world/server |

