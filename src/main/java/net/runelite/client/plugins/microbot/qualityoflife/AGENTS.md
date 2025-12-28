# Quality of Life Plugin Agent Guidelines

## Plugin Overview
The Quality of Life (QoL) plugin is a comprehensive automation enhancement plugin for Microbot. It provides various quality-of-life improvements including auto-eat, auto-prayer, potion management, "Do-Last" action memorization, camera tracking, dialogue automation, inventory setups, skill-specific helpers (Wintertodt, Fletching, Firemaking, Crafting), and UI customization.

**Version**: 1.8.8  
**Author**: See1Duck (and contributors)

---

## Project Structure

```
qualityoflife/
├── QoLPlugin.java              # Main plugin class, lifecycle, event handling & menu entries
├── QoLConfig.java              # Configuration interface (~60+ config options)
├── QoLScript.java              # Core script handling main QoL features
├── QoLOverlay.java             # Max hit overlay rendering
├── enums/
│   ├── CraftingItem.java       # Crafting item types (BODY, etc.)
│   ├── DragonhideCrafting.java # Dragonhide crafting definitions
│   ├── FiremakingLogs.java     # Log types for firemaking
│   ├── FletchingArrow.java     # Arrow fletching definitions
│   ├── FletchingBolt.java      # Bolt fletching definitions
│   ├── FletchingDarts.java     # Dart fletching definitions
│   ├── FletchingItem.java      # Fletching item types
│   ├── FletchingLogs.java      # Log-based fletching definitions
│   ├── FletchingMaterial.java  # Fletching material types
│   ├── FletchingMode.java      # Fletching mode enum
│   ├── UncutGems.java          # Gem cutting definitions
│   ├── WeaponAnimation.java    # Weapon animation to attack style mapping
│   ├── WeaponID.java           # Weapon IDs for prayer switching
│   └── WintertodtActions.java  # Wintertodt action state enum
├── managers/
│   ├── CraftingManager.java    # Handles quick-craft menu entries
│   ├── FiremakingManager.java  # Handles quick-firemake menu entries
│   ├── FletchingManager.java   # Handles quick-fletch menu entries
│   └── GemCuttingManager.java  # Handles quick gem cutting menu entries
├── scripts/
│   ├── AutoItemDropperScript.java  # Auto-drop items based on config
│   ├── AutoPrayer.java             # Anti-PK prayer automation
│   ├── AutoRunScript.java          # Auto-run energy management
│   ├── CameraScript.java           # Camera pitch/zoom fixes
│   ├── NeverLogoutScript.java      # Prevents idle logout
│   ├── PotionManagerScript.java    # Automatic potion consumption
│   ├── QolCannonScript.java        # Cannon refill/repair automation
│   ├── SpecialAttackScript.java    # Special attack automation
│   ├── bank/
│   │   └── BankpinScript.java      # Automatic bank PIN entry
│   ├── pvp/
│   │   └── PvpScript.java          # PvP-related features
│   └── wintertodt/
│       ├── WintertodtScript.java   # Wintertodt automation helpers
│       └── WintertodtOverlay.java  # Wintertodt progress overlay
```

---

## Key Features

### Do-Last Actions
The plugin can record and replay your last actions for:
- **Bank**: Records withdraw/deposit sequences, replays on "Do-Last" click
- **Furnace**: Records smelting/crafting sequences
- **Anvil**: Records smithing sequences
- **Cooking**: Quick-start cooking with spacebar

### Upkeep Automation
- **Auto Eat**: Eat food at configurable HP percentage
- **Auto Prayer Pot**: Drink prayer potions at configurable points
- **Potion Manager**: Comprehensive potion automation (anti-poison, antifire, combat pots, etc.)
- **Auto Run**: Toggle run automatically
- **Auto Stamina**: Use stamina potions at configurable threshold
- **Cannon Refill**: Automatically refill and repair dwarf cannon
- **Never Logout**: Prevent idle logout

### Combat Assistance
- **Auto Prayer vs Players**: Automatically switch protection prayers based on attacker's weapon/animation
- **Aggressive Anti-PK Mode**: Follow and swap prayers based on attacker's gear changes
- **Protect Item**: Optionally enable alongside protection prayers
- **Special Attack**: Auto-use special attack weapon

### Skill Helpers
- **Wintertodt**: Quick-fletch kindling, resume actions after interruption, fix/light brazier, heal pyromancer
- **Fletching**: Quick-fletch logs, arrows, bolts, darts, headless arrows
- **Firemaking**: Quick-firemake logs
- **Crafting**: Quick-cut gems, quick-craft dragonhide items
- **Runecrafting**: Smart runecraft (empty pouches during crafting)
- **Guardian of the Rift**: Smart workbench/mine (fill pouches before actions)
- **Magic**: Quick high-alch profitable items, quick teleport to house

