package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.Preset;
import com.projectkorra.projectkorra.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Executor for /bending preset. Extends {@link PKCommand}.
 */
public class PresetCommand extends PKCommand {

	private static final String[] createaliases = { "create", "c", "save" };
	private static final String[] deletealiases = { "delete", "d", "del" };
	private static final String[] listaliases = { "list", "l" };
	private static final String[] bindaliases = { "bind", "b" };
	private static final String[] exportaliases = { "export", "e", "share" };
	private static final String[] importaliases = { "import", "i", "load" };

	public static final String INVALID_NAME = ".*[.,;:*'\"?=`<>+\\-\\[\\]{}^@!#$/\\\\%&()].*";

	private final String noPresets;
	private final String noPresetName;
	private final String deletePreset;
	private final String noPresetNameExternal;
	private final String bendingRemoved;
	private final String bound;
	private final String failedToBindAll;
	private final String databaseError;
	private final String bendingRemovedOther;
	private final String boundOtherConfirm;
	private final String succesfullyCopied;
	private final String reachedMax;
	private final String alreadyExists;
	private final String createdNewPreset;
	private final String cantEditBinds;
	private final String playerNotFound;
	private final String invalidName;
	private final String exportFailed;
	private final String exportSuccess;
	private final String importSuccess;
	private final String importFailed;
	private final String exportHoverCode;
	private final String exportHoverCommand;
	private final String exportCommandButton;
	private final String exportCodeButton;
	private final String exportNotSpecified;
	private final String importNotSpecified;

	public PresetCommand() {
		super("preset", "/bending preset <Bind/Create/Delete/List/Export/Import> [Preset]", ConfigManager.languageConfig.get().getString("Commands.Preset.Description"), new String[] { "preset", "presets", "pre", "set", "p" });

		this.noPresets = ConfigManager.languageConfig.get().getString("Commands.Preset.NoPresets");
		this.noPresetName = ConfigManager.languageConfig.get().getString("Commands.Preset.NoPresetName");
		this.deletePreset = ConfigManager.languageConfig.get().getString("Commands.Preset.Delete");
		this.noPresetNameExternal = ConfigManager.languageConfig.get().getString("Commands.Preset.External.NoPresetName");
		this.bendingRemoved = ConfigManager.languageConfig.get().getString("Commands.Preset.BendingPermanentlyRemoved");
		this.bound = ConfigManager.languageConfig.get().getString("Commands.Preset.SuccesfullyBound");
		this.failedToBindAll = ConfigManager.languageConfig.get().getString("Commands.Preset.FailedToBindAll");
		this.databaseError = ConfigManager.languageConfig.get().getString("Commands.Preset.DatabaseError");
		this.bendingRemovedOther = ConfigManager.languageConfig.get().getString("Commands.Preset.Other.BendingPermanentlyRemoved");
		this.boundOtherConfirm = ConfigManager.languageConfig.get().getString("Commands.Preset.Other.SuccesfullyBoundConfirm");
		this.succesfullyCopied = ConfigManager.languageConfig.get().getString("Commands.Preset.SuccesfullyCopied");
		this.reachedMax = ConfigManager.languageConfig.get().getString("Commands.Preset.MaxPresets");
		this.alreadyExists = ConfigManager.languageConfig.get().getString("Commands.Preset.AlreadyExists");
		this.createdNewPreset = ConfigManager.languageConfig.get().getString("Commands.Preset.Created");
		this.cantEditBinds = ConfigManager.languageConfig.get().getString("Commands.Preset.CantEditBinds");
		this.playerNotFound = ConfigManager.languageConfig.get().getString("Commands.Preset.PlayerNotFound");
		this.invalidName = ConfigManager.languageConfig.get().getString("Commands.Preset.InvalidName");
		this.exportSuccess = ConfigManager.languageConfig.get().getString("Commands.Preset.ExportSuccess");
		this.exportFailed = ConfigManager.languageConfig.get().getString("Commands.Preset.ExportFailed");
		this.importSuccess = ConfigManager.languageConfig.get().getString("Commands.Preset.ImportSuccess");
		this.importFailed = ConfigManager.languageConfig.get().getString("Commands.Preset.ImportFailed");
		this.exportHoverCode = ConfigManager.languageConfig.get().getString("Commands.Preset.HoverCode");
		this.exportHoverCommand = ConfigManager.languageConfig.get().getString("Commands.Preset.HoverCommand");
		this.exportCommandButton = ConfigManager.languageConfig.get().getString("Commands.Preset.ExportCommandButton");
		this.exportCodeButton = ConfigManager.languageConfig.get().getString("Commands.Preset.ExportCodeButton");
		this.exportNotSpecified = ConfigManager.languageConfig.get().getString("Commands.Preset.ExportNotSpecified");
		this.importNotSpecified = ConfigManager.languageConfig.get().getString("Commands.Preset.ImportNotSpecified");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.correctLength(sender, args.size(), 1, 3)) {
			return;
		} else if (sender instanceof Player && MultiAbilityManager.hasMultiAbilityBound((Player) sender)) {
			ChatUtil.sendBrandingMessage(sender, this.cantEditBinds);
			return;
		}

