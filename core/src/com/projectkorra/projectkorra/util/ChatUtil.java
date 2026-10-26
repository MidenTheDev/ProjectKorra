package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatUtil {

    /**
     * Deserializes section-sign legacy text, including the §x§R§R§G§G§B§B hex format produced by {@link #color(String)}
     */
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    /**
     * Converts a legacy string (using &amp; or § color codes, including &amp;#RRGGBB hex codes) into a Component
     * @param string The legacy string
     * @return The component
     */
    public static Component legacy(final String string) {
        return LEGACY.deserialize(color(string));
    }

    /**
     * Converts a Component back into a legacy string using § color codes. Only use this for APIs that
     * still require strings.
     * @param component The component
     * @return The legacy string
     */
    public static String toLegacy(final Component component) {
        return LEGACY.serialize(component);
    }

    /**
     * Strips all formatting from a Component
     * @param component The component
     * @return The plain text of the component
     */
    public static String plain(final Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    /**
     * Strips all legacy color codes from a string
     * @param string The string to strip
     * @return The plain string
     */
    public static String stripColor(final String string) {
        return string == null ? null : plain(legacy(string));
    }

    /**
     * Parses a color from a config value. Accepts named colors (e.g. "GOLD", "dark_aqua") and hex colors
     * (e.g. "#FFAA00").
     * @param value The value to parse
     * @param def The color to return if the value is invalid
     * @return The parsed color
     */
    public static TextColor parseColor(final String value, final TextColor def) {
        if (value == null) return def;
        if (value.startsWith("#")) {
            final TextColor hex = TextColor.fromHexString(value);
            return hex != null ? hex : def;
        }
        final NamedTextColor named = NamedTextColor.NAMES.value(value.toLowerCase(Locale.ROOT));
        return named != null ? named : def;
    }

    /**
     * Converts a BungeeCord ChatColor into an Adventure TextColor, keeping named colors named
     * @param color The BungeeCord color
     * @return The TextColor, or white if the ChatColor is a format code rather than a color
     */
    @SuppressWarnings("deprecation")
    public static TextColor toTextColor(final net.md_5.bungee.api.ChatColor color) {
        if (color == null || color.getColor() == null) return NamedTextColor.WHITE;
        final int rgb = color.getColor().getRGB() & 0xFFFFFF;
        final NamedTextColor named = NamedTextColor.namedColor(rgb);
        return named != null ? named : TextColor.color(rgb);
    }

    /**
     * Get the color used for ProjectKorra branding from the language config
     * @return The branding color
     */
    public static TextColor getBrandingColor() {
        return parseColor(ConfigManager.languageConfig.get().getString("Chat.Branding.Color"), NamedTextColor.GOLD);
    }

    /**
     * Get the ProjectKorra branding prefix, with its hover and click events
     * @return The prefix component
     */
    public static Component getBrandingPrefix() {
        final TextColor color = getBrandingColor();
        final String start = ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Prefix", "");
        final String main = ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Main", "ProjectKorra");
        final String end = ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Suffix", " » ");
        Component prefix = Component.text().color(color).append(legacy(start), legacy(main), legacy(end)).build();

        final String hover = ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Hover", "Bending brought to you by ProjectKorra | Fork Roku!\nClick for more info.");
        final String click = ConfigManager.languageConfig.get().getString("Chat.Branding.ChatPrefix.Click", "https://www.projectkorra.com");
        if (!hover.isEmpty()) {
            prefix = prefix.hoverEvent(HoverEvent.showText(legacy(hover).colorIfAbsent(color)));
        }
        if (!click.isEmpty()) {
            final String lower = click.toLowerCase(Locale.ROOT);
            if (lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("www.")) {
                prefix = prefix.clickEvent(ClickEvent.openUrl(click));
            } else {
                prefix = prefix.clickEvent(ClickEvent.runCommand(click));
            }
        }
        return prefix;
    }

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send. Uncolored parts are yellow.
     */
    public static void sendBrandingMessage(final CommandSender receiver, final Component message) {
        if (message == null || plain(message).isEmpty()) return;

        receiver.sendMessage(Component.text().append(getBrandingPrefix(), message.colorIfAbsent(NamedTextColor.YELLOW)).build());
    }

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send, using legacy color codes
     */
    public static void sendBrandingMessage(final CommandSender receiver, final String message) {
        sendBrandingMessage(receiver, legacy(message));
    }

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send
     * @deprecated Use {@link #sendBrandingMessage(CommandSender, Component)}
     */
    @Deprecated
    public static void sendBrandingMessage(final CommandSender receiver, final BaseComponent[] message) {
        if (message == null || message.length == 0) return;
        sendBrandingMessage(receiver, GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(message)));
    }

    /**
     * Send a message prefixed with the ProjectKorra branding to the provided receiver
     * @param receiver The person to send the message to
     * @param message The message to send
     * @deprecated Use {@link #sendBrandingMessage(CommandSender, Component)}
     */
    @Deprecated
    public static void sendBrandingMessage(final CommandSender receiver, final BaseComponent message) {
        if (message == null) return;
        sendBrandingMessage(receiver, GsonComponentSerializer.gson().deserialize(ComponentSerializer.toString(message)));
    }

    /**
     * Colors a string with color codes and hex color codes. This also does &amp;#RRGGBB codes which are not
     * done by ChatColor's methods. Prefer {@link #legacy(String)} for anything that is sent to a player.
     * @param string The string to color
     * @return The colored string
     */
    public static String color(String string) {
        string = string == null ? "" : string; //Ensure no NullPointers
        string = string.replaceAll("\\\\n", "\n")
                .replaceAll("[§&]#([A-Fa-f\\d])([A-Fa-f\\d])([A-Fa-f\\d])([A-Fa-f\\d])([A-Fa-f\\d])([A-Fa-f\\d])",
                        "§x§$1§$2§$3§$4§$5§$6"); //Replaces &#RRGGBB to &x&R&R&B&B&G&G (how hex actually works)
        return string.replaceAll("&([0-9A-Fa-fK-Ok-oRrXx])", "§$1");
    }

    /**
     * Ensures a multiline string is properly formatted with color codes
     * @param string The string to format
     * @return The formatted string
     * @deprecated Components keep their formatting across lines; use {@link #legacy(String)}
     */
    @Deprecated
    public static String multiline(String string) {
        string = color(string);
        Character lastColor = null;
        String hex = null;
        List<String> l = new ArrayList<>();
        for (String line : string.split("\n")) {
            String prefix = "";
            if (lastColor != null) {
                prefix = "§" + lastColor;  //Make the prefix the current color
                if (hex != null) prefix += hex; //If the current color is a hex code, add on the RGB as well

                if (l.size() == 0 && string.charAt(0) == '§') prefix = ""; //Don't bother adding a pointless color code
            }

            l.add(prefix + line);
            if (line.contains("§")) {
                int index = line.lastIndexOf('§');
                lastColor = line.charAt(index + 1);
                if (lastColor == 'x') {
                    hex = line.substring(index + 2, index + 14);
                } else hex = null;
            }
        }
        return String.join("\n", l);
    }

    /**
     * Previews the current move to the player
     * @param player The player
     * @param slot The slot to display
     */
    public static void displayMovePreview(final Player player, final int slot) {
        if (!ConfigManager.defaultConfig.get().getBoolean("Properties.BendingPreview")) {
            return;
        }

        final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if (bPlayer == null) {
            return;
        }
        String displayedMessage = bPlayer.getAbilities().get(slot);
        final CoreAbility ability = CoreAbility.getAbility(displayedMessage);

        if (ability != null) {
            displayedMessage = ability.getMovePreview(player);
        } else if (displayedMessage == null) {
            displayedMessage = "";
        }

        sendActionBar(displayedMessage, player);
    }

    /**
     * Previews the current move to the player
     * @param player The player
     */
    public static void displayMovePreview(final Player player) {
        displayMovePreview(player, player.getInventory().getHeldItemSlot() + 1);
    }

    /**
     * Sends an action bar message to the player
     *
     * @param message the message to send
     * @param player the player to send the message to
     */
    public static void sendActionBar(final Component message, final Player... player) {
        for (Player e : player) {
            e.sendActionBar(message);
        }
    }

    /**
     * Sends an action bar message to the player
     *
     * @param message the message to send, using legacy color codes
     * @param player the player to send the message to
     */
    public static void sendActionBar(final String message, final Player... player) {
        sendActionBar(legacy(message), player);
    }
}
