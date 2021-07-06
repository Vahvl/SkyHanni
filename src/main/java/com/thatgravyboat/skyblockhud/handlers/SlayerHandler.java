package com.thatgravyboat.skyblockhud.handlers;

import com.thatgravyboat.skyblockhud.Utils;
import com.thatgravyboat.skyblockhud.api.events.SidebarLineUpdateEvent;
import com.thatgravyboat.skyblockhud.api.events.SidebarPostEvent;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SlayerHandler {

    //Optional Characters are required because Hypixel dumb and cuts off text
    private static final Pattern COMBAT_XP_REGEX = Pattern.compile(
        "\\(([\\d,]+)/([\\dkm]+)\\) comb?a?t? x?p?"
    );
    private static final Pattern KILLS_REGEX = Pattern.compile(
        "(\\d+)/(\\d+) kills?"
    );

    public enum slayerTypes {
        ZOMBIE(34, "Revenant Horror"),
        WOLF(42, "Sven Packmaster"),
        SPIDER(50, "Tarantula Broodfather"),
        VOIDGLOOMSERAPH(50, "Voidgloom Seraph"),
        NONE(0, "");

        private final String displayName;
        private final int x;

        slayerTypes(int x, String displayName) {
            this.displayName = displayName;
            this.x = x;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getX() {
            return x;
        }
    }

    public static slayerTypes currentSlayer = slayerTypes.NONE;
    public static int slayerTier = 0;
    public static boolean isDoingSlayer = false;
    public static int progress = 0;
    public static int maxKills = 0;
    public static boolean bossSlain = false;
    public static boolean isKillingBoss = false;

    public static void clearSlayer() {
        currentSlayer = slayerTypes.NONE;
        isDoingSlayer = false;
        progress = 0;
        maxKills = 0;
        bossSlain = false;
        isKillingBoss = false;
    }

    @SubscribeEvent
    public void onSidebarPost(SidebarPostEvent event) {
        String arrayString = Arrays.toString(event.arrayScores);
        isDoingSlayer =
            Arrays.toString(event.arrayScores).contains("Slayer Quest");
        if (
            isDoingSlayer &&
            (
                currentSlayer.equals(slayerTypes.NONE) ||
                !arrayString
                    .replace(" ", "")
                    .contains(
                        currentSlayer.getDisplayName().replace(" ", "") +
                        Utils.intToRomanNumeral(slayerTier)
                    )
            )
        ) {
            for (int i = 0; i < event.scores.size(); i++) {
                String line = event.scores.get(i);
                if (line.contains("Slayer Quest") && event.scores.size() > 3) {
                    String slayer = event.scores.get(i - 1).toLowerCase();
                    SlayerHandler.slayerTypes selectedSlayer =
                        SlayerHandler.slayerTypes.NONE;
                    for (slayerTypes types : slayerTypes.values()) {
                        if (
                            slayer.contains(
                                types.displayName.toLowerCase(Locale.ENGLISH)
                            )
                        ) {
                            selectedSlayer = types;
                            break;
                        }
                    }
                    SlayerHandler.currentSlayer = selectedSlayer;
                    SlayerHandler.slayerTier =
                        Utils.whatRomanNumeral(
                            slayer
                                .replace(
                                    selectedSlayer
                                        .getDisplayName()
                                        .toLowerCase(),
                                    ""
                                )
                                .replace(" ", "")
                        );
                    break;
                }
            }
        }

        if (!isDoingSlayer) {
            clearSlayer();
        }
    }

    @SubscribeEvent
    public void onSidebarLineUpdate(SidebarLineUpdateEvent event) {
        if (
            !isDoingSlayer && event.formattedLine.equals("Slayer Quest")
        ) isDoingSlayer = true;

        if (isDoingSlayer) {
            String line = event.formattedLine.toLowerCase();
            Matcher killMatcher = KILLS_REGEX.matcher(line);
            Matcher xpMatcher = COMBAT_XP_REGEX.matcher(line);

            if (killMatcher.find()) {
                SlayerHandler.bossSlain = false;
                SlayerHandler.isKillingBoss = false;
                try {
                    progress = Integer.parseInt(killMatcher.group(1));
                } catch (Exception ignored) {}
                try {
                    maxKills = Integer.parseInt(killMatcher.group(2));
                } catch (Exception ignored) {}
            } else if (xpMatcher.find()) {
                SlayerHandler.bossSlain = false;
                SlayerHandler.isKillingBoss = false;
                try {
                    progress = Integer.parseInt(xpMatcher.group(1));
                } catch (Exception ignored) {}
                try {
                    maxKills =
                        Integer.parseInt(xpMatcher.group(2).replace("k", "")) *
                        (
                            xpMatcher.group(2).contains("k")
                                ? 1000
                                : xpMatcher.group(2).contains("m") ? 1000000 : 1
                        );
                } catch (Exception ignored) {}
            } else if (line.contains("slay the boss")) {
                SlayerHandler.bossSlain = false;
                SlayerHandler.isKillingBoss = true;
                SlayerHandler.maxKills = 0;
                SlayerHandler.progress = 0;
            } else if (line.contains("boss slain")) {
                SlayerHandler.isKillingBoss = false;
                SlayerHandler.maxKills = 0;
                SlayerHandler.progress = 0;
                SlayerHandler.bossSlain = true;
            }
            if (maxKills == 0 && progress == 0) {
                SlayerHandler.maxKills = 0;
                SlayerHandler.progress = 0;
            }
        }
    }
}
