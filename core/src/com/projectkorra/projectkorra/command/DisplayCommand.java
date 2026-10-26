package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager;
import com.projectkorra.projectkorra.ability.util.PassiveManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Executor for /bending display. Extends {@link PKCommand}.
 */
public class DisplayCommand extends PKCommand {

	private static final Map<String, String> abbreviations = new HashMap<>();
	private final String noCombosAvailable;
	private final String noPassivesAvailable;
	private final String noAbilitiesAvailable;
	private final String noCombosAccess;
	private final String noPassivesAccess;
	private final String noAbilitiesAccess;
	private final String invalidArgument;
	private final String playersOnly;
	private final String noBinds;
	private final String format;
	private final String separator;
	private final String hoverType;
	private final String hoverAbility;
	private final String subHeader;
	private final String comboPassiveHeader;

	public DisplayCommand() {
		super("display", "/bending display <Element>", ConfigManager.languageConfig.get().getString("Commands.Display.Description"), new String[] { "display", "dis", "d" });

		this.noCombosAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAvailable");
		this.noPassivesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAvailable");
		this.noAbilitiesAvailable = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAvailable");
		this.noCombosAccess = ConfigManager.languageConfig.get().getString("Commands.Display.NoCombosAccess");
		this.noPassivesAccess = ConfigManager.languageConfig.get().getString("Commands.Display.NoPassivesAccess");
		this.noAbilitiesAccess = ConfigManager.languageConfig.get().getString("Commands.Display.NoAbilitiesAccess");
		this.invalidArgument = ConfigManager.languageConfig.get().getString("Commands.Display.InvalidArgument");
		this.playersOnly = ConfigManager.languageConfig.get().getString("Commands.Display.PlayersOnly");
		this.noBinds = ConfigManager.languageConfig.get().getString("Commands.Display.NoBinds");
		this.format = ConfigManager.languageConfig.get().getString("Commands.Display.Format");
		this.separator = ConfigManager.languageConfig.get().getString("Commands.Display.Separator").replaceAll("\\\\n", "\n");
		this.hoverType = ConfigManager.languageConfig.get().getString("Commands.Display.HoverType");
		this.hoverAbility = ConfigManager.languageConfig.get().getString("Commands.Display.HoverAbility");
		this.subHeader = ConfigManager.languageConfig.get().getString("Commands.Display.SubHeader").replaceAll("\\\\n", "\n");
		this.comboPassiveHeader = ConfigManager.languageConfig.get().getString("Commands.Display.ComboPassiveHeader").replaceAll("\\\\n", "\n");
		this.fillAbbreviations();
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}

