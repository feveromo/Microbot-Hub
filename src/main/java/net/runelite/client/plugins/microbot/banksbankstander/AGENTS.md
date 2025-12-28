# Bank's BankStander Plugin Agent Guidelines

## Plugin Overview
Bank's BankStander is a bank-standing skilling automation plugin for Microbot. It automates item combining activities performed at banks, such as crafting, herblore, fletching, and cooking. The plugin handles banking, item withdrawal, item combining, and processing prompts with human-like timing.

**Version**: 2.1.1  
**Authors**: Bankjs, eXioStorm (contributor)

---

## Project Structure

```
banksbankstander/
├── BanksBankStanderPlugin.java   # Main plugin class, lifecycle & event handling
├── BanksBankStanderConfig.java   # Configuration interface (~20 config options)
├── BanksBankStanderScript.java   # Core script logic, banking & item combining
├── BanksBankStanderOverlay.java  # Overlay rendering for status display
└── CurrentStatus.java            # Status enum for state management
```

---

## State Machine

The plugin uses a simple status enum defined in `CurrentStatus.java`:

| Status | Description |
|--------|-------------|
| `FETCH_SUPPLIES` | Withdrawing items from bank |
| `COMBINE_ITEMS` | Using items on each other |
| `ANIMATING` | Waiting for processing animation |

**Status Access Pattern:**
```java
// Get current status
CurrentStatus status = BanksBankStanderScript.currentStatus;

// Set status
BanksBankStanderScript.currentStatus = CurrentStatus.COMBINE_ITEMS;

// Check status before actions
if (currentStatus != CurrentStatus.FETCH_SUPPLIES) {
    currentStatus = CurrentStatus.FETCH_SUPPLIES;
}
```

---

## Core API Interactions

### Rs2Bank - Banking Operations
```java
// Open bank
Rs2Bank.openBank();
sleepUntil(Rs2Bank::isOpen);

// Check bank state
Rs2Bank.isOpen();

// Close bank
Rs2Bank.closeBank();
sleepUntil(() -> !Rs2Bank.isOpen());

// Deposit operations
Rs2Bank.depositAll();
Rs2Bank.depositAll(itemId);
Rs2Bank.depositOne(itemId);
Rs2Bank.depositAllExcept(itemIds);  // Keep specific items

// Withdraw operations
Rs2Bank.withdrawX(itemId, quantity);
Rs2Bank.withdrawX(itemName, quantity);

// Bank queries
Rs2Bank.hasItem(itemId);
Rs2Bank.count(itemName);
Rs2Bank.bankItems().stream()
    .filter(item -> item.getId() == itemId)
    .mapToInt(item -> item.getQuantity())
    .sum();

// Equipment handling in bank
Rs2Bank.withdrawAndEquip(itemId);
```

### Rs2Inventory - Inventory Management
```java
// Item checks
Rs2Inventory.hasItem(itemId);
Rs2Inventory.hasItem(itemName);
Rs2Inventory.hasItemAmount(itemId, amount);
Rs2Inventory.hasItemAmount(itemName, amount);
Rs2Inventory.isFull();
Rs2Inventory.emptySlotCount();

// Counting
Rs2Inventory.count(itemId);
Rs2Inventory.count(itemName);

// Item retrieval
Rs2Inventory.get(itemName);
Rs2Inventory.items().collect(Collectors.toList());

// Interactions
Rs2Inventory.interact(itemId, action);
Rs2Inventory.interact(item, action);

// Calculate optimal interaction order
List<Rs2ItemModel> orderedSlots = calculateInteractOrder(items, interactOrder);
```

### Rs2Player - Player State
```java
// Animation checks
Rs2Player.isAnimating();

// Safety/logout
Rs2Player.logout();
```

### Rs2Equipment - Equipment Checks
```java
// Check worn items
Rs2Equipment.isWearing(itemId);

// Get equipment slot item
Rs2Equipment.get(EquipmentInventorySlot.AMULET);
```

### Rs2Keyboard - Input Handling
```java
// Send key press (for prompts)
Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
```

### Rs2Random - Randomization
```java
// Generate random values for human-like timing
Rs2Random.between(minValue, maxValue);
```

---

## Script Pattern

The main script extends `Script` and uses the scheduled executor pattern:

```java
public class BanksBankStanderScript extends Script {
    
    public boolean run(BanksBankStanderConfig config) {
        this.config = config;
        
        // Initialize state
        itemsProcessed = 0;
        
        // Parse item identifiers
        firstItemId = TryParseInt(config.firstItemIdentifier());
        secondItemId = TryParseInt(config.secondItemIdentifier());
        
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                // Guard clauses
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                
                // Main logic
                combineItems();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Microbot.log(ex.getMessage());
            }
        }, 0, 100, TimeUnit.MILLISECONDS);  // Fast tick rate for responsiveness
        return true;
    }
}
```

