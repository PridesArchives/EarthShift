import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class EarthShift extends SandAbility implements AddonAbility, ComboAbility {
	
	private final String path = "";
	
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.SELECT_RANGE)
	private double select_range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	
	private Block target;
	
	public EarthShift(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (CoreAbility.hasAbility(player, EarthShift.class)) {
			return;
		}
		this.cooldown = ConfigManager.getConfig().getLong(path + "Cooldown");
		this.duration = ConfigManager.getConfig().getLong(path + "Duration");
		this.select_range = ConfigManager.getConfig().getDouble(path + "SelectRange");
		this.radius = ConfigManager.getConfig().getDouble(path + "Radius");
		
		this.target = rayTraceBlock(player, this.select_range);
		
		if (target == null) return;
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.target.getLocation())) {
			return;
		}
		playSandbendingSound(this.target.getLocation());
		start();
	}
	
	@Override
	public void progress() {
		for (Block block : GeneralMethods.getBlocksAroundPoint(this.target.getLocation(), this.radius)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) continue;
			
			Block top = GeneralMethods.getTopBlock(block.getLocation(), 3);
			ParticleEffect.FALLING_DUST.display(top.getLocation(), 2, 0.5, 0.5, 0.5, 0);
			
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(top.getLocation(), 1)) {
				if (entity instanceof ArmorStand || entity.getUniqueId() == player.getUniqueId()) continue;
				
				if (entity instanceof LivingEntity) {
					if (entity instanceof Player) {
						if (Commands.invincible.contains(entity.getName())) continue;
					}
					((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 50, 0));
				}
			}
		}
		if (System.currentTimeMillis() > getStartTime() + this.duration) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
	}

    public static Block rayTraceBlock(Player player, double range) {
		RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation().clone(), player.getEyeLocation().getDirection(), range);
		if (result != null) {
			return result.getHitBlock();
		}
		return null;
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public String getName() {
		return "EarthShift";
	}
	
	@Override
	public Location getLocation() {
		return null;
	}
	
	@Override
	public void load() { }
	
	@Override
	public void stop() { }
	
	@Override
	public boolean isEnabled() {
		return ConfigManager.getConfig().getBoolean("EarthShift.Enabled", true);
	}
	
	@Override
	public String getAuthor() {
		return "Prride";
	}
	
	@Override
	public String getVersion() {
		return "1.0.0";
	}
	
	@Override
	public String getDescription() {
		return Element.EARTH.getColor() + "Shift the earth, changing it to favorable terrain for all of your sandbending needs.";
	}
	
	@Override
	public String getInstructions() {
		return "Shockwave (Right click block) > Shockwave (Left click)";
	}
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new EarthShift(player);
	}
	
	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> info = new ArrayList<>();
		info.add(new AbilityInformation("Shockwave", ClickType.RIGHT_CLICK_BLOCK));
		info.add(new AbilityInformation("Shockwave", ClickType.LEFT_CLICK));
		return info;
	}
}