		// bending display [Element].
		if (args.size() == 1) {
			String elementName = args.get(0).toLowerCase().replace("bending", "");
			elementName = abbreviations.getOrDefault(elementName, elementName);
			
			final Element element = Element.fromString(elementName.replace("combos", "").replace("combo", "").replace("passives", "").replace("passive", ""));
			final TextColor color = element == null ? null : element.getTextColor();
			// Combos.
			if (elementName.contains("combo")) {
				if (element == null) {
					final List<String> allCombos = new ArrayList<>();
					final List<String> allPermittedCombos = new ArrayList<>();
					for (final Element e : Element.getAllElements()) {
						final List<String> combos = this.getCombos(e);
						final List<String> permittedCombos = this.filterAbilities(sender, combos);
						allPermittedCombos.addAll(permittedCombos);
						allCombos.addAll(combos);
					}
					
					if (allPermittedCombos.isEmpty()) {
						ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy((allCombos.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", "")).colorIfAbsent(NamedTextColor.GOLD));
						return;
					}
					
					sender.sendMessage(Component.text("Combos", null, TextDecoration.BOLD));
					this.iterateAbilities(sender, allPermittedCombos);
					return;
				}
				
				final List<String> allCombos = this.getCombos(element);
				final List<String> combos = this.filterAbilities(sender, allCombos);
				
				if (combos.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy((allCombos.isEmpty() ? this.noCombosAvailable : this.noCombosAccess).replace("{element}", element.getName())).colorIfAbsent(color));
					return;
				}
				
				sender.sendMessage(Component.text(element.getName() + element.getType().getBending() + " Combos", color, TextDecoration.BOLD));
				this.iterateAbilities(sender, combos);
				return;
				// Passives.
			} else if (elementName.contains("passive")) {
				if (element == null) {
					final List<String> allPassives = new ArrayList<>();
					final List<String> allPermittedPassives = new ArrayList<>();
					for (final Element e : Element.getAllElements()) {
						final Set<String> passives = PassiveManager.getPassivesForElement(e);
						final List<String> permittedPassives = this.filterAbilities(sender, passives);
						allPermittedPassives.addAll(permittedPassives);
						allPassives.addAll(passives);
					}
					
					if (allPermittedPassives.isEmpty()) {
						ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy((allPassives.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", "")).colorIfAbsent(NamedTextColor.GOLD));
						return;
					}
					
					sender.sendMessage(Component.text("Passives", null, TextDecoration.BOLD));
					this.iterateAbilities(sender, allPermittedPassives);
					return;
				}
				
				final Set<String> allPassives = PassiveManager.getPassivesForElement(element);
				final List<String> passives = this.filterAbilities(sender, allPassives);

				if (passives.isEmpty()) {
					ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy((allPassives.isEmpty() ? this.noPassivesAvailable : this.noPassivesAccess).replace("{element}", element.getName())).colorIfAbsent(color));
					return;
				}

				sender.sendMessage(Component.text(element.getName() + element.getType().getBending() + " Passives", color, TextDecoration.BOLD));
				this.iterateAbilities(sender, passives);
				return;
			} else if (element != null) {
				this.displayElement(sender, element);
			} else {
				final TextComponent.Builder elements = Component.text().append(ChatUtil.legacy(this.invalidArgument).colorIfAbsent(NamedTextColor.RED), Component.text("\nElements: ", NamedTextColor.WHITE));
				boolean first = true;

				for (final Element e : Element.getAllElements()) {
					if (e instanceof SubElement || (sender instanceof Player && !sender.hasPermission("bending." + e.getName()))) {
						continue;
					}

					if (!first) {
						elements.append(Component.text(" | ", NamedTextColor.WHITE));
					}
					first = false;

					elements.append(Component.text(e.getName(), e.getTextColor())
							.hoverEvent(this.hoverEvent(code(e.getTextColor()) + this.hoverType.replace("{type}", e.getName())))
							.clickEvent(ClickEvent.runCommand("/bending display " + e.getName())));
				}

				final TextComponent.Builder subelements = Component.text().append(Component.text("SubElements: ", NamedTextColor.WHITE));
				first = true;
				for (final SubElement e : Element.getAllSubElements()) {
					if (sender instanceof Player && !sender.hasPermission("bending." + e.getParentElement().getName() + "." + e.getName())) {
						continue;
					}

					if (!first) {
						subelements.append(Component.text(" | ", NamedTextColor.WHITE));
					}
					first = false;

					subelements.append(Component.text(e.getName(), e.getTextColor())
							.hoverEvent(this.hoverEvent(code(e.getTextColor()) + this.hoverType.replace("{type}", e.getName())))
							.clickEvent(ClickEvent.runCommand("/bending display " + e.getName())));
				}

				sender.sendMessage(elements.build());
				sender.sendMessage(subelements.build());
			}
		}
		
		if (args.isEmpty()) {
			// Bending Display.
			if (!(sender instanceof Player)) {
				ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy(this.playersOnly).colorIfAbsent(NamedTextColor.RED));
				return;
			}
			
			this.displayBinds(sender);
		}
	}

	/**
	 * Iterates over a given list of Abilities (Moves, Passives, Combos) and sends them
	 *
	 * @param sender The CommandSender to show the abilities to
	 * @param abilityNames The list to iterate through
	 */
	private void iterateAbilities(final CommandSender sender, final Collection<String> abilityNames) {
		if (abilityNames.isEmpty()) {
			return;
		}
		
		final TextComponent.Builder messageBuilder = Component.text();
		boolean first = true;
		for (String abilityName : abilityNames) {
			final CoreAbility coreAbil = CoreAbility.getAbility(abilityName);
			if (coreAbil == null) {
				continue;
			}
			
			if (!first) {
				messageBuilder.append(ChatUtil.legacy(this.separator));
			}
			first = false;

			final TextColor color = coreAbil.getElement().getTextColor();
			final TextColor subColor = coreAbil.getElement() instanceof SubElement || coreAbil.getElement() == Element.AVATAR ? color : coreAbil.getElement().getSubTextColor();
			
			messageBuilder.append(ChatUtil.legacy(this.format.replace("{ability}", code(color) + abilityName))
					.hoverEvent(this.hoverEvent(code(color) + this.hoverAbility.replace("{ability}", code(subColor) + abilityName)))
					.clickEvent(ClickEvent.runCommand("/bending help " + coreAbil.getName())));
		}
		sender.sendMessage(messageBuilder.build());
	}

