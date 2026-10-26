package com.projectkorra.projectkorra.command;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending version. Extends {@link PKCommand}.
 */
public class VersionCommand extends PKCommand {

	public VersionCommand() {
		super("version", "/bending version", ConfigManager.languageConfig.get().getString("Commands.Version.Description"), new String[] { "version", "v" });
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		sender.sendMessage(line("Core Version: ", ProjectKorra.plugin.getPluginMeta().getVersion()));
		if (GeneralMethods.hasRPG()) {
			sender.sendMessage(line("RPG Version: ", GeneralMethods.getRPG().getPluginMeta().getVersion()));
		}
		if (GeneralMethods.hasItems()) {
			sender.sendMessage(line("Items Version: ", GeneralMethods.getItems().getPluginMeta().getVersion()));
		}
		if (GeneralMethods.hasSpirits()) {
			sender.sendMessage(line("Spirits Version: ", GeneralMethods.getSpirits().getPluginMeta().getVersion()));
		}
		if (GeneralMethods.hasProbending()) {
			sender.sendMessage(line("Probending Version: ", GeneralMethods.getProbending().getPluginMeta().getVersion()));
		}
		sender.sendMessage(line("Founded by: ", "MistPhizzle"));
		sender.sendMessage(Component.text("Special thanks to ", NamedTextColor.GREEN).append(Component.text("Orion304 ", NamedTextColor.RED)).append(Component.text("for establishing this great community.", NamedTextColor.GREEN)));
		sender.sendMessage(line("Learn More: ", "http://projectkorra.com"));
	}

	private static Component line(final String label, final String value) {
		return Component.text(label, NamedTextColor.GREEN).append(Component.text(value, NamedTextColor.RED));
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {}

}
