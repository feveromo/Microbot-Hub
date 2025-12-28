package net.runelite.client.plugins.microbot.crafting.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LeatherArmour {
    NONE("", 0, 0, 0, '0'),

    // Regular leather items (Material ID: 1741)
    LEATHER_GLOVES("Leather gloves", 1741, 1059, 1, '1'),
    LEATHER_BOOTS("Leather boots", 1741, 1061, 7, '2'),
    LEATHER_COWL("Leather cowl", 1741, 1167, 9, '3'),
    LEATHER_VAMBRACES("Leather vambraces", 1741, 1063, 11, '4'),
    LEATHER_BODY("Leather body", 1741, 1129, 14, '5'),
    LEATHER_CHAPS("Leather chaps", 1741, 1095, 18, '6'),
    COIF("Coif", 1741, 1169, 38, '7'),

    // Hard leather items (Material ID: 1743)
    HARDLEATHER_BODY("Hardleather body", 1743, 1131, 28, '1');

    private final String name;
    private final int leatherId;
    private final int itemId;
    private final int levelRequired;
    private final char menuEntry;

    @Override
    public String toString() {
        return name;
    }
}