### Dialogue Automation
- **Auto Continue**: Automatically click through dialogue
- **Quest Dialogue Options**: Auto-select quest dialogue options
- **Bank PIN**: Automatically enter bank PIN from profile

### Inventory & Equipment
- **Inventory Setups**: Load equipment/inventory setups from bank via menu option
- **Auto Drop**: Automatically drop specified items

### Camera & UI
- **Camera Tracking**: Right-click to track NPCs with smooth camera
- **Fix Camera Pitch/Zoom**: Restore camera on login
- **UI Customization**: Custom accent color, toggle button color, plugin label color

---

## Core API Interactions

### Rs2Player - Player State & Upkeep
```java
// Health-based eating
Rs2Player.eatAt(percent, true);

// Prayer management
Rs2Player.drinkPrayerPotionAt(points);
Rs2Player.drinkPrayerPotion();

// Combat potions
Rs2Player.drinkCombatPotionAt(Skill.STRENGTH);
Rs2Player.drinkCombatPotionAt(Skill.RANGED, false);
Rs2Player.drinkCombatPotionAt(Skill.MAGIC, false);

// Other potions
Rs2Player.drinkAntiPoisonPotion();
Rs2Player.drinkAntiFirePotion();
Rs2Player.drinkGoadingPotion();

// Wait for animations
Rs2Player.waitForAnimation();
Rs2Player.waitForXpDrop(Skill.MAGIC, 1000);
```

### Rs2Inventory - Item Management
```java
// Item checks
Rs2Inventory.hasItem(ItemID.GUARDIAN_ESSENCE);
Rs2Inventory.hasRunePouch();
Rs2Inventory.isSlotEmpty(slotIndex);
Rs2Inventory.slot(itemId);

// Item interactions
Rs2Inventory.interact(item, "Fill");
Rs2Inventory.dropAll(ItemID.VIAL_EMPTY);

// Pouch operations
Rs2Inventory.fillPouches();
Rs2Inventory.emptyPouches();
Rs2Inventory.anyPouchEmpty();
Rs2Inventory.anyPouchFull();

// Wait for changes
Rs2Inventory.waitForInventoryChanges(timeout);

// Stream access
Rs2Inventory.all().stream()
    .filter(item -> item.getName().contains("pouch"))
    .forEach(item -> Rs2Inventory.interact(item, "Fill"));
```

### Rs2Bank - Banking Operations
```java
// Bank state
Rs2Bank.isOpen();
Rs2Bank.isTabOpen(tabIndex);

// Bank navigation
Rs2Bank.openTab(tabIndex);
Rs2Bank.scrollBankToSlot(slotIndex);
Rs2Bank.getItemWidget(slotIndex);
Rs2Bank.getItemTabForBankItem(slotIndex);
```

### Rs2InventorySetup - Loadout Management
```java
// Load a named inventory setup
Rs2InventorySetup inventorySetup = new Rs2InventorySetup(setupName, scheduledFuture);

// Check current state
inventorySetup.doesEquipmentMatch();
inventorySetup.doesInventoryMatch();

// Apply setup
inventorySetup.loadEquipment();
inventorySetup.loadInventory();
inventorySetup.prePot();  // Use additional items
```

### Rs2Prayer - Prayer System
```java
// Toggle protection prayers
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_ITEM, true);

// Check prayer state
Rs2Prayer.isPrayerActive(Rs2PrayerEnum.PROTECT_MELEE);
```

### Rs2Dialogue - Dialogue Handling
```java
// Dialogue state
Rs2Dialogue.isInDialogue();

// Dialogue actions
Rs2Dialogue.clickContinue();
Rs2Dialogue.handleQuestOptionDialogueSelection();
```

### Rs2Widget - Widget Interactions
```java
// Production widgets
Rs2Widget.isProductionWidgetOpen();
Rs2Widget.isGoldCraftingWidgetOpen();
Rs2Widget.isSilverCraftingWidgetOpen();
Rs2Widget.isSmithingWidgetOpen();
```

### Rs2Magic - Magic Utilities
```java
// Spellbook checks
Rs2Magic.isSpellbook(Rs2Spellbook.MODERN);
Rs2Magic.hasRequiredRunes(Rs2Spells.TELEPORT_TO_HOUSE);

// Cast spells
Rs2Magic.cast(MagicAction.TELEPORT_TO_HOUSE);
```