**Tick interval**: 100ms for responsive item processing

---

## Configuration Sections

The config is organized into logical groups:

| Section | Key Options |
|---------|-------------|
| **Item Settings** | `firstItemIdentifier`, `firstItemQuantity`, `secondItemIdentifier`, `secondItemQuantity`, `thirdItemIdentifier`, `thirdItemQuantity`, `fourthItemIdentifier`, `fourthItemQuantity`, `interactOrder` |
| **Toggles** | `pause`, `needPromptEntry`, `waitForAnimation`, `depositAll`, `amuletOfChemistry` |
| **Interaction Menu** | `menu` (default action like "use" or "clean") |
| **Sleep Settings** | `sleepMin`, `sleepMax`, `sleepTarget` (Gaussian distribution) |

---

## Core Workflow

### 1. Fetch Supplies Flow
```java
private String fetchItems() {
    // 1. Handle pause toggle
    if (config.pause()) { /* wait */ }
    
    // 2. Check if we already have items
    if (!hasItems()) {
        // 3. Open bank
        if (!Rs2Bank.isOpen()) {
            Rs2Bank.openBank();
            sleepUntil(Rs2Bank::isOpen);
        }
        
        // 4. Handle optional amulet
        if (config.amuletOfChemistry()) {
            checkForAmulet();
        }
        
        // 5. Deposit unwanted items
        depositUnwantedItems(firstItemId, config.firstItemQuantity());
        
        // 6. Check bank has enough items
        String missingItem = checkItemSums();
        if (!missingItem.isEmpty()) return missingItem;
        
        // 7. Withdraw required items
        getXItem(config.firstItemIdentifier(), config.firstItemQuantity());
        
        // 8. Close bank
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen());
    }
    return "";
}
```

### 2. Combine Items Flow
```java
private boolean combineItems() {
    // 1. Fetch items if needed
    if (!hasItems()) {
        fetchItems();
    }
    
    // 2. Ensure bank is closed
    if (Rs2Bank.isOpen()) {
        Rs2Bank.closeBank();
        sleepUntil(() -> !Rs2Bank.isOpen());
    }
    
    // 3. Wait for animation if configured
    if (config.waitForAnimation()) {
        if (Rs2Player.isAnimating() || recentItemChange) return false;
    }
    
    // 4. Use first item
    interactOrder(firstItemId);
    sleep(calculateSleepDuration(0.5));
    
    // 5. Use second item
    if (config.secondItemQuantity() > 0) {
        interactOrder(secondItemId);
        sleep(calculateSleepDuration(0.5));
    }
    
    // 6. Handle prompt if needed
    if (config.needPromptEntry()) {
        sleepUntil(() -> !isWaitingForPrompt);
        Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
        sleepUntil(() -> !Rs2Inventory.hasItem(secondItemId), 40000);
    }
    return true;
}
```

---

## Adding New Features

### 1. Add Config Option
```java
// In BanksBankStanderConfig.java
@ConfigItem(
    keyName = "myFeature",
    name = "Enable My Feature",
    description = "Description of what this does",
    position = 10,
    section = toggles
)
default boolean myFeature() { return false; }
```

### 2. Use Config in Script
```java
// In BanksBankStanderScript.java
if (config.myFeature()) {
    // Feature logic here
}
```

### 3. Add Event Handling (Optional)
```java
// In BanksBankStanderPlugin.java
@Subscribe
public void onItemContainerChanged(ItemContainerChanged event) {
    if (event.getContainerId() == 93) {  // Inventory container ID
        // Track inventory changes
        BanksBankStanderScript.previousItemChange = System.currentTimeMillis();
    }
}

@Subscribe
public void onWidgetLoaded(WidgetLoaded widget) {
    if (widget.getGroupId() == 270) {  // Make-X prompt widget
        BanksBankStanderScript.isWaitingForPrompt = false;
    }
}
```

### 4. Add New Status
```java
// In CurrentStatus.java
public enum CurrentStatus {
    FETCH_SUPPLIES,
    COMBINE_ITEMS,
    ANIMATING,
    MY_NEW_STATUS  // Add new state
}
```

---

## Best Practices

