package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.ProjectKorra;

public class Commands {

	private final ProjectKorra plugin;

	public static Set<String> invincible = new HashSet<String>();
	public static boolean debugEnabled = false;
	public static boolean isToggledForAll = false;

	public Commands(final ProjectKorra plugin) {
		this.plugin = plugin;
		debugEnabled = ProjectKorra.plugin.getConfig().getBoolean("debug");
		this.init();
	}

	// Element Aliases.
	public static String[] airaliases = { "air", "a", "airbending", "airbender" };
	public static String[] chialiases = { "chi", "c", "chiblocking", "chiblocker" };
	public static String[] earthaliases = { "earth", "e", "earthbending", "earthbender" };
	public static String[] firealiases = { "fire", "f", "firebending", "firebender" };
	public static String[] wateraliases = { "water", "w", "waterbending", "waterbender" };
	public static String[] elementaliases = { "air", "a", "airbending", "airbender", "chi", "c", "chiblocking", "chiblocker", "earth", "e", "earthbending", "earthbender", "fire", "f", "firebending", "firebender", "water", "w", "waterbending", "waterbender" };
	public static String[] avataraliases = { "avatar", "av", "avy", "aang", "korra" };

	// Combo Aliases.
	public static String[] aircomboaliases = { "aircombo", "ac", "aircombos", "airbendingcombos" };
	public static String[] chicomboaliases = { "chicombo", "cc", "chicombos", "chiblockingcombos", "chiblockercombos" };
	public static String[] earthcomboaliases = { "earthcombo", "ec", "earthcombos", "earthbendingcombos" };
	public static String[] firecomboaliases = { "firecombo", "fc", "firecombos", "firebendingcombos" };
	public static String[] watercomboaliases = { "watercombo", "wc", "watercombos", "waterbendingcombos" };

	public static String[] comboaliases = { "aircombo", "ac", "aircombos", "airbendingcombos", "chicombo", "cc", "chicombos", "chiblockingcombos", "chiblockercombos", "earthcombo", "ec", "earthcombos", "earthbendingcombos", "firecombo", "fc", "firecombos", "firebendingcombos", "watercombo", "wc", "watercombos", "waterbendingcombos" };

	// Passive Aliases.
	public static String[] passivealiases = { "airpassive", "ap", "airpassives", "airbendingpassives", "chipassive", "cp", "chipassives", "chiblockingpassives", "chiblockerpassives", "earthpassive", "ep", "earthpassives", "earthbendingpassives", "firepassive", "fp", "firepassives", "firebendingpassives", "waterpassive", "wp", "waterpassives", "waterbendingpassives" };

	// Subelement Aliases.
	public static String[] subelementaliases = { "flight", "fl", "spiritualprojection", "sp", "spiritual", "bloodbending", "bb", "healing", "heal", "icebending", "ice", "ib", "plantbending", "plant", "metalbending", "mb", "metal", "lavabending", "lb", "lava", "sandbending", "sb", "sand", "combustionbending", "combustion", "cb", "lightningbending", "lightning" };

	// Air.
	public static String[] flightaliases = { "flight", "fl" };
	public static String[] spiritualprojectionaliases = { "spiritualprojection", "sp", "spiritual" };

	// Water.
	public static String[] bloodaliases = { "bloodbending", "bb" };
	public static String[] healingaliases = { "healing", "heal" };
	public static String[] icealiases = { "icebending", "ice", "ib" };
	public static String[] plantaliases = { "plantbending", "plant" };

	// Earth.
	public static String[] metalbendingaliases = { "metalbending", "mb", "metal" };
	public static String[] lavabendingaliases = { "lavabending", "lb", "lava" };
	public static String[] sandbendingaliases = { "sandbending", "sb", "sand" };

	// Fire.
	public static String[] combustionaliases = { "combustionbending", "combustion", "cb" };
	public static String[] lightningaliases = { "lightningbending", "lightning" };

	// Miscellaneous.
	public static String[] commandaliases = { "b", "pk", "projectkorra", "bending", "mtla", "tla", "korra", "bend" };