### Rs2Camera - Camera Control
```java
// NPC tracking
Rs2Camera.trackNpc(npcId);
Rs2Camera.stopTrackingNpc();
Rs2Camera.isTrackingNpc();
```

### Rs2GameObject - Object Interactions
```java
// Interact with game objects
Rs2GameObject.interact(ObjectID.WORKBENCH_43754);
Rs2GameObject.interact("Altar");
```

---

## Script Pattern

All scripts extend `Script` and use the scheduled executor pattern:

```java
public class ExampleScript extends Script {
    
    public boolean run(QoLConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                // Guard clauses
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.toggleFeature()) return;
                
                // Main logic here
                
            } catch (Exception ex) {
                Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
                // Or use: log.error("Error message: {}", ex.getMessage(), ex);
            }
        }, 0, 300, TimeUnit.MILLISECONDS); // Initial delay, period, unit
        return true;
    }
    
    @Override
    public void shutdown() {
        super.shutdown();
        log.info("Script shutdown complete.");
    }
}
```

**Typical tick intervals:**
- QoL Script: 300ms (quick response)
- Potion Manager: 600ms (1 game tick)
- Auto Prayer: 300ms (fast prayer switching)

---

## Manager Pattern

Managers handle menu entry modifications for quick-action features. They are registered/unregistered with the EventBus:

```java
// In QoLPlugin.java startup
eventBus.register(fletchingManager);

// In QoLPlugin.java shutdown
eventBus.unregister(fletchingManager);
```

Manager implementation pattern:
```java
@Slf4j
public class ExampleManager {
    private final QoLConfig config;
    
    @Inject
    public ExampleManager(QoLConfig config) {
        this.config = config;
    }
    
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!config.quickFeature()) return;
        
        // Modify menu entry
        modifyMenuEntry(event, "Quick Action", "Target", this::onQuickActionClick);
    }
    
    private void modifyMenuEntry(MenuEntryAdded event, String newOption, 
                                  String newTarget, Consumer<MenuEntry> onClick) {
        event.getMenuEntry().setOption(newOption);
        event.getMenuEntry().onClick(onClick);
    }
    
    private void onQuickActionClick(MenuEntry event) {
        // Handle the quick action
    }
}
```

---

## Configuration Sections

The config is organized into logical groups:

| Section | Key Options |
|---------|-------------|
| **Overlay** | `renderMaxHitOverlay` |
| **Do-Last** | `useDoLastBank`, `useDoLastFurnace`, `useDoLastAnvil`, `useDoLastCooking` |
| **Camera** | `rightClickCameraTracking`, `smoothCameraTracking`, `fixCameraPitch`, `fixCameraZoom` |
| **Bank** | `useBankPin` |
| **Dialogue** | `useDialogueAutoContinue`, `useQuestDialogueOptions` |
| **Upkeep** | `enablePotionManager`, `autoEatFood`, `autoDrinkPrayerPot`, `neverLogout`, `useSpecWeapon`, `autoRun`, `autoStamina`, `refillCannon` |
| **Inventory** | `displayInventorySetups`, `Setup1-4`, `autoDrop`, `autoDropItems`, `excludeItems` |
| **UI** | `accentColor`, `toggleButtonColor`, `pluginLabelColor` |
| **Wintertodt** | `quickFletchKindling`, `resumeFletchingKindling`, `resumeFeedingBrazier`, `fixBrokenBrazier`, `lightUnlitBrazier`, `healPyromancer` |
| **GOTR** | `smartWorkbench`, `smartGotrMine` |
| **Fletching** | `quickFletchItems`, `fletchingItem`, `quickFletchDarts/Arrows/Bolts/HeadlessArrows` |
| **Firemaking** | `quickFiremakeLogs` |
| **Runecrafting** | `smartRunecraft` |
| **Magic** | `quickHighAlch`, `useQuickTeleportToHouse` |
| **Crafting** | `quickCutGems`, `quickCraftItems`, `craftingItem` |
| **Auto Prayer** | `autoPrayAgainstPlayers`, `enableProtectItemPrayer`, `aggressiveAntiPkMode` |
| **Grand Exchange** | `grandExchangeHotkey` |

---

## Adding New Features

### 1. Create New Script
```java
package net.runelite.client.plugins.microbot.qualityoflife.scripts;

@Slf4j
public class MyFeatureScript extends Script {
    public boolean run(QoLConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run()) return;
                if (!config.toggleMyFeature()) return;
                
                // Your logic
            } catch (Exception ex) {
                log.error("Error in MyFeatureScript: {}", ex.getMessage(), ex);
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }
    
    @Override
    public void shutdown() { 
        super.shutdown(); 
    }
}
```

