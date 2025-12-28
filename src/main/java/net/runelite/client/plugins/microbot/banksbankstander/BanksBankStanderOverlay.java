package net.runelite.client.plugins.microbot.banksbankstander;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class BanksBankStanderOverlay extends OverlayPanel {
    private final BanksBankStanderConfig config;
    private long startTime;

    @Inject
    BanksBankStanderOverlay(BanksBankStanderPlugin plugin, BanksBankStanderConfig config) {
        super(plugin);
        this.config = config;
        this.startTime = System.currentTimeMillis();
        setPosition(OverlayPosition.TOP_LEFT);
        setNaughty();
    }

    public void resetTimer() {
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        try {
            panelComponent.setPreferredSize(new Dimension(200, 0));

            // Title
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("BankStander v" + BanksBankStanderPlugin.version)
                    .color(Color.CYAN)
                    .build());

            // Runtime
            long runtime = System.currentTimeMillis() - startTime;
            String formattedTime = formatTime(runtime);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Runtime:")
                    .right(formattedTime)
                    .build());

            // Current Status
            CurrentStatus status = BanksBankStanderScript.currentStatus;
            Color statusColor = getStatusColor(status);
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Status:")
                    .right(getStatusText(status))
                    .rightColor(statusColor)
                    .build());

            // Items Processed
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Items:")
                    .right(String.valueOf(BanksBankStanderScript.itemsProcessed))
                    .rightColor(Color.GREEN)
                    .build());

            // Items per hour calculation
            if (runtime > 5000 && BanksBankStanderScript.itemsProcessed > 0) {
                int itemsPerHour = (int) ((BanksBankStanderScript.itemsProcessed * 3600000L) / runtime);
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Per Hour:")
                        .right(String.format("%,d", itemsPerHour))
                        .rightColor(Color.YELLOW)
                        .build());
            }

            // Paused indicator
            if (config.pause()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("** PAUSED **")
                        .leftColor(Color.ORANGE)
                        .build());
            }

            // Activity info (if configured)
            String firstItem = config.firstItemIdentifier();
            if (!firstItem.isBlank()) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Item:")
                        .right(truncate(firstItem, 15))
                        .rightColor(Color.WHITE)
                        .build());
            }

            // Action being used
            String action = config.menu();
            if (!action.equalsIgnoreCase("use")) {
                panelComponent.getChildren().add(LineComponent.builder()
                        .left("Action:")
                        .right(action)
                        .rightColor(Color.WHITE)
                        .build());
            }

        } catch (Exception ex) {
            System.out.println("Overlay error: " + ex.getMessage());
        }
        return super.render(graphics);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60);
    }

    private Color getStatusColor(CurrentStatus status) {
        if (status == null)
            return Color.WHITE;
        switch (status) {
            case FETCH_SUPPLIES:
                return Color.YELLOW;
            case COMBINE_ITEMS:
                return Color.GREEN;
            case ANIMATING:
                return Color.CYAN;
            default:
                return Color.WHITE;
        }
    }

    private String getStatusText(CurrentStatus status) {
        if (status == null)
            return "Starting...";
        switch (status) {
            case FETCH_SUPPLIES:
                return "Banking";
            case COMBINE_ITEMS:
                return "Processing";
            case ANIMATING:
                return "Animating";
            default:
                return status.name();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength - 2) + "..";
    }
}
