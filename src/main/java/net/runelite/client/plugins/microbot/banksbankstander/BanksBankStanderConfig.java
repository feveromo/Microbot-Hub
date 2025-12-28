package net.runelite.client.plugins.microbot.banksbankstander;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.util.inventory.InteractOrder;

@ConfigGroup("BankStander")
@ConfigInformation("<h2>Bank's BankStander</h2>" +
                "<p>Automates bank-standing skilling.</p>" +

                "<h3>Quick Examples</h3>" +
                "<ul style='margin:0;padding-left:15px'>" +
                "<li><b>Fletching:</b> Knife(1) + Logs(27), Prompt ✓</li>" +
                "<li><b>Herblore:</b> Vial(14) + Herb(14), Prompt ✓</li>" +
                "<li><b>Clean herbs:</b> Grimy(28), Menu='clean'</li>" +
                "<li><b>Scatter ashes:</b> Ashes(28), Menu='scatter', Wait Each ✓</li>" +
                "<li><b>Bury bones:</b> Bones(28), Menu='bury', Wait Each ✓</li>" +
                "</ul>" +

                "<h3>Toggle Guide</h3>" +
                "<ul style='margin:0;padding-left:15px'>" +
                "<li><b>Prompt</b> - 'Make X' dialogs</li>" +
                "<li><b>Wait Process</b> - Batch actions</li>" +
                "<li><b>Wait Each</b> - Per-item anims</li>" +
                "</ul>" +

                "<p><small>Use item names or IDs. Qty=0 disables slot.</small></p>")
public interface BanksBankStanderConfig extends Config {

        // ========================
        // SECTION DEFINITIONS
        // ========================

        @ConfigSection(name = "Item Settings", description = "Configure items to use/combine. Use item names or IDs.", position = 0, closedByDefault = false)
        String itemSection = "itemSection";

        @ConfigSection(name = "Behavior Toggles", description = "Control script behavior based on activity type", position = 1, closedByDefault = false)
        String toggles = "toggles";

        @ConfigSection(name = "Interaction Menu", description = "Custom right-click action (e.g., 'clean', 'scatter', 'bury')", position = 2, closedByDefault = true)
        String interaction = "interaction";

        @ConfigSection(name = "Sleep Settings", description = "Timing randomization for human-like behavior", position = 3, closedByDefault = true)
        String sleepSection = "sleepSection";

        // ========================
        // ITEM SETTINGS
        // ========================

        @ConfigItem(keyName = "interactOrder", name = "Interact Order", description = "Order to process inventory items (Standard, Random, etc.)", position = 0, section = itemSection)
        default InteractOrder interactOrder() {
                return InteractOrder.STANDARD;
        }

        @ConfigItem(keyName = "First Item", name = "First Item", description = "Item name or ID (e.g., 'Knife' or '946')", position = 1, section = itemSection)
        default String firstItemIdentifier() {
                return "Knife";
        }

        @ConfigItem(keyName = "First Item Quantity", name = "First Item Qty", description = "Amount to withdraw (1 for tools, 28 for single-item processing)", position = 2, section = itemSection)
        @Range(min = 1, max = 28)
        default int firstItemQuantity() {
                return 1;
        }

        @ConfigItem(keyName = "Second Item", name = "Second Item", description = "Item name or ID. Leave blank if not needed.", position = 3, section = itemSection)
        default String secondItemIdentifier() {
                return "Logs";
        }

        @ConfigItem(keyName = "Second Item Quantity", name = "Second Item Qty", description = "Set to 0 to disable second item", position = 4, section = itemSection)
        @Range(min = 0, max = 27)
        default int secondItemQuantity() {
                return 27;
        }

        @ConfigItem(keyName = "Third Item", name = "Third Item", description = "Optional third item name or ID", position = 5, section = itemSection)
        default String thirdItemIdentifier() {
                return "";
        }

        @ConfigItem(keyName = "Third Item Quantity", name = "Third Item Qty", description = "Set to 0 to disable third item", position = 6, section = itemSection)
        @Range(min = 0, max = 27)
        default int thirdItemQuantity() {
                return 0;
        }

        @ConfigItem(keyName = "Fourth Item", name = "Fourth Item", description = "Optional fourth item name or ID", position = 7, section = itemSection)
        default String fourthItemIdentifier() {
                return "";
        }

        @ConfigItem(keyName = "Fourth Item Quantity", name = "Fourth Item Qty", description = "Set to 0 to disable fourth item", position = 8, section = itemSection)
        @Range(min = 0, max = 27)
        default int fourthItemQuantity() {
                return 0;
        }

        // ========================
        // BEHAVIOR TOGGLES
        // ========================

        @ConfigItem(keyName = "pause", name = "⏸ Pause Script", description = "Temporarily pause the script without losing progress", position = 0, section = toggles)
        default boolean pause() {
                return false;
        }

        @ConfigItem(keyName = "Prompt", name = "📝 Wait for Prompt", description = "Enable for 'Make X' dialogs (fletching, herblore, crafting). Presses SPACE to confirm.", position = 1, section = toggles)
        default boolean needPromptEntry() {
                return true;
        }

        @ConfigItem(keyName = "WaitForProcess", name = "⏳ Wait for Batch Process", description = "Wait for entire inventory to finish processing before banking. Use with Prompt for batch activities.", position = 2, section = toggles)
        default boolean waitForAnimation() {
                return true;
        }

        @ConfigItem(keyName = "WaitBetweenEachItem", name = "🔄 Wait Between Each Item", description = "Wait for animation after EACH item (scatter ashes, bury bones). NOT for batch activities.", position = 3, section = toggles)
        default boolean waitBetweenEachItem() {
                return false;
        }

        @ConfigItem(keyName = "DepositAll", name = "🗑 Deposit All First", description = "Deposit entire inventory before withdrawing items. Useful for clearing unwanted items.", position = 4, section = toggles)
        default boolean depositAll() {
                return false;
        }

        @ConfigItem(keyName = "AmuletofChemistry", name = "⚗ Use Amulet of Chemistry", description = "Auto-equip Amulet of Chemistry for potion making (Members only)", position = 5, section = toggles)
        default boolean amuletOfChemistry() {
                return false;
        }

        // ========================
        // INTERACTION MENU
        // ========================

        @ConfigItem(keyName = "Interaction Option", name = "Menu Action", description = "Right-click action to use. Examples: 'use', 'clean', 'scatter', 'bury', 'eat'", position = 0, section = interaction)
        default String menu() {
                return "use";
        }

        // ========================
        // SLEEP SETTINGS
        // ========================

        @ConfigItem(keyName = "Sleep Min", name = "Minimum Delay (ms)", description = "Minimum delay between actions in milliseconds", position = 0, section = sleepSection)
        @Range(min = 60, max = 20000)
        default int sleepMin() {
                return 60;
        }

        @ConfigItem(keyName = "Sleep Max", name = "Maximum Delay (ms)", description = "Maximum delay between actions in milliseconds", position = 1, section = sleepSection)
        @Range(min = 90, max = 20000)
        default int sleepMax() {
                return 1800;
        }

        @ConfigItem(keyName = "Sleep Target", name = "Target Delay (ms)", description = "Most common delay value (Gaussian distribution mean)", position = 2, section = sleepSection)
        @Range(min = 100, max = 20000)
        default int sleepTarget() {
                return 900;
        }
}
