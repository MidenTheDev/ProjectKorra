package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.GeneralMethods;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

/**
 * @deprecated Marked for removal. Use {@link org.bukkit.World#spawnParticle} instead
 */
@Deprecated
public enum ParticleEffect {
	
	ASH (Particle.ASH),
	
	/**
	 * Applicable data: {@link BlockData}
	 */
	BLOCK_CRACK (Particle.BLOCK_CRUMBLE),
	
	/**
	 * Applicable data: {@link BlockData}
	 */
	BLOCK_DUST (Particle.DUST),
	BUBBLE_COLUMN_UP (Particle.BUBBLE_COLUMN_UP),
	BUBBLE_POP (Particle.BUBBLE_POP),
	CAMPFIRE_COSY_SMOKE (Particle.CAMPFIRE_COSY_SMOKE),
	CAMPFIRE_SIGNAL_SMOKE (Particle.CAMPFIRE_SIGNAL_SMOKE),
	CLOUD (Particle.CLOUD),
	COMPOSTER (Particle.COMPOSTER),
	CRIMSON_SPORE (Particle.CRIMSON_SPORE),
	CRIT (Particle.CRIT),
	CRIT_MAGIC (Particle.CRIT), @Deprecated MAGIC_CRIT (Particle.CRIT),
	CURRENT_DOWN (Particle.CURRENT_DOWN),
	DAMAGE_INDICATOR (Particle.DAMAGE_INDICATOR),
	DOLPHIN (Particle.DOLPHIN),
	DRAGON_BREATH (Particle.DRAGON_BREATH),
	DRIP_LAVA (Particle.DRIPPING_LAVA),
	DRIP_WATER (Particle.DRIPPING_WATER),
	DRIPPING_HONEY (Particle.DRIPPING_HONEY),
	DRIPPING_OBSIDIAN_TEAR (Particle.DRIPPING_OBSIDIAN_TEAR),
	ENCHANTMENT_TABLE (Particle.ENCHANT),
	END_ROD (Particle.END_ROD),
	EXPLOSION_HUGE (Particle.EXPLOSION), @Deprecated HUGE_EXPLOSION (Particle.EXPLOSION),
	EXPLOSION_LARGE (Particle.EXPLOSION), @Deprecated LARGE_EXPLODE (Particle.EXPLOSION),
	EXPLOSION_NORMAL (Particle.EXPLOSION), @Deprecated EXPLODE (Particle.EXPLOSION),
	
	/**
	 * Applicable data: {@link BlockData}
	 */
	FALLING_DUST (Particle.FALLING_DUST),
	FALLING_HONEY (Particle.FALLING_HONEY),
	FALLING_LAVA (Particle.FALLING_LAVA),
	FALLING_NECTAR (Particle.FALLING_NECTAR),
	FALLING_OBSIDIAN_TEAR (Particle.FALLING_OBSIDIAN_TEAR),
	FALLING_WATER (Particle.FALLING_WATER),
	FIREWORKS_SPARK (Particle.FIREWORK),
	FLAME (Particle.FLAME),
	FLASH (Particle.FLASH),
	HEART (Particle.HEART),
	
	/**
	 * Applicable data: {@link ItemStack}
	 */
	ITEM_CRACK (Particle.ITEM),
	LANDING_HONEY (Particle.LANDING_HONEY),
	LANDING_LAVA (Particle.LANDING_LAVA),
	LANDING_OBSIDIAN_TEAR (Particle.LANDING_OBSIDIAN_TEAR),
	LAVA (Particle.LAVA),
	MOB_APPEARANCE (Particle.ANGRY_VILLAGER),
	NAUTILUS (Particle.NAUTILUS),
	NOTE (Particle.NOTE),
	PORTAL (Particle.PORTAL),
	
