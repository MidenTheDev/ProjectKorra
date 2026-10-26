package com.projectkorra.projectkorra.command;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;

/**
 * Executor for /bending reload. Extends {@link PKCommand}.
 */
public class ReloadCommand extends PKCommand {

	public ReloadCommand() {
		super("reload", "/bending reload", ConfigManager.languageConfig.get().getString("Commands.Reload.Description"), new String[] { "reload", "r" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}
		GeneralMethods.reloadPlugin(sender);
		sender.sendMessage(ChatUtil.legacy(ConfigManager.languageConfig.get().getString("Commands.Reload.SuccessfullyReloaded")).colorIfAbsent(NamedTextColor.AQUA));
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
	}

}