### 2. Add Config Option
```java
// In QoLConfig.java
@ConfigItem(
    keyName = "toggleMyFeature",
    name = "Enable My Feature",
    description = "Description of what this does",
    position = 50,
    section = upkeepSection
)
default boolean toggleMyFeature() { return false; }
```

### 3. Register in Plugin
```java
// In QoLPlugin.java
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

### 4. For Menu Entry Modifications (Manager Pattern)
```java
// Create a manager class
package net.runelite.client.plugins.microbot.qualityoflife.managers;

public class MyFeatureManager {
    private final QoLConfig config;
    
    @Inject
    public MyFeatureManager(QoLConfig config) {
        this.config = config;
    }
    
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        // Modify menu entries
    }
}

// Register in QoLPlugin.java
@Inject
MyFeatureManager myFeatureManager;

// In startUp():
eventBus.register(myFeatureManager);

// In shutDown():
eventBus.unregister(myFeatureManager);
```

---

## Event Handling

The QoL plugin subscribes to various RuneLite events:

| Event | Purpose |
|-------|---------|
| `GameTick` | Never logout check, spec weapon updates |
| `GameStateChanged` | Camera fixes, menu entry resets |
| `ChatMessage` | Wintertodt message handling |
| `HitsplatApplied` | Wintertodt interruption detection |
| `MenuOptionClicked` | Action recording, smart menu handling |
| `MenuEntryAdded` | Add custom menu entries (Do-Last, Track, etc.) |
| `ConfigChanged` | Handle runtime config updates |
| `NpcChanged/Spawned/Despawned` | Wintertodt NPC tracking |
| `BeforeRender` | Smooth camera tracking |
| `ProfileChanged` | UI element updates |

---

## Best Practices

### Error Handling
```java
try {
    // Risky operation
} catch (Exception ex) {
    log.error("Error in {}: {}", this.getClass().getSimpleName(), ex.getMessage(), ex);
    // Or use Microbot helper:
    Microbot.logStackTrace(this.getClass().getSimpleName(), ex);
}
```

### Logging
```java
// Standard logging
log.info("Descriptive message");
log.warn("Warning message");
log.error("Error message: {}", ex.getMessage(), ex);

// Microbot status
Microbot.log("Game message");  // Shows in game chat
Microbot.log("<col=245C2D>Success message</col>");  // Green
Microbot.log("<col=5F1515>Error message</col>");    // Red
Microbot.log("<col=FFA500>Warning message</col>");  // Orange
```

### Async Execution
```java
// Run on separate thread (for blocking operations)
Microbot.getClientThread().runOnSeperateThread(() -> {
    // Blocking operations here
    Rs2Inventory.fillPouches();
    Rs2GameObject.interact(ObjectID.WORKBENCH);
    return null;
});
```

### Waiting & Sleeping
```java
// Wait for condition with timeout
sleepUntil(Rs2Bank::isOpen, 10000);
sleepUntil(() -> Rs2Widget.isProductionWidgetOpen(), 5000);

// Random delays
Rs2Random.wait(200, 500);

// Global sleep utilities
Global.sleep(Rs2Random.randomGaussian(150, 50));
Global.sleepUntilTrue(Rs2Widget::isProductionWidgetOpen);
```

### Menu Entry Creation
```java
// Create custom menu entry
int index = Microbot.getClient().getMenuEntries().length;
Microbot.getClient().createMenuEntry(index)
    .setOption("Custom Option")
    .setTarget(target)
    .setParam0(event.getActionParam0())
    .setParam1(event.getActionParam1())
    .setIdentifier(event.getIdentifier())
    .setType(event.getMenuEntry().getType())
    .onClick(this::customOnClickHandler);
```

### Invoke Menu Actions
```java
// Execute a cached menu entry
Microbot.doInvoke(menuEntry, rectangle);
Microbot.doInvoke(menuEntry, menuEntry.getWidget().getBounds());
```

---

## Testing

1. Enable the QoL plugin in Microbot
2. Configure desired features in the plugin settings
3. Monitor the game chat for status messages (green = success, red = error)
4. Check the RuneLite logs for detailed debug information
5. Use the overlay to verify features like max hit display

## Contributing

- Follow existing script/manager patterns
- Add config options to appropriate sections
- Use existing Rs2 utilities instead of raw client calls
- Register managers with EventBus for menu modifications
- Handle errors gracefully with proper logging
- Document new features in this AGENTS.md file