### Human-Like Timing
```java
// Use Gaussian distribution for natural delays
private int calculateSleepDuration(double multiplier) {
    Random random = new Random();
    double mean = (sleepMin + sleepMax + sleepTarget) / 3.0;
    double stdDeviation = Math.abs(sleepTarget - mean) / 3.0;
    
    int sleepDuration;
    do {
        sleepDuration = (int) Math.round(mean + random.nextGaussian() * stdDeviation);
    } while (sleepDuration < sleepMin || sleepDuration > sleepMax);
    
    return sleepDuration;
}
```

### Item Identifier Flexibility
```java
// Parse item as ID or lookup by name
public static Integer TryParseInt(String text) {
    if (text.isBlank()) return null;
    try {
        return Integer.parseInt(text);
    } catch (NumberFormatException ex) {
        // Lookup item by name
        return Microbot.getItemManager().search(text)
            .stream()
            .map(ItemPrice::getId)
            .findFirst()
            .orElse(null);
    }
}
```

### Proper Item Verification
```java
public boolean hasItems() {
    boolean firstOK = config.firstItemQuantity() <= 0 || Rs2Inventory.hasItem(firstItemId);
    boolean secondOK = config.secondItemQuantity() <= 0 || Rs2Inventory.hasItem(secondItemId);
    // ... repeat for third and fourth
    
    return firstOK && secondOK && thirdOK && fourthOK;
}
```

### Bank Full Handling
```java
// Handle when bank is full
if (Rs2Inventory.emptySlotCount() < 28) {
    Microbot.showMessage("Bank is full, unable to deposit items.");
    // Wait for user to make space
    long start = System.currentTimeMillis();
    while (isRunning() && (System.currentTimeMillis() - start) < 120000) {
        sleepUntilTrue(() -> Rs2Inventory.emptySlotCount() == 28, 1800, 3600);
    }
    // Logout if still full
    if (isRunning() && Rs2Inventory.emptySlotCount() < 28) {
        Rs2Player.logout();
    }
}
```

### Pause Support
```java
// Allow script to be paused without losing overlay
if (config.pause()) {
    while (this.isRunning() && config.pause()) {
        if (!config.pause() || !this.isRunning()) break;
        sleep(100, 1000);
    }
}
```

---

## Logging

```java
// Standard logging
Microbot.log("Descriptive message");
Microbot.log("Insufficient " + itemName);

// Status display in overlay
Microbot.status = "Current action...";

// Show message to user
Microbot.showMessage("Bank is full, unable to deposit items.");

// Stack trace for errors
ex.printStackTrace();
```

---

## Common Patterns

### Sleep Until Condition
```java
// Wait for bank to open
sleepUntil(Rs2Bank::isOpen);
sleepUntil(Rs2Bank::isOpen, 18000);  // With timeout

// Wait for bank to close
sleepUntil(() -> !Rs2Bank.isOpen());

// Wait for inventory change
sleepUntilTrue(() -> Rs2Inventory.hasItemAmount(id, amount), 40, 1800);

// Wait for item processing
sleepUntil(() -> !Rs2Inventory.hasItem(secondItemId), 40000);
```

### Time-Aware Delays
```java
// Track time for consistent delays
timeValue = System.currentTimeMillis();
performAction();
randomNum = calculateSleepDuration(1);
if (System.currentTimeMillis() - timeValue < randomNum) {
    sleep((int) (randomNum - (System.currentTimeMillis() - timeValue)));
} else {
    sleep(Rs2Random.between(14, 48));
}
```

### Efficient Deposit
```java
// Build list of items to keep
List<Integer> bankExcept = new ArrayList<>();
if (config.firstItemQuantity() > 0 && Rs2Inventory.hasItem(firstItemId)) {
    bankExcept.add(firstItemId);
}
// ... add other items

// Deposit all except kept items
if (!bankExcept.isEmpty()) {
    Rs2Bank.depositAllExcept(bankExcept.toArray(new Integer[0]));
} else {
    Rs2Bank.depositAll();
}
```

---

## Testing

1. Set up the plugin in Microbot's debug runner
2. Configure items (use item ID or name)
3. Stand near a bank
4. Enable the plugin and monitor overlay
5. Check logs for any errors

### Common Test Cases
- **Fletching**: Knife + Logs (prompt required)
- **Herblore**: Vial of water + Herb (prompt required, animation wait)
- **Cleaning herbs**: Single item with "clean" action (no prompt, no second item)
- **Cooking**: Raw food at bank chest (special interaction)

---

## Contributing

- Follow existing script patterns
- Use `Rs2*` utilities instead of raw client calls
- Support both item IDs and item names where possible
- Add config options to appropriate sections
- Test with various item combinations
- Handle edge cases (bank full, missing items, etc.)
