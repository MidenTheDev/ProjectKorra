package com.projectkorra.projectkorra.command;

import java.util.List;

import com.projectkorra.projectkorra.util.ChatUtil;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * Executor for /bending invincible. Extends {@link PKCommand}.
 */
public class InvincibleCommand extends PKCommand {

	public InvincibleCommand() {
		super("invincible", "/bending invincible", ConfigManager.languageConfig.get().getString("Commands.Invincible.Description"), new String[] { "invincible", "inv", "i" });

	}

	@Override
	public void execute(final CommandSender sender, final List<String> args) {
		if (!this.hasPermission(sender) || !this.isPlayer(sender) || !this.correctLength(sender, args.size(), 0, 0)) {
			return;
		}

		if (!Commands.invincible.contains(sender.getName())) {
			Commands.invincible.add(sender.getName());
			ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy(ConfigManager.languageConfig.get().getString("Commands.Invincible.ToggledOn")).colorIfAbsent(NamedTextColor.GREEN));
		} else {
			Commands.invincible.remove(sender.getName());
			ChatUtil.sendBrandingMessage(sender, ChatUtil.legacy(ConfigManager.languageConfig.get().getString("Commands.Invincible.ToggledOff")).colorIfAbsent(NamedTextColor.RED));
		}
	}

	@Override
	protected void buildArguments(final LiteralArgumentBuilder<CommandSourceStack> node) {
	}

}
