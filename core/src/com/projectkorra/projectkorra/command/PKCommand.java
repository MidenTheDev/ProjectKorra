package com.projectkorra.projectkorra.command;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.ChatUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract representation of a command executor. Implements {@link SubCommand}.
 *
 * @author kingbirdy
 *
 */
public abstract class PKCommand implements SubCommand {

	protected String noPermissionMessage, mustBePlayerMessage;

	/**
	 * The full name of the command.
	 */
	private final String name;
	/**
	 * The proper use of the command, in the form '/b {@link PKCommand#name
	 * name} arg1 arg2 ... '
	 */
	private final String properUse;
	/**
	 * A description of what the command does.
	 */
	private final String description;
	/**
	 * String[] of all possible aliases of the command.
	 */
	private final String[] aliases;
	/**
	 * List of all command executors which extends PKCommand
	 */
	public static Map<String, PKCommand> instances = new HashMap<String, PKCommand>();

	public PKCommand(final String name, final String properUse, final String description, final String[] aliases) {
		this.name = name;
		this.properUse = properUse;
		this.description = description;
		this.aliases = aliases;

		this.noPermissionMessage = ChatUtil.color("&c" + ConfigManager.languageConfig.get().getString("Commands.NoPermission"));
		this.mustBePlayerMessage = ChatUtil.color("&c" + ConfigManager.languageConfig.get().getString("Commands.MustBePlayer"));

		instances.put(name, this);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getProperUse() {
		return this.properUse;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String[] getAliases() {
		return this.aliases;
	}

	@Override
	public void help(final CommandSender sender, final boolean description) {
		final String message = ConfigManager.languageConfig.get().getString("Commands.ProperUsage").replace("{command}", "&3" + this.properUse + "&6");
		sender.sendMessage(ChatUtil.legacy(message).colorIfAbsent(NamedTextColor.GOLD));
		if (description) {
			sender.sendMessage(ChatUtil.legacy(this.description).colorIfAbsent(NamedTextColor.YELLOW));
		}
	}

	/**
	 * Checks if the {@link CommandSender} has permission to execute the
	 * command. The permission is in the format 'bending.command.
	 * {@link PKCommand#name name}'. If not, they are told so.
	 *
	 * @param sender The CommandSender to check
	 * @return True if they have permission, false otherwise
	 */
	protected boolean hasPermission(final CommandSender sender) {
		if (sender.hasPermission("bending.command." + this.name)) {
			return true;
		} else {
			sender.sendMessage(ChatUtil.legacy(this.noPermissionMessage));
			return false;
		}
	}

	/**
	 * Checks if the {@link CommandSender} has permission to execute the
	 * command. The permission is in the format 'bending.command.
	 * {@link PKCommand#name name}.extra'. If not, they are told so.
	 *
	 * @param sender The CommandSender to check
	 * @param extra The additional node to check
	 * @return True if they have permission, false otherwise
	 */
	protected boolean hasPermission(final CommandSender sender, final String extra) {
		if (sender.hasPermission("bending.command." + this.name + "." + extra)) {
			return true;
		} else {
			ChatUtil.sendBrandingMessage(sender, this.noPermissionMessage);
			return false;
		}
	}

	/**
	 * Checks if the argument length is within certain parameters, and if not,
	 * informs the CommandSender of how to correctly use the command.
	 *
	 * @param sender The CommandSender who issued the command
	 * @param size The length of the arguments list
	 * @param min The minimum acceptable number of arguments
	 * @param max The maximum acceptable number of arguments
	 * @return True if min &lt; size &lt; max, false otherwise
	 */
	protected boolean correctLength(final CommandSender sender, final int size, final int min, final int max) {
		if (size < min || size > max) {
			this.help(sender, false);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Checks if the CommandSender is an instance of a Player. If not, it tells
	 * them they must be a Player to use the command.
	 *
	 * @param sender The CommandSender to check
	 * @return True if sender instanceof Player, false otherwise
	 */
	protected boolean isPlayer(final CommandSender sender) {
		if (sender instanceof Player) {
			return true;
		} else {
			ChatUtil.sendBrandingMessage(sender, this.mustBePlayerMessage);
			return false;
		}
	}

	/**
	 * Returns a string representation of one of the five base elements,
	 * converted from any possible alias of that element, its combos, or its
	 * subelements.
	 *
	 * @param element The string to try and determine an element for
	 * @return The element associated with the input string, if found, or null
	 *         otherwise
	 */
	public String getElement(final String element) {
		if (Arrays.asList(Commands.firealiases).contains(element) || Arrays.asList(Commands.firecomboaliases).contains(element)) {
			return "fire";
		} else if (Arrays.asList(Commands.combustionaliases).contains(element)) {
			return "combustion";
		} else if (Arrays.asList(Commands.lightningaliases).contains(element)) {
			return "lightning";
		} else if (Arrays.asList(Commands.earthaliases).contains(element) || Arrays.asList(Commands.earthcomboaliases).contains(element)) {
			return "earth";
		} else if (Arrays.asList(Commands.metalbendingaliases).contains(element)) {
			return "metal";
		} else if (Arrays.asList(Commands.sandbendingaliases).contains(element)) {
			return "sand";
		} else if (Arrays.asList(Commands.lavabendingaliases).contains(element)) {
			return "lava";
		} else if (Arrays.asList(Commands.airaliases).contains(element) || Arrays.asList(Commands.aircomboaliases).contains(element)) {
			return "air";
		} else if (Arrays.asList(Commands.spiritualprojectionaliases).contains(element)) {
			return "spiritual";
		} else if (Arrays.asList(Commands.flightaliases).contains(element)) {
			return "flight";
		} else if (Arrays.asList(Commands.wateraliases).contains(element) || Arrays.asList(Commands.watercomboaliases).contains(element)) {
			return "water";
		} else if (Arrays.asList(Commands.healingaliases).contains(element)) {
			return "healing";
		} else if (Arrays.asList(Commands.bloodaliases).contains(element)) {
			return "blood";
		} else if (Arrays.asList(Commands.icealiases).contains(element)) {
			return "ice";
		} else if (Arrays.asList(Commands.plantaliases).contains(element)) {
			return "plant";
		} else if (Arrays.asList(Commands.chialiases).contains(element) || Arrays.asList(Commands.chicomboaliases).contains(element)) {
			return "chi";
		}
		return null;
	}

	/**
	 * Returns a boolean if the string provided is numerical.
	 * @param id The string to check
	 * @return boolean if the string is numerical
	 */
	protected boolean isNumeric(final String id) {
		final NumberFormat formatter = NumberFormat.getInstance();
		final ParsePosition pos = new ParsePosition(0);
		formatter.parse(id, pos);
		return id.length() == pos.getIndex();
	}

	/**
	 * Returns a list for of commands for a page.
	 * @param entries The list of entries to paginate
	 * @param title The title of the paginated list
	 * @param page The page number to get
	 * @return A list of strings for the page
	 */
	protected List<String> getPage(final List<String> entries, final String title, int page, final boolean sort) {
		final List<String> strings = new ArrayList<String>();
		if (sort) {
			Collections.sort(entries);
		}

		if (page < 1) {
			page = 1;
		}
		if ((page * 8) - 8 >= entries.size()) {
			page = Math.round(entries.size() / 8) + 1;
			if (page < 1) {
				page = 1;
			}
		}
		strings.add(ChatUtil.color("&8- [&7" + page + "/" + (int) Math.ceil((entries.size() + .0) / (8 + .0)) + "&8]"));
		strings.add(title);
		if (entries.size() > ((page * 8) - 8)) {
			for (int i = ((page * 8) - 8); i < entries.size(); i++) {
				if (entries.get(i) != null) {
					strings.add(entries.get(i).toString());
				}
				if (i >= (page * 8) - 1) {
					break;
				}
			}
		}
		return strings;
	}

	/**
	 * Gets a list of valid arguments that can be used in tabbing.
	 * @param sender The sender of the command
	 * */
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		return new ArrayList<String>();
	}

	/**
	 * Whether the sender may see and run this command at all. This gates the Brigadier node, so it must
	 * return true for every sender that any code path of {@link #execute(CommandSender, List)} accepts.
	 * More specific permission checks (and their messages) stay inside execute.
	 * @param sender The sender to check
	 * @return True if the sender can use the command
	 */
	public boolean canUse(final CommandSender sender) {
		return sender.hasPermission("bending.command." + this.name);
	}

	/**
	 * Builds the Brigadier node for one alias of this command. The node forwards the typed arguments to
	 * {@link #execute(CommandSender, List)} and gets its suggestions from
	 * {@link #getTabCompletion(CommandSender, List)}.
	 * @param alias The literal to register the node under
	 * @return The node
	 */
	public final LiteralArgumentBuilder<CommandSourceStack> createNode(final String alias) {
		this.buildingAlias = alias;
		try {
			final LiteralArgumentBuilder<CommandSourceStack> node = io.papermc.paper.command.brigadier.Commands.literal(alias)
					.requires(source -> this.current().canUse(sender(source)))
					.executes(this.forwarder());
			this.buildArguments(node);
			return node;
		} finally {
			this.buildingAlias = null;
		}
	}

	/**
	 * The alias whose node is being built. The helpers capture it so they know which literal starts the command.
	 */
	private String buildingAlias;

	/**
	 * Adds the argument nodes of this command to its literal. Override this to declare typed arguments using
	 * {@link #word(String)}, {@link #integer(String, int, int)}, {@link #greedy(String)} and
	 * {@link #literals(ArgumentBuilder, String[], UnaryOperator)}. By default, the command accepts any arguments.
	 * @param node The literal node of this command
	 */
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
		node.then(this.greedy("args"));
	}

	/**
	 * @param name The argument name
	 * @return A single-word argument that suggests from {@link #getTabCompletion(CommandSender, List)} and runs the command
	 */
	protected RequiredArgumentBuilder<CommandSourceStack, String> word(final String name) {
		return io.papermc.paper.command.brigadier.Commands.argument(name, StringArgumentType.word()).suggests(this.suggester()).executes(this.forwarder());
	}

	/**
	 * @param name The argument name
	 * @return An argument taking the rest of the input that suggests from {@link #getTabCompletion(CommandSender, List)} and runs the command
	 */
	protected RequiredArgumentBuilder<CommandSourceStack, String> greedy(final String name) {
		return io.papermc.paper.command.brigadier.Commands.argument(name, StringArgumentType.greedyString()).suggests(this.suggester()).executes(this.forwarder());
	}

	/**
	 * @param name The argument name
	 * @param min The minimum value
	 * @param max The maximum value
	 * @return An integer argument that suggests from {@link #getTabCompletion(CommandSender, List)} and runs the command
	 */
	protected RequiredArgumentBuilder<CommandSourceStack, Integer> integer(final String name, final int min, final int max) {
		return io.papermc.paper.command.brigadier.Commands.argument(name, IntegerArgumentType.integer(min, max)).suggests(this.suggester()).executes(this.forwarder());
	}

	/**
	 * Adds a literal for each name to the parent, each with the same children
	 * @param parent The node to add the literals to
	 * @param names The names of the literals, e.g. a sub command and its aliases
	 * @param children Adds the child nodes to each literal
	 */
	protected void literals(final ArgumentBuilder<CommandSourceStack, ?> parent, final String[] names, final UnaryOperator<LiteralArgumentBuilder<CommandSourceStack>> children) {
		for (final String name : names) {
			parent.then(children.apply(io.papermc.paper.command.brigadier.Commands.literal(name).executes(this.forwarder())));
		}
		// Brigadier literals are case-sensitive and reject unknown input. Anything that doesn't match a literal falls
		// through to execute() instead, which matches case-insensitively and shows the usage help.
		if (parent.getArguments().stream().noneMatch(child -> child.getName().equals("args"))) {
			parent.then(this.greedy("args"));
		}
	}

	/**
	 * @return A command that runs this command with the arguments typed after the literal currently being built
	 */
	private Command<CommandSourceStack> forwarder() {
		final String alias = this.requireBuildingAlias();
		return context -> this.forward(context, alias);
	}

	/**
	 * @return A suggestion provider for the literal currently being built
	 */
	private SuggestionProvider<CommandSourceStack> suggester() {
		final String alias = this.requireBuildingAlias();
		return (context, builder) -> this.suggest(context, builder, alias);
	}

	private String requireBuildingAlias() {
		if (this.buildingAlias == null) {
			throw new IllegalStateException("Argument nodes can only be created inside buildArguments");
		}
		return this.buildingAlias;
	}

	/**
	 * Runs the command with the arguments typed after its literal
	 */
	private int forward(final CommandContext<CommandSourceStack> context, final String alias) {
		final int end = literalEnd(context, alias);
		final List<String> args = end < 0 ? new ArrayList<>() : split(context.getInput().substring(end));
		this.current().execute(sender(context.getSource()), args);
		return Command.SINGLE_SUCCESS;
	}

	/**
	 * Suggests completions for the word at the cursor using {@link #getTabCompletion(CommandSender, List)}
	 */
	private CompletableFuture<Suggestions> suggest(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder, final String alias) {
		final String input = builder.getInput();
		final int wordStart = input.lastIndexOf(' ', input.length() - 1) + 1;
		final int end = literalEnd(context, alias);
		final List<String> previous = end < 0 || end > wordStart ? new ArrayList<>() : split(input.substring(end, wordStart));
		final CommandSender sender = sender(context.getSource());

		final SuggestionsBuilder offset = builder.createOffset(Math.max(wordStart, builder.getStart()));
		BendingTabComplete.getPossibleCompletions(new String[] { input.substring(Math.max(wordStart, builder.getStart())) }, this.current().getTabCompletion(sender, previous)).forEach(offset::suggest);
		return offset.buildFuture();
	}

	/**
	 * Finds where the literal of this command ends in the input. The root command's label can have the same name
	 * as the alias (e.g. "/b b FireBlast"), so this takes the last literal with that name before the first
	 * argument. Works through Paper's alias redirects and /execute.
	 * @param alias The alias the command's node was built under
	 * @return The index after the literal, or -1 if it isn't in the context
	 */
	private static int literalEnd(final CommandContext<CommandSourceStack> context, final String alias) {
		int end = -1;
		for (final ParsedCommandNode<CommandSourceStack> parsed : context.getNodes()) {
			if (!(parsed.getNode() instanceof LiteralCommandNode)) {
				break;
			}
			if (parsed.getNode().getName().equals(alias)) {
				end = parsed.getRange().getEnd();
			}
		}
		return end;
	}

	/**
	 * Gets the instance of this command that is currently registered. Commands are recreated when ProjectKorra
	 * reloads, but the Brigadier tree is not.
	 * @return The current instance of this command
	 */
	protected PKCommand current() {
		return instances.getOrDefault(this.name, this);
	}

	/**
	 * Gets the sender a command should act as. Under /execute as, this is the executing entity, which matches how
	 * Bukkit commands behaved.
	 * @param source The command source
	 * @return The sender
	 */
	static CommandSender sender(final CommandSourceStack source) {
		return source.getExecutor() != null ? source.getExecutor() : source.getSender();
	}

	/**
	 * Splits command input into arguments, ignoring repeated spaces
	 * @param input The input
	 * @return The arguments
	 */
	static List<String> split(final String input) {
		final List<String> args = new ArrayList<>();
		for (final String arg : input.split(" ")) {
			if (!arg.isEmpty()) {
				args.add(arg);
			}
		}
		return args;
	}

	/**
	 * Create all command instances again. We do this so all commands pull their stuff from
	 * configs again
	 */
	public static void reloadCommands() {
		instances.clear();
		Commands.initializeCommands();
	}

	/**
	 * Get a list of online players that the sender can see
	 * @param sender Who is sending the command
	 * @return A list of online players that the sender can see
	 */
	public List<Player> getOnlinePlayers(final CommandSender sender) {
		return Bukkit.getOnlinePlayers().stream().filter(p -> !(sender instanceof Player) || ((Player) sender).canSee(p)).collect(Collectors.toList());
	}

	/**
	 * Get a list of online player names that the sender can see
	 * @param sender Who is sending the command
	 * @return A list of online player names that the sender can see
	 */
	public List<String> getOnlinePlayerNames(final CommandSender sender) {
		return Bukkit.getOnlinePlayers().stream().filter(p -> !(sender instanceof Player) || ((Player) sender).canSee(p)).map(Player::getName).collect(Collectors.toList());
	}

	/**
	 * Get an offline player by name. Runs in an async thread so the main thread doesn't stall.
	 * @param name The name of the player to get
	 * @return A CompletableFuture that will complete with the player
	 */
	@NotNull
	public CompletableFuture<OfflinePlayer> getPlayer(String name) {
		CompletableFuture<OfflinePlayer> future = new CompletableFuture<OfflinePlayer>().whenComplete((o, e) -> {
			if (e != null) {
				e.printStackTrace();
			}
		});

		//If the player is online, return them instantly so it doesn't happen next tick
		if (Bukkit.getPlayer(name) != null) return CompletableFuture.completedFuture(Bukkit.getPlayer(name));

		Bukkit.getScheduler().runTaskAsynchronously(ProjectKorra.plugin, () -> {
			OfflinePlayer player = Bukkit.getOfflinePlayer(name);
			Bukkit.getScheduler().runTask(ProjectKorra.plugin, () -> future.complete(player)); //Complete in a sync thread
		});

		return future;
	}
}