		Player target = null;

		int page = 1;
		String name = null;
		if (args.size() == 1 && !Arrays.asList(listaliases).contains(args.get(0))){
			this.help(sender, false);
		} else if (args.size() >= 2) {
			if (Arrays.asList(listaliases).contains(args.get(0))) {
				if (args.size() == 3) {
					target = Bukkit.getPlayer(args.get(1));
					if (target == null) {
						ChatUtil.sendBrandingMessage(sender, this.playerNotFound);
						return;
					}
					page = parseInt(args.get(2));
				} else {
					page = parseInt(args.get(1));
				}

			} else {
				name = args.get(1);
			}
		}

		// bending preset list.
		if (Arrays.asList(listaliases).contains(args.get(0)) && this.hasPermission(sender, "list")) {
			boolean firstMessage = true;

			if (target == null) {
				if (!isPlayer(sender)) return;

				target = (Player) sender;
			}

			final List<Preset> presets = Preset.presets.get(target.getUniqueId());
			final List<String> presetNames = new ArrayList<String>();

			if (presets == null || presets.isEmpty()) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresets);
				return;
			}

			for (final Preset preset : presets) {
				presetNames.add(preset.getName());
			}

			for (final String s : this.getPage(presetNames, ChatUtil.color("&6Presets: "), page, false)) {
				if (firstMessage) {
					ChatUtil.sendBrandingMessage(sender, s);
					firstMessage = false;
				} else {
					sender.sendMessage(ChatUtil.legacy(s).colorIfAbsent(NamedTextColor.YELLOW));
				}
			}
		} else if (Arrays.asList(deletealiases).contains(args.get(0)) && this.hasPermission(sender, "delete")) { // bending preset delete name.
			if (args.size() >= 3) {
				target = Bukkit.getPlayer(args.get(1));

				if (target == null) {
					ChatUtil.sendBrandingMessage(sender, this.playerNotFound);
					return;
				}
			} else {
				if (!isPlayer(sender)) return;

				target = (Player) sender;
			}

			if (!Preset.presetExists(target, name)) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresetName);
				return;
			}

			final Preset preset = Preset.getPreset(target, name);
			preset.delete().thenAccept(b -> {
				if (b) {
					ChatUtil.sendBrandingMessage(sender, "&a" + this.deletePreset.replace("{name}", "&e" + preset.getName() + "&a"));
				} else {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.databaseError.replace("{name}", "&e" + preset.getName() + "&c"));
				}
			}).exceptionally(e -> {
				e.printStackTrace();
				return null;
			});
		} else if (Arrays.asList(bindaliases).contains(args.get(0)) && this.hasPermission(sender, "bind")) { // bending preset bind name.
			if (args.size() < 3) {
				if (!isPlayer(sender)) return;
				Player player = (Player) sender;
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

				if (name == null) {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.invalidName);
				} else if (bPlayer.isPermaRemoved()) {
					ChatUtil.sendBrandingMessage(player, "&c" + this.bendingRemoved);
				} else if (Preset.presetExists(player, name)) {
					final Preset preset = Preset.getPreset(player, name);

					ChatUtil.sendBrandingMessage(sender, "&a" + this.bound.replace("{name}", "&e" + preset.getName() + "&a"));
					boolean boundAll = Preset.bindPreset(player, preset);

					if (!boundAll) {
						ChatUtil.sendBrandingMessage(sender, "&c" + this.failedToBindAll);
					}
				} else if (Preset.externalPresetExists(name) && this.hasPermission(sender, "bind.external")) {
					Preset.bindExternalPreset(player, name);
				} else if (!Preset.externalPresetExists(name) && this.hasPermission(sender, "bind.external")) {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresetNameExternal);
				} else {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresetName);
				}

			} else if (this.hasPermission(sender, "bind.external.assign") && Preset.externalPresetExists(name)) {
				if (!Preset.externalPresetExists(name)) {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresetNameExternal);
					return;
				}

				target = Bukkit.getPlayer(args.get(2));
				if (target != null && target.isOnline()) {
					BendingPlayer bPlayer2 = BendingPlayer.getBendingPlayer(target);

					if (bPlayer2.isPermaRemoved()) {
						ChatUtil.sendBrandingMessage(target, "&c" + this.bendingRemovedOther);
						return;
					}
					final boolean boundAll = Preset.bindExternalPreset(target, name);

					ChatUtil.sendBrandingMessage(sender, "&a" + this.boundOtherConfirm.replace("{target}", "&e" + target.getName() + "&a").replace("{name}", "&e" + name + "&a" + "&e"));
					ChatUtil.sendBrandingMessage(target, "&a" + this.bound.replace("{name}", "&e" + name + "&a"));
					if (!boundAll) {
						ChatUtil.sendBrandingMessage(target, "&c" + this.failedToBindAll);
					}
				} else {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.playerNotFound);
				}
			} else if (this.hasPermission(sender, "bind.assign")) {
				target = Bukkit.getPlayer(args.get(2));

				if (target != null && target.isOnline()) {
					BendingPlayer bPlayer2 = BendingPlayer.getBendingPlayer(target);

					if (bPlayer2.isPermaRemoved()) {
						ChatUtil.sendBrandingMessage(target, "&c" + this.bendingRemovedOther);
						return;
					}
					else if (!Preset.presetExists(target, name)) {
						ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresetName);
						return;
					}
					final Preset preset = Preset.getPreset(target, name);
					final boolean boundAll = Preset.bindPreset(target, preset);

					ChatUtil.sendBrandingMessage(sender, "&a" + this.boundOtherConfirm.replace("{target}", "&e" + target.getName() + "&a").replace("{name}", "&e" + name + "&a" + "&e"));
					ChatUtil.sendBrandingMessage(target, "&a" + this.succesfullyCopied.replace("{target}", "&e" + target.getName() + "&a"));
					if (!boundAll) {
						ChatUtil.sendBrandingMessage(target, "&c" + this.failedToBindAll);
					}
				} else {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.playerNotFound);
				}
			}
		} else if (Arrays.asList(createaliases).contains(args.get(0)) && this.hasPermission(sender, "create")) { // bending preset create name.
			if (!isPlayer(sender)) return;
			target = (Player) sender;
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(target);

			final int limit = GeneralMethods.getMaxPresets(target);
			if (name == null || name.matches(INVALID_NAME)) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.invalidName);
				return;
			}
			if (Preset.presets.get(target.getUniqueId()) != null && Preset.presets.get(target.getUniqueId()).size() >= limit) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.reachedMax);
				return;
			} else if (Preset.presetExists(target, name)) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.alreadyExists);
				return;
			}

			if (bPlayer == null) {
				return;
			}
			final HashMap<Integer, String> abilities = (HashMap<Integer, String>) bPlayer.getAbilities().clone();

			final Preset preset = new Preset(target.getUniqueId(), name, abilities);
			final String finalName = name;
			preset.save(target).thenAccept(b -> {
				if (b) {
					ChatUtil.sendBrandingMessage(sender, "&a" + this.createdNewPreset.replace("{name}", "&e" + finalName + "&a"));
				} else {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.databaseError.replace("{name}", "&e" + finalName + "&c"));
				}
			});
		} else if (Arrays.asList(exportaliases).contains(args.get(0)) && this.hasPermission(sender, "export")) { // bending preset export name.
			if (!isPlayer(sender)) return;
			Player player = (Player) sender;

			if (args.size() < 2) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.exportNotSpecified);
				return;
			}

			name = args.get(1);
			if (!Preset.presetExists(player, name)) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.noPresetName);
				return;
			}

			Preset preset = Preset.getPreset(player, name);
			String exportCode = preset.exportToString();
			if (exportCode != null) {
				String message = this.exportSuccess
						.replace("{name}", "&e" + name + "&a")
						.replace("{code}", "&b" + exportCode + "&a");
				ChatUtil.sendBrandingMessage(sender, "&a" + message);
				final Component copyComponent = ChatUtil.legacy(this.exportCodeButton).colorIfAbsent(NamedTextColor.AQUA)
						.clickEvent(ClickEvent.copyToClipboard(exportCode))
						.hoverEvent(HoverEvent.showText(ChatUtil.legacy(this.exportHoverCode.replace("{code}", exportCode)).colorIfAbsent(NamedTextColor.YELLOW)));
				ChatUtil.sendBrandingMessage(sender, copyComponent);
				final Component copyImportComponent = ChatUtil.legacy(this.exportCommandButton).colorIfAbsent(NamedTextColor.AQUA)
						.clickEvent(ClickEvent.copyToClipboard("/bending preset import " + exportCode))
						.hoverEvent(HoverEvent.showText(ChatUtil.legacy(this.exportHoverCommand).colorIfAbsent(NamedTextColor.YELLOW)));
				ChatUtil.sendBrandingMessage(sender, copyImportComponent);
			} else {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.exportFailed);
			}
		}
		else if (Arrays.asList(importaliases).contains(args.get(0)) && this.hasPermission(sender, "import")) { // bending preset import key.
			if (!isPlayer(sender)) return;
			Player player = (Player) sender;

			if (args.size() < 2) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.importNotSpecified);
				return;
			}

			String importCode = args.get(1);
			Preset importedPreset = Preset.importFromString(player, importCode);

			if (importedPreset == null) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.importFailed);
				return;
			}

			int limit = GeneralMethods.getMaxPresets(player);
			if (Preset.presets.get(player.getUniqueId()) != null &&
					Preset.presets.get(player.getUniqueId()).size() >= limit) {
				ChatUtil.sendBrandingMessage(sender, "&c" + this.reachedMax);
				return;
			}

			importedPreset.save(player).thenAccept(b -> {
				if (b) {
					ChatUtil.sendBrandingMessage(sender, "&a" + this.importSuccess
							.replace("{name}", "&e" + importedPreset.getName() + "&a"));
				} else {
					ChatUtil.sendBrandingMessage(sender, "&c" + this.databaseError
							.replace("{name}", "&e" + importedPreset.getName() + "&c"));
				}
			});
		} else {
			this.help(sender, false);
		}
	}

	/**
	 * Players are given the permissions of the individual sub commands rather than bending.command.preset
	 */
	@Override
	public boolean canUse(final CommandSender sender) {
		if (super.canUse(sender)) {
			return true;
		}
		for (final String sub : new String[] { "list", "create", "delete", "bind", "export", "import" }) {
			if (sender.hasPermission("bending.command.preset." + sub)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
		this.literals(node, listaliases, list -> list.then(this.word("pageOrPlayer").then(this.word("page")))); // list [page] or list <player> <page>
		this.literals(node, deletealiases, delete -> delete.then(this.word("preset").then(this.word("player"))));
		this.literals(node, bindaliases, bind -> bind.then(this.word("preset").then(this.word("player"))));
		this.literals(node, createaliases, create -> create.then(this.word("preset")));
		this.literals(node, exportaliases, export -> export.then(this.word("preset")));
		this.literals(node, importaliases, imp -> imp.then(this.greedy("code"))); // Base64, so it can contain '/', '+' and '='
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 3 || !this.canUse(sender) || !(sender instanceof Player)) {
			return new ArrayList<>();
		}
		final List<String> l = new ArrayList<>();
		if (args.size() == 0) {
			l.add("create");
			l.add("delete");
			l.add("list");
			l.add("bind");
			l.add("import");
			l.add("export");
			return l;
		} else if (args.size() <= 1 && (Arrays.asList(new String[] { "delete", "d", "del", "bind", "b", "e", "export", "share" }).contains(args.get(0).toLowerCase()))) {
			final List<Preset> presets = Preset.presets.get(((Player) sender).getUniqueId());
			final List<String> presetNames = new ArrayList<>();
			if (presets != null && presets.size() != 0) {
				presets.stream().map(Preset::getName).forEach(presetNames::add);
			}
			//Add external presets if they are binding
			if (sender.hasPermission("bending.command.preset.bind.external")
					&& Arrays.asList(new String[] { "bind", "b" }).contains(args.get(0).toLowerCase())) {
				if (Preset.externalPresets.keySet().size() > 0) {
					presetNames.addAll(Preset.externalPresets.keySet());
				}
			}
			return presetNames;
		}
		return l;
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
