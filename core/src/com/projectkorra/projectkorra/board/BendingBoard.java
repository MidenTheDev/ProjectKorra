package com.projectkorra.projectkorra.board;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import com.projectkorra.projectkorra.util.ChatUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.scoreboard.Criteria;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a player's scoreboard for bending purposes
 */
public class BendingBoard {

	protected static final char[] CHAT_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	public static class BoardSlot {
		
		protected Scoreboard board;
		protected Objective obj;
		public int slot;
		protected Team team;
		protected String entry;
		public Optional<BoardSlot> next = Optional.empty(), prev = Optional.empty();

		public BoardSlot(Scoreboard board, Objective obj, int slot) {
			this.board = board;
			this.obj = obj;
			this.slot = slot + 1;
			this.formTeam();
		}

		public void formTeam() {
			this.team = board.getTeam("slot" + this.slot) == null
					? board.registerNewTeam("slot" + this.slot)
					: board.getTeam("slot" + this.slot);
			this.entry = "\u00A7" + CHAT_CHARS[slot % 10] + "" + "\u00A7" + CHAT_CHARS[slot % 16];
			
			team.addEntry(entry);
		}

		public void set() {
			int s = Math.min(slot, 11);
			obj.getScore(entry).setScore(-s);
		}
		
		public void update(@NotNull Component prefix, @NotNull Component name) {
			team.prefix(prefix);
			team.suffix(name);
			set();
		}

		/**
		 * @deprecated Use {@link #update(Component, Component)}
		 */
		@Deprecated
		public void update(@NotNull String prefix, @NotNull String name) {
			update(ChatUtil.legacy(prefix), ChatUtil.legacy(name));
		}
		
		public void setSlot(int slot) {
			this.slot = slot + 1;
			set();
		}

		public void clear(boolean formNewTeam) {
			//Make sure the team hasn't already been unregistered
			if (team.getScoreboard() != null && team.getScoreboard().getTeam("slot" + this.slot) != null) {
				Component prefix = team.prefix(), suffix = team.suffix();
				team.unregister();
				if (formNewTeam) {
					formTeam();
					update(prefix, suffix);
				}
			}
			board.resetScores(entry);
		}

		public void setNext(BoardSlot slot) {
			this.next = Optional.of(slot);
		}

		public void setPrev(BoardSlot slot) {
			this.prev = Optional.of(slot);
		}
	}

	protected final BoardSlot[] slots = new BoardSlot[9];
	protected final Map<String, BoardSlot> misc = new HashMap<>();
	protected final Queue<Integer> miscSlotIds = new LinkedList<>();
	protected BoardSlot miscTail = null;

	protected final Player player;
	protected final BendingPlayer bendingPlayer;

	protected final Scoreboard bendingBoard;
	protected final Objective bendingSlots;
	protected int selectedSlot;

	protected String prefix, emptySlot, miscSeparator;
	protected TextColor selectedColor, altColor;

	public BendingBoard(final BendingPlayer bPlayer) {
		bendingPlayer = bPlayer;
		player = bPlayer.getPlayer();
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;

		bendingBoard = Bukkit.getScoreboardManager().getNewScoreboard();
		
		Component title = ChatUtil.legacy(ConfigManager.languageConfig.get().getString("Board.Title"));
		bendingSlots = bendingBoard.registerNewObjective("Board Slots", Criteria.DUMMY, title);
		bendingSlots.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (int i = 0; i < 9; ++i) {
			slots[i] = new BoardSlot(bendingBoard, bendingSlots, i);
		}
		
		prefix = ChatUtil.stripColor(ConfigManager.languageConfig.get().getString("Board.Prefix.Text"));
		emptySlot = ChatUtil.color(ConfigManager.languageConfig.get().getString("Board.EmptySlot"));
		miscSeparator = ChatUtil.color(ConfigManager.languageConfig.get().getString("Board.MiscSeparator")); // Score entries are plain strings

		for (int i = 0; i < 9; i++) {
			miscSlotIds.add(i);
		}

		updateAll();
	}

	void destroy() {
		bendingBoard.clearSlot(DisplaySlot.SIDEBAR);
		bendingSlots.unregister();
	}
	
	private TextColor getElementColor() {
		if (bendingPlayer.getElements().size() > 1) {
			return Element.AVATAR.getTextColor();
		} else if (bendingPlayer.getElements().size() == 1) {
			return bendingPlayer.getElements().get(0).getTextColor();
		} else {
			return NamedTextColor.WHITE;
		}
	}
	
