# AIO Fighter Plugin Agent Guidelines

## Plugin Overview
The AIO Fighter is a comprehensive combat automation plugin for Microbot. It handles NPC combat, looting, banking, prayer, potions, special attacks, slayer tasks, and safety features through a modular script architecture.

**Version**: 2.0.8  
**Authors**: LilBro, chsami, Mocrosoft, Styl3s (contributors)

---

## Project Structure

```
aiofighter/
├── AIOFighterPlugin.java      # Main plugin class, lifecycle & state management
├── AIOFighterConfig.java      # Configuration interface (~80 config options)
├── AIOFighterOverlay.java     # Main overlay rendering
├── AIOFighterInfoOverlay.java # Info panel overlay
├── bank/
│   └── BankerScript.java      # Banking, inventory setups, restocking
├── cannon/
│   └── CannonScript.java      # Dwarf cannon management
├── combat/
│   ├── AttackNpcScript.java   # Core NPC targeting & combat logic
│   ├── BuryScatterScript.java # Bone burying & ash scattering
│   ├── DodgeProjectileScript.java # Projectile avoidance
│   ├── FlickerScript.java     # Prayer flicking
│   ├── FoodScript.java        # Food consumption
│   ├── HighAlchScript.java    # High alchemy during combat
│   ├── PotionManagerScript.java # Potion management
│   ├── PrayerScript.java      # Prayer activation
│   ├── SafeSpot.java          # Safespot positioning
│   ├── SlayerScript.java      # Slayer task automation
│   └── UseSpecialAttackScript.java # Special attack handling
├── enums/
│   ├── AttackStyle.java       # MELEE, MAGE, RANGED
│   ├── AttackStyleMapper.java # Maps NPC attack styles
│   ├── DefaultLooterStyle.java # ITEM_LIST, GE_PRICE_RANGE, MIXED
│   ├── PlayStyle.java         # Timing profiles (EXTREME_AGGRESSIVE → PASSIVE)
│   ├── PrayerStyle.java       # Prayer automation styles
│   └── State.java             # Plugin state machine
├── loot/
│   └── LootScript.java        # Ground item collection
├── model/
│   └── InventorySetupUtil.java # Inventory setup utilities
├── safety/
│   └── SafetyScript.java      # Safety checks & emergency logout
├── shop/
│   └── ShopScript.java        # NPC shop interactions
└── skill/
    └── AttackStyleScript.java # Combat style management
```

---

## State Machine

The plugin operates on a finite state machine defined in `State.java`:

| State | Description |
|-------|-------------|
| `IDLE` | Waiting, no active task |
| `COMBAT` | Actively fighting an NPC |
| `WALKING` | Moving to a location |
| `BANKING` | Performing bank operations |
| `GETTING_TASK` | Obtaining slayer assignment |
| `DEATH` | Handling death event |
| `MISC` | Miscellaneous actions |
| `INITIALIZING` | Plugin startup |
| `UNKNOWN` | Fallback state |

**State Access Pattern:**
```java
// Get current state
State currentState = AIOFighterPlugin.getState();

// Set state
AIOFighterPlugin.setState(State.COMBAT);

// Check state before actions
if (config.state().equals(State.BANKING) || config.state().equals(State.WALKING)) {
    return; // Skip combat logic
}
```

---

## Core API Interactions

### Rs2Npc - NPC Interactions
```java
// Get attackable NPCs filtered by config
Rs2Npc.getAttackableNpcs(config.attackReachableNpcs())
    .filter(npc -> npc.getWorldLocation().distanceTo(config.centerLocation()) <= config.attackRadius())
    .filter(npc -> npcsToAttack.stream().anyMatch(npc.getName()::equalsIgnoreCase))
    .sorted(Comparator.comparingInt(npc -> Rs2Player.getRs2WorldPoint().distanceToPath(npc.getWorldLocation())))
    .collect(Collectors.toList());

// Attack an NPC
Rs2Npc.interact(npc, "attack");

// Get NPC by index
Rs2NpcModel cachedNpcModel = Rs2Npc.getNpcByIndex(cachedTargetNpcIndex);

// Check if NPC is dead
if (npc.isDead() || npc.getHealthRatio() == 0) { ... }

// Use item on NPC (slayer finishing items)
Rs2Inventory.useItemOnNpc(ItemID.SLAYER_ROCK_HAMMER, npc);
```

