package com.projectkorra.projectkorra.command;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.projectkorra.projectkorra.util.ChatUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent;
import com.projectkorra.projectkorra.event.PlayerChangeElementEvent.Result;

/**
 * Executor for /bending permaremove. Extends {@link PKCommand}.
 */
public class PermaremoveCommand extends PKCommand {

	private final String invalidPlayer;
	private final String restored;
	private final String restoredConfirm;
	private final String removed;
	private final String removedConfirm;

	public PermaremoveCommand() {
		super("permaremove", "/bending permaremove <Player>", ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Description"), new String[] { "permaremove", "premove", "permremove", "pr" });

		this.invalidPlayer = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.InvalidPlayer");
		this.restored = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Restored");
		this.restoredConfirm = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.RestoredConfirm");
		this.removed = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.Removed");
		this.removedConfirm = ConfigManager.languageConfig.get().getString("Commands.PermaRemove.RemovedConfirm");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 1)) {
			return;
		}
		if (args.size() == 1) {
			this.permaremove(sender, args.get(0));
		} else if (args.size() == 0 && this.isPlayer(sender)) {
			this.permaremove(sender, sender.getName());
		}
	}

	/**
	 * Permanently removes a player's bending, or restores it if it had already
	 * been permaremoved.
	 *
	 * @param sender The CommandSender who issued the permaremove command
	 * @param target The Player who's bending should be permaremoved
	 */
	private void permaremove(final CommandSender sender, final String target) {
		// Looked up off the main thread, as an unknown name can need a request to Mojang
		this.getPlayer(target).thenAccept(player -> {
			if (!player.isOnline() && !player.hasPlayedBefore()) {
				ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy(this.invalidPlayer).colorIfAbsent(NamedTextColor.RED));
				return;
			}

			BendingPlayer.getOrLoadOfflineAsync(player).thenAccept(bPlayer -> {
				boolean online = bPlayer instanceof BendingPlayer;
				if (bPlayer.isPermaRemoved()) {
					bPlayer.setPermaRemoved(false);
					if (online) ChatUtil.sendBrandingMessage((Player) player, ChatUtil.legacy(this.restored).colorIfAbsent(NamedTextColor.GREEN));
					if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(target)) {
						ChatUtil.sendBrandingMessage(sender, replace(ChatUtil.legacy(this.restoredConfirm), "{target}", Component.text(player.getName(), NamedTextColor.DARK_AQUA)).colorIfAbsent(NamedTextColor.GREEN));
					}
				} else {
					PlayerChangeElementEvent event = new PlayerChangeElementEvent(sender, player, null, Result.PERMAREMOVE);

					bPlayer.getElements().clear();
					bPlayer.saveElements();
					bPlayer.setPermaRemoved(true);
					if (online) {
						((BendingPlayer)bPlayer).removeUnusableAbilities();
						ChatUtil.sendBrandingMessage((Player) player, ChatUtil.legacy(this.removed).colorIfAbsent(NamedTextColor.RED));
						Bukkit.getServer().getPluginManager().callEvent(new PlayerChangeElementEvent(sender, (Player) player, null, Result.PERMAREMOVE));
					}

					if (!(sender instanceof Player) || !sender.getName().equalsIgnoreCase(target)) {
						ChatUtil.sendBrandingMessage(sender, replace(ChatUtil.legacy(this.removedConfirm), "{target}", Component.text(player.getName(), NamedTextColor.DARK_AQUA)).colorIfAbsent(NamedTextColor.RED));
					}

				}
			});
		});
	}

	/**
	 * Checks if the sender has the permission 'bending.admin.permaremove'. If
	 * not, it tells them they don't have permission to use the command.
	 *
	 * @return True if they have the permission, false otherwise
	 */
	@Override
	public boolean hasPermission(final CommandSender sender) {
		if (!sender.hasPermission("bending.admin.permaremove")) {
			ChatUtil.sendBrandingMessage(sender, super.noPermissionMessage);
			return false;
		}
		return true;
	}

	@Override
	public boolean canUse(final CommandSender sender) {
		return sender.hasPermission("bending.admin.permaremove");
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
		node.then(this.word("player"));
	}

	private static Component replace(final Component component, final String placeholder, final Component replacement) {
		return component.replaceText(builder -> builder.matchLiteral(placeholder).replacement(replacement));
	}

	@Override
	protected List<String> getTabCompletion(final CommandSender sender, final List<String> args) {
		if (args.size() >= 1 || !sender.hasPermission("bending.command.permaremove")) {
			return new ArrayList<String>();
		}
		return getOnlinePlayerNames(sender);
	}
}