	/**
	 * Displays the enabled moves and subelements for the given element to the CommandSender.
	 *
	 * @param sender The CommandSender to show the moves to
	 * @param element The element to show the moves for
	 */
	private void displayElement(final CommandSender sender, final Element element) {
		final List<String> allAbilities = this.getAbilities(element);
		final List<String> abilities = this.filterAbilities(sender, allAbilities);
		final Set<String> allPassives = PassiveManager.getPassivesForElement(element);
		final List<String> passives = this.filterAbilities(sender, allPassives);
		final List<String> allCombos = this.getCombos(element);
		final List<String> combos = this.filterAbilities(sender, allCombos);
		final String elementName = element.getName();
		final String bending = element.getType().getBending();
		final TextColor mainColor = element.getTextColor();
		final TextColor subColor = element instanceof SubElement || element == Element.AVATAR ? mainColor : element.getSubTextColor();

		if (abilities.isEmpty() && combos.isEmpty() && passives.isEmpty()) {
			sender.sendMessage(ChatUtil.legacy("&e" + (allAbilities.isEmpty() && allCombos.isEmpty() && allPassives.isEmpty() ? this.noAbilitiesAvailable : this.noAbilitiesAccess).replace("{element}", code(mainColor) + elementName + "&e")));
			return;
		}
		
		sender.sendMessage(Component.text(elementName + bending, mainColor, TextDecoration.BOLD));
		
		this.iterateAbilities(sender, abilities);

		if (!(combos.isEmpty() || passives.isEmpty()) && !ChatUtil.stripColor(this.comboPassiveHeader).isEmpty()) {
			sender.sendMessage(this.header(this.comboPassiveHeader, elementName, mainColor));
		}

		//Display the number of Passives and Combos
		if (!combos.isEmpty()) {
			sender.sendMessage(Component.text(elementName + " Combos (#)".replace("#", String.valueOf(combos.size())), subColor)
					.hoverEvent(this.hoverEvent(code(mainColor) + this.hoverType.replace("{type}", code(subColor) + elementName + "Combos")))
					.clickEvent(ClickEvent.runCommand("/bending display " + elementName + "Combos")));
		}
		
		if (!passives.isEmpty()) {
			sender.sendMessage(Component.text(elementName + " Passives (#)".replace("#", String.valueOf(passives.size())), subColor)
					.hoverEvent(this.hoverEvent(code(mainColor) + this.hoverType.replace("{type}", code(subColor) + elementName + "Passives")))
					.clickEvent(ClickEvent.runCommand("/bending display " + elementName + "Passives")));
		}

		// Display the subelements and the number of their abilities
		if (Element.getSubElements(element).length > 0) {
			final TextComponent.Builder message = Component.text();
			boolean first = true;
			for (final SubElement sub : Element.getSubElements(element)) {
				final int count = this.filterAbilities(sender, this.getAbilities(sub)).size();
				if ((!(sender instanceof Player) || sender.hasPermission("bending." + elementName.toLowerCase() + "." + sub.getName().toLowerCase() + sub.getType().getBending())) && count > 0) {
					final TextColor color = sub.getTextColor();
					final String name = sub.getName();
					
					if (!first) {
						message.append(ChatUtil.legacy(this.separator));
					}
					first = false;
					
					message.append(Component.text(name + " Abilities (#)".replace("#", String.valueOf(count)), color)
							.hoverEvent(this.hoverEvent(code(color) + this.hoverType.replace("{type}", code(color) + name)))
							.clickEvent(ClickEvent.runCommand("/bending display " + name)));
				}
			}
			if (!first) {
				if (!ChatUtil.stripColor(this.subHeader).isEmpty()) {
					sender.sendMessage(this.header(this.subHeader, elementName, mainColor));
				}
				sender.sendMessage(message.build());
			}
		}
	}

	/**
	 * Displays a Player's bound abilities.
	 *
	 * @param sender The CommandSender to output the bound abilities to
	 */
	private void displayBinds(final CommandSender sender) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		final HashMap<Integer, String> abilities = bPlayer.getAbilities();

		if (abilities.isEmpty()) {
			sender.sendMessage(ChatUtil.legacy(this.noBinds).colorIfAbsent(NamedTextColor.RED));
			return;
		}

		sender.sendMessage(Component.text("Abilities", NamedTextColor.WHITE, TextDecoration.BOLD));