### Rs2Player - Player State
```java
// Check player state
Rs2Player.isInCombat();
Rs2Player.isInteracting();
Rs2Player.getWorldLocation();
Rs2Player.getHealthPercentage();
Rs2Player.waitForAnimation();

// Get current target
Actor currentInteracting = Rs2Player.getInteracting();
```

### Rs2Inventory - Inventory Management
```java
// Inventory checks
Rs2Inventory.isFull();
Rs2Inventory.emptySlotCount();
Rs2Inventory.getInventoryFood();

// Item operations
Rs2Inventory.useItemOnNpc(itemId, npc);
```

### Rs2Walker - Pathfinding
```java
// Walk to location
Rs2Walker.walkTo(worldPoint, distance);
Rs2Walker.setTarget(null); // Clear path

// Check distance
Rs2Player.getWorldLocation().distanceTo(targetPoint);
```

### Rs2Bank - Banking Operations
```java
// Bank interactions
Rs2Bank.openBank();
Rs2Bank.isOpen();
Rs2Bank.walkToBank();
Rs2Bank.depositAll();
Rs2Bank.close();

// Inventory setup loading
Rs2InventorySetup setup = new Rs2InventorySetup(inventorySetup, config.toggleFuzzy());
setup.loadInventory();
setup.loadEquipment();
```

### Rs2Prayer - Prayer System
```java
// Toggle protection prayers
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);

// Quick prayers
Rs2Prayer.toggleQuickPrayer(true);
```

### Rs2Combat - Combat Utilities
```java
// Combat state
Rs2Combat.inCombat();
```

### Rs2Camera - Camera Control
```java
// Focus on target
Rs2Camera.isTileOnScreen(npc.getLocalLocation());
Rs2Camera.turnTo(npc);
```

### Rs2Antiban - Anti-Ban System
```java
// Setup antiban for combat
Rs2Antiban.resetAntibanSettings();
Rs2Antiban.antibanSetupTemplates.applyCombatSetup();
Rs2Antiban.setActivityIntensity(ActivityIntensity.EXTREME);
Rs2Antiban.actionCooldown();
```

### Rs2LootEngine - Loot Collection
```java
// Build loot configuration
LootingParameters params = new LootingParameters(
    minPrice, maxPrice, radius, minQuantity, minSlots, 
    delayedLooting, onlyMyItems
);

Rs2LootEngine.with(params)
    .withLootAction(Rs2GroundItem::coreLoot)
    .addByValue()          // GE price filtering
    .addBones()            // Auto-bury bones
    .addAshes()            // Scatter ashes
    .addCoins()            // Loot coins
    .addArrows(minStack)   // Loot arrows
    .addRunes(minStack)    // Loot runes
    .addUntradables()      // Loot untradables
    .loot();               // Execute
```

### Rs2Slayer - Slayer System
```java
// Slayer task info
Rs2Slayer.hasSlayerTask();
Rs2Slayer.getSlayerTask();
Rs2Slayer.getSlayerTaskSize();
Rs2Slayer.getSlayerMonsters();
Rs2Slayer.getSlayerTaskLocation(clusterSize, preferBest);

// Task weakness handling
Rs2Slayer.hasSlayerTaskWeakness();
Rs2Slayer.getSlayerTaskWeaknessName();
Rs2Slayer.getSlayerTaskWeaknessThreshold();

// Walk to master
Rs2Slayer.walkToSlayerMaster(slayerMaster);
```

---

## Script Pattern

All scripts extend `Script` and use the scheduled executor pattern:

```java
public class ExampleScript extends Script {
    
    public void run(AIOFighterConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                // Guard clauses
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.toggleFeature()) return;
                
                // State checks
                if (config.state().equals(State.BANKING)) return;
                
                // Main logic here
                
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS); // Initial delay, period, unit
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
    }
}
```

**Typical tick intervals:**
- Combat scripts: 600ms (1 game tick)
- Loot scripts: 200ms (faster response)
- Slayer scripts: 1000ms (less frequent checks)

---

## Configuration Sections

The config is organized into logical groups:

