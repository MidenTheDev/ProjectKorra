package com.projectkorra.projectkorra.command;

import java.util.List;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ChatUtil;

/**
 * Executor for /bending check. Extends {@link PKCommand}.
 */
public class CheckCommand extends PKCommand {

	private final String newVersionAvailable;
	private final String curVersion;
	private final String newVersion;
	private final String upToDate;

	public CheckCommand() {
		super("check", "/bending check", ConfigManager.languageConfig.get().getString("Commands.Check.Description"), new String[] { "check", "chk" });

		this.newVersionAvailable = ConfigManager.languageConfig.get().getString("Commands.Check.NewVersionAvailable");
		this.curVersion = ConfigManager.languageConfig.get().getString("Commands.Check.CurrentVersion");
		this.newVersion = ConfigManager.languageConfig.get().getString("Commands.Check.LatestVersion");
		this.upToDate = ConfigManager.languageConfig.get().getString("Commands.Check.UpToDate");
	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender)) {
			return;
		} else if (args.size() > 0) {
			this.help(sender, false);
			return;
		}
		if (!ProjectKorra.plugin.updater.isEnabled()) {
			sender.sendMessage(Component.text("The update checker has been disabled in the config. Please enable it in order to use this command.", NamedTextColor.YELLOW));
		} else if (ProjectKorra.plugin.updater.updateAvailable()) {
			sender.sendMessage(replace(ChatUtil.legacy(this.newVersionAvailable), "ProjectKorra", Component.text("ProjectKorra", NamedTextColor.GOLD)).colorIfAbsent(NamedTextColor.GREEN));
			sender.sendMessage(replace(ChatUtil.legacy(this.curVersion), "{version}", Component.text(ProjectKorra.plugin.updater.getCurrentVersion(), NamedTextColor.RED)).colorIfAbsent(NamedTextColor.YELLOW));
			sender.sendMessage(replace(ChatUtil.legacy(this.newVersion), "{version}", Component.text(ProjectKorra.plugin.updater.getUpdateVersion(), NamedTextColor.GOLD)).colorIfAbsent(NamedTextColor.YELLOW));
		} else {
			sender.sendMessage(replace(ChatUtil.legacy(this.upToDate), "ProjectKorra", Component.text("ProjectKorra", NamedTextColor.GOLD)).colorIfAbsent(NamedTextColor.YELLOW));
		}
	}

	private static Component replace(final Component component, final String placeholder, final Component replacement) {
		return component.replaceText(builder -> builder.matchLiteral(placeholder).replacement(replacement));
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
	}

}