		for (int i = 1; i <= 9; i++) {
			final String ability = abilities.get(i);
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil != null && !ability.equalsIgnoreCase("null")) {
				sender.sendMessage(Component.text(i + ". ").append(Component.text(ability, coreAbil.getElement().getTextColor())));
			}
		}
	}
	
	private List<String> filterAbilities(final CommandSender sender, final Collection<String> abilities) {
		final List<String> filteredAbilities = new ArrayList<>();
		for (String ability : abilities) {
			final CoreAbility coreAbil = CoreAbility.getAbility(ability);
			if (coreAbil == null || filteredAbilities.contains(coreAbil.getName()) || !coreAbil.isEnabled() || (!(coreAbil instanceof PassiveAbility) && coreAbil.isHiddenAbility()) || (sender instanceof Player && !sender.hasPermission("bending.ability." + coreAbil.getName()))) {
				continue;
			}
			filteredAbilities.add(coreAbil.getName());
		}
		return filteredAbilities;
	}
	
	private List<String> getAbilities(final Element element) {
		final List<String> abilities = new ArrayList<>();
		for (CoreAbility coreAbil : CoreAbility.getAbilitiesByElement(element)) {
			if (!coreAbil.getElement().equals(element) || !coreAbil.isEnabled()|| coreAbil.isHiddenAbility() || abilities.contains(coreAbil.getName()) || coreAbil instanceof ComboAbility || coreAbil instanceof PassiveAbility) {
				continue;
			}
			abilities.add(coreAbil.getName());
		}
		return abilities;
	}
	
	private List<String> getCombos(final Element element) {
		if (element instanceof SubElement) {
			final List<String> combos = new ArrayList<>();
			for (CoreAbility coreAbil : CoreAbility.getAbilitiesByElement(element)) {
				if (!(coreAbil instanceof ComboAbility) || !coreAbil.isEnabled() || coreAbil.isHiddenAbility() || combos.contains(coreAbil.getName())) {
					continue;
				}
				combos.add(coreAbil.getName());
			}
			return combos;
		} else {
			return ComboManager.getCombosForElement(element);
		}
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (!args.isEmpty() || !sender.hasPermission("bending.command.display")) {
			return new ArrayList<>();
		}
		final List<String> list = new ArrayList<>();
		
		for (final Element e : Element.getAllElements()) {
			if (filterAbilities(sender, getAbilities(e)).size() > 0) {
				list.add(e.getName());
			}
			for (final SubElement s : Element.getSubElements(e)) {
				if (filterAbilities(sender, getAbilities(s)).size() > 0) {
					list.add(s.getName());
				}
			}
		}

		for (String elementName : new ArrayList<>(list)) {
			if (filterAbilities(sender, getCombos(Element.getElement(elementName))).size() > 0)
				list.add(elementName + "Combos");
			if (filterAbilities(sender, PassiveManager.getPassivesForElement(Element.getElement(elementName))).size() > 0)
				list.add(elementName + "Passives");
		}

		return list;
	}
	
	private void fillAbbreviations() {
		abbreviations.put("ac", "aircombo");
		abbreviations.put("ap", "airpassive");
		abbreviations.put("avc", "avatarcombo");
		abbreviations.put("avp", "avatarpassive");
		abbreviations.put("cc", "chicombo");
		abbreviations.put("cp", "chipassive");
		abbreviations.put("ec", "earthcombo");
		abbreviations.put("ep", "earthpassive");
		abbreviations.put("fc", "firecombo");
		abbreviations.put("fp", "firepassive");
		abbreviations.put("wc", "watercombo");
		abbreviations.put("wp", "waterpassive");
	}
	
	public void addAbbreviation(String abbreviated, String unAbbreviated) {
		abbreviations.put(abbreviated, unAbbreviated);
	}
	
	private HoverEvent<Component> hoverEvent(final String string) {
		return HoverEvent.showText(ChatUtil.legacy(string));
	}

	/**
	 * A header from the config, with {element} and {element_color} filled in
	 */
	private Component header(final String header, final String elementName, final TextColor color) {
		return ChatUtil.legacy(code(color) + header.replaceAll("\\{(?i)element}", elementName).replaceAll("\\{(?i)element_?color}", code(color)));
	}

	/**
	 * @return A legacy color code for the color, for config placeholders like {element_color}
	 */
	private static String code(final TextColor color) {
		return color == null ? "" : "&" + color.asHexString();
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
		node.then(this.word("element"));
	}
}