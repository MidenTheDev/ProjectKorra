package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Executor for /bending help. Extends {@link PKCommand}.
 */
public class HelpCommand extends PKCommand {

	private final String required;
	private final String optional;
	private final String properUsage;
	private final Map<Element, String> elementHelp = new HashMap<>();
	private final String invalidTopic;
	private final String usage;
	private final String slotFormat;
	private final String bindStart;
	private final String bindSeperator;
	private final String bindEnd;
	private final String hoverBind;
	private final boolean enableQuickBind;

	public HelpCommand() {
		super("help", "/bending help <Page/Topic>", ConfigManager.languageConfig.get().getString("Commands.Help.Description"), new String[] { "help", "h" });

		this.required = ConfigManager.languageConfig.get().getString("Commands.Help.Required");
		this.optional = ConfigManager.languageConfig.get().getString("Commands.Help.Optional");
		this.properUsage = ConfigManager.languageConfig.get().getString("Commands.Help.ProperUsage");
		this.invalidTopic = ConfigManager.languageConfig.get().getString("Commands.Help.InvalidTopic");
		this.usage = ConfigManager.languageConfig.get().getString("Commands.Help.Usage");
		this.slotFormat = ConfigManager.languageConfig.get().getString("Commands.Help.SlotFormat");
		this.bindStart = ConfigManager.languageConfig.get().getString("Commands.Help.BindStart");
		this.bindSeperator = ConfigManager.languageConfig.get().getString("Commands.Help.BindSeparator");
		this.bindEnd = ConfigManager.languageConfig.get().getString("Commands.Help.BindEnd");
		this.hoverBind = ConfigManager.languageConfig.get().getString("Commands.Help.HoverBind");
		this.enableQuickBind = ConfigManager.languageConfig.get().getBoolean("Commands.Help.EnableQuickBind");

		elementHelp.clear();

		for (Element e : Element.getAllElements()) {
			if (ConfigManager.languageConfig.get().contains("Commands.Help.Elements." + e.getName())) {
				elementHelp.put(e, ConfigManager.languageConfig.get().getString("Commands.Help.Elements." + e.getName()));
			}
		}
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		boolean firstMessage = true;

		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		} else if (args.isEmpty()) {
			final List<String> strings = new ArrayList<>();
			for (final PKCommand command : instances.values()) {
				if (!command.getName().equalsIgnoreCase("help") && command.canUse(sender)) {
					strings.add(command.getProperUse());
				}
			}
			
			Collections.sort(strings);
			Collections.reverse(strings);
			strings.add(instances.get("help").getProperUse());
			Collections.reverse(strings);

			for (final String s : this.getPage(strings, ChatUtil.color("&6Commands: <" + this.required + "> [" + this.optional + "]"), 1, false)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatUtil.legacy(s).colorIfAbsent(NamedTextColor.YELLOW));
				}
			}
			return;
		}

		final String arg = args.get(0).toLowerCase();

		if (this.isNumeric(arg)) {
			final List<String> strings = new ArrayList<>();
			for (final PKCommand command : instances.values()) {
				strings.add(command.getProperUse());
			}
			
			for (final String s : this.getPage(strings, ChatUtil.color("&6Commands: <" + this.required + "> [" + this.optional + "]"), Integer.valueOf(arg), true)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatUtil.legacy(s).colorIfAbsent(NamedTextColor.YELLOW));
				}
			}
		} else if (instances.keySet().contains(arg)) {// bending help command.
			instances.get(arg).help(sender, true);
		} else if (Arrays.asList(Commands.comboaliases).contains(arg)) { // bending help elementcombo.
			sender.sendMessage(ChatUtil.legacy("&6" + this.properUsage.replace("{command1}", "&c/bending display " + arg + "&6").replace("{command2}", "&c/bending help <Combo Name>&6")));
		} else if (Arrays.asList(Commands.passivealiases).contains(arg)) { // bending help elementpassive.
			sender.sendMessage(ChatUtil.legacy("&6" + this.properUsage.replace("{command1}", "&c/bending display " + arg + "&6").replace("{command2}", "&c/bending help <Passive Name>&c")));
		} else if (CoreAbility.getAbility(arg) != null && !(CoreAbility.getAbility(arg) instanceof ComboAbility) && CoreAbility.getAbility(arg).isEnabled() && !CoreAbility.getAbility(arg).isHiddenAbility() || CoreAbility.getAbility(arg) instanceof PassiveAbility) { // bending help ability.
			final CoreAbility ability = CoreAbility.getAbility(arg);
			final TextColor color = ability.getElement().getTextColor();
			final boolean isAddonAbility = ability instanceof AddonAbility;
			final boolean isPassiveAbility = ability instanceof PassiveAbility;
			
			
			sender.sendMessage(this.title(ability.getName(), color, isAddonAbility ? (isPassiveAbility ? " (Addon Passive)" : " (Addon)") : (isPassiveAbility ? " (Passive)" : "")));
			sender.sendMessage(ChatUtil.legacy(ability.getDescription()).colorIfAbsent(color));
			
			if (!ability.getInstructions().isEmpty()) {
				sender.sendMessage(ChatUtil.legacy(this.usage + ability.getInstructions()).colorIfAbsent(NamedTextColor.WHITE));
			}
			
			if (!isPassiveAbility && sender instanceof Player && this.enableQuickBind && BendingPlayer.getBendingPlayer((Player)sender).canBind(CoreAbility.getAbility(arg))) {
				final TextComponent.Builder bindShortcut = Component.text().append(ChatUtil.legacy(this.bindStart));

				for (int i = 1; i <= 9; i++) {
					if (i > 1) {
						bindShortcut.append(ChatUtil.legacy(this.bindSeperator));
					}

					final Component hover = ChatUtil.legacy(this.hoverBind).colorIfAbsent(NamedTextColor.WHITE)
							.replaceText(literal("{ability}", Component.text(ability.getName(), color)))
							.replaceText(literal("{slot}", Component.text(i, color)));
					bindShortcut.append(ChatUtil.legacy(this.slotFormat.replace("{slot}", String.valueOf(i)).replace("{element_color}", code(color)))
							.hoverEvent(HoverEvent.showText(hover))
							.clickEvent(ClickEvent.runCommand("/bending bind " + ability.getName() + " " + i)));
				}

				bindShortcut.append(ChatUtil.legacy(this.bindEnd));
				sender.sendMessage(bindShortcut.build());
			}
			
			if (isAddonAbility) {
				final AddonAbility addonAbility = (AddonAbility) ability;
				sender.sendMessage(this.info("- By: ", color, addonAbility.getAuthor()));
				sender.sendMessage(this.info("- Version: ", color, addonAbility.getVersion()));
			}
		} else if (Arrays.asList(Commands.airaliases).contains(arg)) {
			sender.sendMessage(this.elementHelp(Element.AIR));
		} else if (Arrays.asList(Commands.wateraliases).contains(arg)) {
			sender.sendMessage(this.elementHelp(Element.WATER));
		} else if (Arrays.asList(Commands.earthaliases).contains(arg)) {
			sender.sendMessage(this.elementHelp(Element.EARTH));
		} else if (Arrays.asList(Commands.firealiases).contains(arg)) {
			sender.sendMessage(this.elementHelp(Element.FIRE));
		} else if (Arrays.asList(Commands.chialiases).contains(arg)) {
			sender.sendMessage(this.elementHelp(Element.CHI));
		} else if (Arrays.asList(Commands.avataraliases).contains(arg)) {
			sender.sendMessage(this.elementHelp(Element.AVATAR));
		} else if (Element.getElement(arg) != null && elementHelp.containsKey(Element.getElement(arg))) {
			Element element = Element.getElement(arg);
			sender.sendMessage(this.elementHelp(element));
		} else {
			// combos - handled differently because they're stored in CamelCase in ComboManager.
			for (final String combo : ComboManager.getDescriptions().keySet()) {
				if (combo.equalsIgnoreCase(arg)) {
					final CoreAbility ability = CoreAbility.getAbility(combo);
					final TextColor color = ability != null ? ability.getElement().getTextColor() : null;

					if (ability instanceof AddonAbility) {
						sender.sendMessage(this.title(ability.getName(), color, " (Addon Combo)"));
						sender.sendMessage(ChatUtil.legacy(ability.getDescription()).colorIfAbsent(color));

						if (!ability.getInstructions().isEmpty()) {
							sender.sendMessage(ChatUtil.legacy(this.usage + ability.getInstructions()).colorIfAbsent(NamedTextColor.WHITE));
						}

						final AddonAbility abil = (AddonAbility) CoreAbility.getAbility(arg);
						sender.sendMessage(this.info("- By: ", color, abil.getAuthor()));
						sender.sendMessage(this.info("- Version: ", color, abil.getVersion()));
					} else {
						sender.sendMessage(this.title(ability.getName(), color, " (Combo)"));
						sender.sendMessage(ChatUtil.legacy(ComboManager.getDescriptions().get(combo)).colorIfAbsent(color));
						sender.sendMessage(ChatUtil.legacy(this.usage + ComboManager.getInstructions().get(combo)).colorIfAbsent(NamedTextColor.WHITE));
					}

					return;
				}
			}

			sender.sendMessage(ChatUtil.legacy(this.invalidTopic).colorIfAbsent(NamedTextColor.RED));
		}
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
		node.then(this.word("topic"));
	}

	/**
	 * The help text of an element, with its "/b display element" command highlighted in the sub color
	 */
	private Component elementHelp(final Element element) {
		final String command = "/b display " + element.getName().toLowerCase();
		return ChatUtil.legacy(this.elementHelp.get(element)).colorIfAbsent(element.getTextColor())
				.replaceText(TextReplacementConfig.builder().match(Pattern.compile("(?i)" + Pattern.quote(command))).replacement(Component.text(command, element.getSubTextColor())).build());
	}

	/**
	 * A bold ability name followed by a white label
	 */
	private Component title(final String name, final TextColor color, final String label) {
		return Component.text().append(Component.text(name, color, TextDecoration.BOLD), Component.text(label, NamedTextColor.WHITE)).build();
	}

	/**
	 * A colored label followed by a white value
	 */
	private Component info(final String label, final TextColor color, final String value) {
		return Component.text().append(Component.text(label, color), Component.text(value, NamedTextColor.WHITE)).build();
	}

	private static TextReplacementConfig literal(final String placeholder, final Component replacement) {
		return TextReplacementConfig.builder().matchLiteral(placeholder).replacement(replacement).build();
	}

	/**
	 * @return A legacy color code for the color, for config placeholders like {element_color}
	 */
	private static String code(final TextColor color) {
		return color == null ? "" : "&" + color.asHexString();
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (!args.isEmpty() || !sender.hasPermission("bending.command.help")) {
			return new ArrayList<>();
		}

		final List<String> list = new ArrayList<>();
		for (final Element e : Element.getAllElements()) {
			list.add(e.getName());
		}

		final List<String> abils = new ArrayList<>();
		for (final CoreAbility coreAbil : CoreAbility.getAbilities()) {
			if (!(sender instanceof Player) && (!coreAbil.isHiddenAbility()) && coreAbil.isEnabled() && !abils.contains(coreAbil.getName())) {
				abils.add(coreAbil.getName());
			} else if (sender instanceof Player) {
				if ((!coreAbil.isHiddenAbility()) && coreAbil.isEnabled() && !abils.contains(coreAbil.getName())) {
					abils.add(coreAbil.getName());
				}
			}
		}

		Collections.sort(abils);
		list.addAll(abils);
		return list;
	}
}
