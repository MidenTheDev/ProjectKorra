package com.projectkorra.projectkorra.command;

import java.io.File;
import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.ChatUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending debug. Extends {@link PKCommand}.
 */
public class DebugCommand extends PKCommand {

	public DebugCommand() {
		super("debug", "/bending debug", ConfigManager.languageConfig.get().getString("Commands.Debug.Description"), new String[] { "debug", "de" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender)) {
			return;
		} else if (args.size() != 0) {
			this.help(sender, false);
			return;
		}

		GeneralMethods.runDebug();
		final File debugFile = new File(ProjectKorra.plugin.getDataFolder(), "debug.txt");
		sender.sendMessage(ChatUtil.legacy(ConfigManager.languageConfig.get().getString("Commands.Debug.SuccessfullyExported"))
				.colorIfAbsent(NamedTextColor.GREEN)
				.clickEvent(ClickEvent.copyToClipboard(debugFile.getAbsolutePath())));
	}

	@Override
	public boolean canUse(final CommandSender sender) {
		return sender.hasPermission("bending.admin." + this.getName());
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
	}

	/**
	 * Checks if the CommandSender has the permission 'bending.admin.debug'. If
	 * not, it tells them they don't have permission.
	 *
	 * @return True if they have permission, false otherwise.
	 */
	@Override
	public boolean hasPermission(final CommandSender sender) {
		if (!sender.hasPermission("bending.admin." + this.getName())) {
			sender.sendMessage(ChatUtil.legacy(super.noPermissionMessage));
			return false;
		}
		return true;
	}
}