	private void init() {
		/**
		 * Set of all of the Classes which extend Command
		 */
		initializeCommands();

		this.plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
			final List<String> aliases = new ArrayList<>(Arrays.asList(commandaliases));
			aliases.remove("projectkorra");
			event.registrar().register(buildRoot(), "The ProjectKorra bending command", aliases);
		});
	}

	/**
	 * Builds the Brigadier tree for /projectkorra. Every command registered so far gets its own typed node.
	 * Anything else (including addon commands registered after the tree was built) goes through a catch-all
	 * argument that dispatches the same way the old Bukkit command did.
	 */
	private static LiteralCommandNode<CommandSourceStack> buildRoot() {
		final LiteralArgumentBuilder<CommandSourceStack> root = io.papermc.paper.command.brigadier.Commands.literal("projectkorra")
				.executes(context -> {
					PKCommand.instances.get("help").execute(PKCommand.sender(context.getSource()), new ArrayList<>());
					return Command.SINGLE_SUCCESS;
				});

		final Set<String> registered = new HashSet<>();
		for (final PKCommand command : PKCommand.instances.values()) {
			for (final String alias : command.getAliases()) {
				if (registered.add(alias.toLowerCase())) {
					root.then(command.createNode(alias.toLowerCase()));
				} else {
					ProjectKorra.log.warning("The command alias '" + alias + "' of /bending " + command.getName() + " is already used by another command.");
				}
			}
		}

		root.then(io.papermc.paper.command.brigadier.Commands.argument("args", StringArgumentType.greedyString())
				.suggests(Commands::suggestFallback)
				.executes(context -> {
					dispatch(PKCommand.sender(context.getSource()), PKCommand.split(StringArgumentType.getString(context, "args")));
					return Command.SINGLE_SUCCESS;
				}));
		return root.build();
	}

	/**
	 * Runs the sub command named by the first argument, or shows the help if there is none
	 */
	private static void dispatch(final CommandSender sender, final List<String> args) {
		if (!args.isEmpty()) {
			final PKCommand command = getCommand(args.getFirst());
			if (command != null) {
				command.execute(sender, args.subList(1, args.size()));
				return;
			}
		}
		PKCommand.instances.get("help").execute(sender, new ArrayList<>());
	}

	/**
	 * Suggestions for the catch-all argument, matching the old tab completer
	 */
	private static CompletableFuture<Suggestions> suggestFallback(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
		final CommandSender sender = PKCommand.sender(context.getSource());
		final String remaining = builder.getRemaining();
		final String[] args = remaining.split(" ", -1);
		final SuggestionsBuilder offset = builder.createOffset(builder.getStart() + remaining.lastIndexOf(' ') + 1);

		if (args.length == 1) {
			BendingTabComplete.getPossibleCompletions(args, BendingTabComplete.getCommandsForUser(sender)).forEach(offset::suggest);
		} else {
			final PKCommand command = getCommand(args[0]);
			if (command != null && command.canUse(sender)) {
				final List<String> previous = PKCommand.split(String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)));
				BendingTabComplete.getPossibleCompletions(args, command.getTabCompletion(sender, previous)).forEach(offset::suggest);
			}
		}
		return offset.buildFuture();
	}

	private static PKCommand getCommand(final String alias) {
		for (final PKCommand command : PKCommand.instances.values()) {
			if (Arrays.asList(command.getAliases()).contains(alias.toLowerCase())) {
				return command;
			}
		}
		return null;
	}

	/**
	 * Create all the command instances. In a package level method so this can be called
	 * when the plugin is reloaded
	 */
	static void initializeCommands() {
		new AddCommand();
		new BindCommand();
		new CheckCommand();
		new BoardCommand();
		new ChooseCommand();
		new ClearCommand();
		new CopyCommand();
		new DebugCommand();
		new DisplayCommand();
		new HelpCommand();
		new InvincibleCommand();
		new PermaremoveCommand();
		new PresetCommand();
		new ReloadCommand();
		new RemoveCommand();
		new StatsCommand();
		new ToggleCommand();
		new VersionCommand();
		new WhoCommand();
		new CooldownCommand();
		new TempCommand();
	}
}