	/**
	 * Applicable data: {@link DustOptions}
	 */
	REDSTONE (Particle.DUST), @Deprecated RED_DUST (Particle.DUST),
	REVERSE_PORTAL (Particle.REVERSE_PORTAL),
	SLIME (Particle.ITEM_SLIME),
	SMOKE_NORMAL (Particle.SMOKE), @Deprecated SMOKE (Particle.SMOKE),
	SMOKE_LARGE (Particle.LARGE_SMOKE), @Deprecated LARGE_SMOKE (Particle.LARGE_SMOKE),
	SNEEZE (Particle.SNEEZE),
	SNOW_SHOVEL (Particle.SNOWFLAKE),
	SNOWBALL (Particle.ITEM_SNOWBALL), @Deprecated SNOWBALL_PROOF (Particle.ITEM_SNOWBALL),
	SOUL (Particle.SOUL),
	SOUL_FIRE_FLAME (Particle.SOUL_FIRE_FLAME),
	SPELL (Particle.ENTITY_EFFECT),
	SPELL_INSTANT (Particle.ENTITY_EFFECT), @Deprecated INSTANT_SPELL (Particle.ENTITY_EFFECT),
	SPELL_MOB (Particle.ENTITY_EFFECT), @Deprecated MOB_SPELL (Particle.ENTITY_EFFECT),
	SPELL_MOB_AMBIENT (GeneralMethods.getMCVersion() >= 1205 ? Particle.ENTITY_EFFECT : Particle.ENTITY_EFFECT),
	@Deprecated MOB_SPELL_AMBIENT (GeneralMethods.getMCVersion() >= 1205 ? Particle.ENTITY_EFFECT : Particle.ENTITY_EFFECT),
	SPELL_WITCH (Particle.WITCH), @Deprecated WITCH_SPELL (Particle.WITCH),
	SPIT (Particle.SPIT),
	SQUID_INK (Particle.SQUID_INK),
	SUSPENDED (Particle.VIBRATION), @Deprecated SUSPEND (Particle.VIBRATION),
	SUSPENDED_DEPTH (Particle.VIBRATION), @Deprecated DEPTH_SUSPEND (Particle.VIBRATION),
	SWEEP_ATTACK (Particle.SWEEP_ATTACK),
	TOTEM (Particle.TOTEM_OF_UNDYING),
	TOWN_AURA (Particle.VIBRATION),
	VILLAGER_ANGRY (Particle.ANGRY_VILLAGER), @Deprecated ANGRY_VILLAGER (Particle.ANGRY_VILLAGER),
	VILLAGER_HAPPY (Particle.HAPPY_VILLAGER), @Deprecated HAPPY_VILLAGER (Particle.HAPPY_VILLAGER),
	WARPED_SPORE (Particle.WARPED_SPORE),
	WATER_BUBBLE (Particle.BUBBLE), @Deprecated BUBBLE (Particle.BUBBLE),
	WATER_DROP (Particle.DRIPPING_WATER),
	WATER_SPLASH (Particle.FALLING_WATER), @Deprecated SPLASH (Particle.FALLING_WATER),
	WATER_WAKE (Particle.FALLING_WATER), @Deprecated WAKE (Particle.FALLING_WATER),
	WHITE_ASH (Particle.WHITE_ASH);
	
	Particle particle;
	Class<?> dataClass;
	
	private ParticleEffect(Particle particle) {
		this.particle = particle;
		this.dataClass = particle.getDataType();
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	/**
	 * Displays the particle at the specified location without offsets
	 * @param loc Location to display the particle at
	 * @param amount how many of the particle to display
	 */
	public void display(Location loc, int amount) {
		display(loc, amount, 0, 0, 0);
	}
	
	/**
	 * Displays the particle at the specified location with no extra data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0);
	}
	
	/**
	 * Displays the particle at the specified location with extra data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 * @param extra extra data to affect the particle, usually affects speed or does nothing
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra) {
		if(particle == Particle.ENTITY_EFFECT) {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, Color.WHITE, true);
		}
		else if (particle == Particle.DUST) {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, new DustOptions(Color.GRAY,1f), true);
		}
		else {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, null, true);
		}

	}
	
	/**
	 * Displays the particle at the specified location with data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 * @param data data to display the particle with, only applicable on several particle types (check the enum)
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, Object data) {
		display(loc, amount, offsetX, offsetY, offsetZ, 0, data);
	}
	
	/**
	 * Displays the particle at the specified location with regular and extra data
	 * @param loc Location to spawn the particle
	 * @param amount how many of the particle to spawn
	 * @param offsetX random offset on the x axis
	 * @param offsetY random offset on the y axis
	 * @param offsetZ random offset on the z axis
	 * @param extra extra data to affect the particle, usually affects speed or does nothing
	 * @param data data to display the particle with, only applicable on several particle types (check the enum)
	 */
	public void display(Location loc, int amount, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
		if (dataClass.isAssignableFrom(Void.class) || data == null || !dataClass.isAssignableFrom(data.getClass())) {
			display(loc, amount, offsetX, offsetY, offsetZ, extra);
		} else {
			loc.getWorld().spawnParticle(particle, loc, amount, offsetX, offsetY, offsetZ, extra, data, true);
		}
	}
}