	private TextColor getColor(String from, TextColor def) {
		if (from.equalsIgnoreCase("element")) {
			return getElementColor();
		}

		TextColor color = ChatUtil.parseColor(from, null);
		if (color == null) {
			ProjectKorra.plugin.getLogger().warning("Couldn't parse board color from '" + from + "', using default!");
			return def;
		}
		return color;
	}

	public void hide() {
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	public void show() {
		player.setScoreboard(bendingBoard);
		updateAll();
	}

	public boolean isVisible() {
		return player.getScoreboard().equals(bendingBoard);
	}

	public void setVisible(boolean show) {
		if (show) {
			show();
		} else {
			hide();
		}
	}

	public void setSlot(int slot, String ability, boolean cooldown) {
		if (slot < 1 || slot > 9 || !player.getScoreboard().equals(bendingBoard)) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		if (ability == null || ability.isEmpty()) {
			sb.append(emptySlot.replaceAll("\\{slot_number\\}", "" + slot));
		} else {
			CoreAbility coreAbility = CoreAbility.getAbility(ChatUtil.stripColor(ability));
			if (coreAbility == null) { // MultiAbility
				if (cooldown || bendingPlayer.isOnCooldown(ability)) {
					sb.append("\u00A7m");
				}
				
				sb.append(ability);
			} else {
				sb.append(coreAbility.getMovePreviewWithoutCooldownTimer(player, cooldown));
			}
		}
		
		slots[slot - 1].update(Component.text(prefix, slot == selectedSlot ? selectedColor : altColor), ChatUtil.legacy(sb.toString()));
	}
	
	private int updateSelected(int newSlot) {
		int oldSlot = selectedSlot;
		selectedSlot = newSlot;
		return oldSlot;
	}
	
	public void updateColors() {
		selectedColor = getColor(ConfigManager.languageConfig.get().getString("Board.Prefix.SelectedColor"), NamedTextColor.WHITE);
		altColor = getColor(ConfigManager.languageConfig.get().getString("Board.Prefix.NonSelectedColor"), NamedTextColor.DARK_GRAY);
	}

	public void updateAll() {
		updateColors();
		selectedSlot = player.getInventory().getHeldItemSlot() + 1;
		for (int i = 1; i <= 9; i++) {
			setSlot(i, bendingPlayer.getAbilities().get(i), false);
		}
	}

	public void clearSlot(int slot) {
		setSlot(slot, null, false);
	}

	public void setActiveSlot(int newSlot) {
		int oldSlot = updateSelected(newSlot);
		setSlot(oldSlot, bendingPlayer.getAbilities().get(oldSlot), false);
		setSlot(newSlot, bendingPlayer.getAbilities().get(newSlot), false);
	}

	public void setAbilityCooldown(String name, boolean cooldown) {
		bendingPlayer.getAbilities().entrySet().stream().filter(entry -> name.equals(entry.getValue())).forEach(entry -> setSlot(entry.getKey(), name, cooldown));
	}
	
	/**
	 * @deprecated Use {@link #updateMisc(String, TextColor, boolean)}
	 */
	@Deprecated
	public void updateMisc(String name, net.md_5.bungee.api.ChatColor color, boolean cooldown) {
		updateMisc(name, ChatUtil.toTextColor(color), cooldown);
	}

	public void updateMisc(String name, TextColor color, boolean cooldown) {
		if (!cooldown) {
			misc.computeIfPresent(name, (key, slot) -> {
				slot.next.ifPresent(n -> n.prev = slot.prev);
				slot.prev.ifPresent(p -> p.next = slot.next);
				if (slot == miscTail) {
					miscTail = null;
				}
				
				slot.clear(false);
				miscSlotIds.add(slot.slot - 10);
				return null;
			});
				
			if (misc.isEmpty()) {
				bendingBoard.resetScores(miscSeparator);
			}
		} else if (!misc.containsKey(name)) {
			BoardSlot slot = new BoardSlot(bendingBoard, bendingSlots, 10 + miscSlotIds.poll());
			slot.update(Component.text(String.join("", Collections.nCopies(prefix.length() + 1, " "))), Component.text(name, color, TextDecoration.STRIKETHROUGH));
			
			if (miscTail != null) {
				miscTail.setNext(slot);
				slot.setPrev(miscTail);
			}
			
			miscTail = slot;
			misc.put(name, slot);
			bendingSlots.getScore(miscSeparator).setScore(-10);
		}	
	}
}