| Section | Key Options |
|---------|-------------|
| **Combat** | `attackableNpcs`, `attackRadius`, `centerLocation`, `toggleCombat` |
| **Cannon** | `toggleCannon` |
| **SafeSpot** | `toggleSafeSpot`, `safeSpot` |
| **PlayStyle** | `playStyle` (timing), `dodgeProjectiles`, `returnToCenter` |
| **Food** | `toggleFood`, food type & threshold |
| **Loot** | `toggleLootItems`, `looterStyle`, `minPriceOfItemsToLoot`, `listOfItemsToLoot` |
| **Prayer** | `togglePrayer`, `toggleQuickPray`, `prayerStyle` |
| **Potions** | `togglePotion`, `togglePrayerPotion`, `toggleCombatPotion` |
| **Special Attack** | `useSpecialAttack`, `specWeapon` |
| **Alchemy** | `toggleHighAlch`, alchemy item list |
| **Magic** | `useMagic`, `magicSpell` |
| **Slayer** | `slayerMode`, `slayerMaster`, inventory setups |
| **Banking** | `bank`, `inventorySetup`, `toggleFuzzy`, `minFreeSlots` |
| **Safety** | `useSafety`, `missingFood`, `missingRunes`, `lowHealth` |

---

## Adding New Features

### 1. Create New Script
```java
package net.runelite.client.plugins.microbot.aiofighter.myfeature;

public class MyFeatureScript extends Script {
    public void run(AIOFighterConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;
                if (!config.toggleMyFeature()) return;
                
                // Your logic
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void shutdown() { super.shutdown(); }
}
```

### 2. Add Config Option
```java
// In AIOFighterConfig.java
@ConfigItem(
    keyName = "toggleMyFeature",
    name = "Enable My Feature",
    description = "Description of what this does",
    position = 50,
    section = combatSection
)
default boolean toggleMyFeature() { return false; }
```

### 3. Register in Plugin
```java
// In AIOFighterPlugin.java
@Inject
private MyFeatureScript myFeatureScript;

@Override
protected void startUp() {
    // ... existing code
    myFeatureScript.run(config);
}

@Override
protected void shutDown() {
    myFeatureScript.shutdown();
    // ... existing code
}
```

---

## Best Practices

### State Management
- Always check state before performing actions
- Use `setState()` to communicate between scripts
- Return early from script loops when in incompatible states

### Error Handling
```java
try {
    // Risky operation
} catch (Exception ex) {
    Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
}
```

### Logging
```java
Microbot.log("Descriptive message");
Microbot.log(Level.INFO, "With log level");
Microbot.status = "Current action..."; // UI status
```

### Script Coordination
```java
// Pause other scripts during critical operations
boolean prevPause = Microbot.pauseAllScripts.getAndSet(true);
try {
    // Critical operation
} finally {
    Microbot.pauseAllScripts.set(prevPause);
}
```

### Timing & Delays
```java
// Wait for condition with timeout
sleepUntil(() -> someCondition(), 5000);

// Use config-based play styles for human-like timing
PlayStyle style = config.playStyle();
int delay = style.getRandomTickInterval() * 600; // ms
```

---

## Common Patterns

### NPC Death Detection & Loot Wait
```java
if (cachedNpcModel.isDead() || cachedNpcModel.getHealthRatio() == 0) {
    AIOFighterPlugin.setWaitingForLoot(true);
    AIOFighterPlugin.setLastNpcKilledTime(System.currentTimeMillis());
    return;
}
```

### Distance-Based Filtering
```java
npcs.stream()
    .filter(npc -> npc.getWorldLocation().distanceTo(center) <= radius)
    .sorted(Comparator.comparingInt(npc -> 
        Rs2Player.getRs2WorldPoint().distanceToPath(npc.getWorldLocation())))
    .findFirst();
```

### Safe Walking
```java
if (!center.equals(new WorldPoint(0, 0, 0)) && distanceToCenter > 1) {
    Rs2Walker.walkTo(center, 0);
    AIOFighterPlugin.setState(State.WALKING);
}
```

---

## Testing

1. Set up the plugin in Microbot's debug runner
2. Configure NPCs and location
3. Monitor overlay for state transitions
4. Check logs for errors: `Microbot.logStackTrace()`

## Contributing

- Follow existing script patterns
- Add config options to appropriate sections
- Use existing Rs2 utilities instead of raw client calls
- Test state transitions thoroughly
- Document new features in overlay if user-visible
