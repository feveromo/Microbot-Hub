package net.runelite.client.plugins.microbot.crafting.scripts;

import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.crafting.CraftingConfig;
import net.runelite.client.plugins.microbot.crafting.enums.LeatherArmour;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LeatherScript extends Script implements ICraftingScript {

    public static double version = 1.0;
    private CraftingConfig config;
    private CraftingState state = CraftingState.IDLE;
    private int itemsCrafted = 0;
    private long lastCraftingXp = -1;

    // Item IDs
    private static final int NEEDLE = 1733;
    private static final int THREAD = 1734;
    private static final int COSTUME_NEEDLE = 6363;

    @Override
    public String getName() {
        return "Leather Crafting";
    }

    @Override
    public String getVersion() {
        return String.valueOf(version);
    }

    @Override
    public String getState() {
        if (state == null) {
            return "null";
        }
        return state.toString();
    }

    @Override
    public Map<String, String> getCustomProperties() {
        Map<String, String> properties = new LinkedHashMap<>();
        if (config != null) {
            properties.put(config.leatherType().getName() + " made:", Integer.toString(itemsCrafted));
        }
        return properties;
    }

    public boolean run(CraftingConfig config) {
        this.config = config;
        this.state = CraftingState.IDLE;
        this.itemsCrafted = 0;
        this.lastCraftingXp = -1;
        Rs2Antiban.resetAntibanSettings();
        leatherCraftingAntiBan();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!super.run())
                    return;
                calculateState();

                switch (state) {
                    case BANKING:
                        handleBanking();
                        break;
                    case CRAFTING:
                        handleCrafting();
                        break;
                    case STOPPED:
                        shutdown();
                        break;
                    default:
                        break;
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
        return true;
    }

    private void calculateState() {
        LeatherArmour armour = config.leatherType();
        if (armour == LeatherArmour.NONE ||
                Rs2Player.getRealSkillLevel(Skill.CRAFTING) < armour.getLevelRequired()) {
            state = CraftingState.STOPPED;
            return;
        }

        if (hasMaterials()) {
            state = CraftingState.CRAFTING;
        } else {
            state = CraftingState.BANKING;
        }
    }

    private boolean hasMaterials() {
        LeatherArmour armour = config.leatherType();

        // All leather items require only 1 leather
        int leatherRequired = 1;

        // Check for tools based on costume needle setting
        boolean hasTools;
        if (config.useCostumeNeedleLeather()) {
            hasTools = Rs2Inventory.hasItem(COSTUME_NEEDLE);
        } else {
            hasTools = Rs2Inventory.hasItem(NEEDLE) && Rs2Inventory.hasItem(THREAD);
        }

        return hasTools && Rs2Inventory.hasItemAmount(armour.getLeatherId(), leatherRequired);
    }

    private void handleBanking() {
        Microbot.status = "Banking";
        if (Rs2Bank.isOpen()) {
            if (config.useCostumeNeedleLeather()) {
                if (!Rs2Inventory.hasItem(COSTUME_NEEDLE)) {
                    if (!Rs2Bank.hasItem(COSTUME_NEEDLE)) {
                        Microbot.showMessage("You don't have a costume needle!");
                        state = CraftingState.STOPPED;
                        shutdown();
                        return;
                    }
                    if (Rs2Inventory.emptySlotCount() < 1) {
                        Rs2Bank.depositAll();
                    }
                    Rs2Bank.withdrawItem(true, COSTUME_NEEDLE);
                }

                LeatherArmour armour = config.leatherType();
                Rs2Bank.depositAllExcept(COSTUME_NEEDLE, armour.getLeatherId());
            } else {
                if (!Rs2Inventory.hasItem(NEEDLE) || !Rs2Inventory.hasItem(THREAD)) {
                    if (!Rs2Bank.hasAllItems(new int[] { NEEDLE, THREAD })) {
                        Microbot.showMessage("You've run out of materials!");
                        state = CraftingState.STOPPED;
                        shutdown();
                        return;
                    }
                    if (Rs2Inventory.emptySlotCount() < 2) {
                        Rs2Bank.depositAll();
                    }
                    Rs2Bank.withdrawItem(true, NEEDLE);
                    Rs2Bank.withdrawAll(THREAD);
                }

                LeatherArmour armour = config.leatherType();
                Rs2Bank.depositAllExcept(NEEDLE, THREAD, armour.getLeatherId());
            }

            LeatherArmour armour = config.leatherType();
            if (!Rs2Bank.hasItem(armour.getLeatherId())) {
                Microbot.showMessage("You've run out of leather!");
                state = CraftingState.STOPPED;
                shutdown();
                return;
            }

            Rs2Bank.withdrawAll(armour.getLeatherId());
            Rs2Bank.closeBank();
            sleepUntil(() -> !Rs2Bank.isOpen());
        } else {
            Rs2Bank.openBank();
        }
    }

    private void handleCrafting() {
        LeatherArmour armour = config.leatherType();
        Microbot.status = "Crafting " + armour.getName();

        if (Rs2Bank.isOpen()) {
            Rs2Bank.closeBank();
            sleepUntil(() -> !Rs2Bank.isOpen(), 5000);
        }

        if (lastCraftingXp <= 0) {
            lastCraftingXp = Microbot.getClient().getSkillExperience(Skill.CRAFTING);
        }

        if (config.useCostumeNeedleLeather()) {
            Rs2Inventory.use(COSTUME_NEEDLE);
        } else {
            Rs2Inventory.use(NEEDLE);
        }
        Rs2Inventory.use(armour.getLeatherId());

        if (sleepUntil(() -> Rs2Widget.getWidget(17694733) != null, 5000)) {
            Rs2Keyboard.keyPress(armour.getMenuEntry());
            sleep(1800);

            while (super.run() && hasMaterials() && Microbot.isGainingExp) {
                long currentXp = Microbot.getClient().getSkillExperience(Skill.CRAFTING);
                if (currentXp > lastCraftingXp) {
                    itemsCrafted++;
                    lastCraftingXp = currentXp;
                }
                sleep(10);
            }
        }
    }

    public void leatherCraftingAntiBan() {
        Rs2AntibanSettings.antibanEnabled = true;
        Rs2AntibanSettings.universalAntiban = false;
        Rs2AntibanSettings.contextualVariability = true;
        Rs2AntibanSettings.devDebug = false;

        Rs2AntibanSettings.usePlayStyle = true;
        Rs2AntibanSettings.randomIntervals = false;
        Rs2AntibanSettings.simulateFatigue = true;
        Rs2AntibanSettings.simulateAttentionSpan = true;
        Rs2AntibanSettings.behavioralVariability = true;
        Rs2AntibanSettings.nonLinearIntervals = true;
        Rs2AntibanSettings.dynamicIntensity = false;
        Rs2AntibanSettings.dynamicActivity = false;

        Rs2AntibanSettings.profileSwitching = false;
        Rs2AntibanSettings.timeOfDayAdjust = false;
        Rs2AntibanSettings.playSchedule = false;

        Rs2AntibanSettings.naturalMouse = true;
        Rs2AntibanSettings.simulateMistakes = true;
        Rs2AntibanSettings.moveMouseOffScreen = true;
        Rs2AntibanSettings.moveMouseOffScreenChance = 0.10;
        Rs2AntibanSettings.moveMouseRandomly = true;
        Rs2AntibanSettings.moveMouseRandomlyChance = 0.10;
        Rs2Antiban.setActivityIntensity(ActivityIntensity.LOW);

        Rs2AntibanSettings.takeMicroBreaks = true;
        Rs2AntibanSettings.microBreakDurationLow = 1;
        Rs2AntibanSettings.microBreakDurationHigh = 2;
        Rs2AntibanSettings.microBreakChance = 0.15;
        Rs2Antiban.setActivity(Activity.GENERAL_CRAFTING);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}
