package net.runelite.client;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.runelite.client.plugins.microbot.aiofighter.AIOFighterPlugin;
import net.runelite.client.plugins.microbot.banksbankstander.BanksBankStanderPlugin;
import net.runelite.client.plugins.microbot.crafting.CraftingPlugin;
import net.runelite.client.plugins.microbot.qualityoflife.QoLPlugin;

public class Microbot {

    private static final Class<?>[] debugPlugins = {
            AIOFighterPlugin.class,
            BanksBankStanderPlugin.class,
            CraftingPlugin.class,
            QoLPlugin.class
    };

    public static void main(String[] args) throws Exception {
        List<Class<?>> _debugPlugins = Arrays.stream(debugPlugins).collect(Collectors.toList());
        RuneLiteDebug.pluginsToDebug.addAll(_debugPlugins);
        RuneLiteDebug.main(args);
    }
}
