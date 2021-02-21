package fr.stormer3428.chairs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Chairs extends JavaPlugin implements Listener,TabCompleter{

	public static Chairs i;
	public static NamespacedKey locationKeyX;
	public static NamespacedKey locationKeyY;
	public static NamespacedKey locationKeyZ;
	public static NamespacedKey locationKeyYaw;
	public static NamespacedKey locationKeyPitch;

	@Override
	public void onEnable() {
		i = this;
		locationKeyX = new NamespacedKey(i, "riderLocationX");
		locationKeyY = new NamespacedKey(i, "riderLocationY");
		locationKeyZ = new NamespacedKey(i, "riderLocationZ");
		locationKeyYaw = new NamespacedKey(i, "riderLocationYaw");
		locationKeyPitch = new NamespacedKey(i, "riderLocationPitch");
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("chair").setExecutor(this);
		getCommand("chair").setTabCompleter(this);
		loadConfig();
	}

	void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if(s instanceof Player){
			Player p = (Player) s;
			if(cmd.getName().equals("chair")){
				if(!p.isOp()) return false;
				if(args.length > 0){
					if (args[0].equalsIgnoreCase("add")){

						if(args.length == 1) {
							if(p.getTargetBlockExact(5) != null && p.getTargetBlockExact(5).getType() != Material.AIR) return addChairWithMessages(p.getTargetBlockExact(5).getType(), p);

							Message.error(s, "You must be looking at a block or specify a block to add");
							return false;
						}else {
							Material mat = Material.AIR;
							for(Material m : Material.values()) if(args[1].equalsIgnoreCase(m.name())) {
								mat = m;
								break;
							}

							if(mat == Material.AIR) Message.error(p, "Found no block with such name : " + args[1]);
							else return addChairWithMessages(mat, p);
						}
					}

					if (args[0].equalsIgnoreCase("remove")){
						if(args.length == 1) {
							if(p.getTargetBlockExact(5) != null && p.getTargetBlockExact(5).getType() != Material.AIR) return removeChairWithMessages(p.getTargetBlockExact(5).getType(), p);

							Message.error(s, "You must be looking at a block or specify a block to add");
							return false;
						}else {
							Material mat = Material.AIR;
							for(Material m : Material.values()) if(args[1].equalsIgnoreCase(m.name())) {
								mat = m;
								break;
							}

							if(mat == Material.AIR) Message.error(p, "Found no block with such name : " + args[1]);
							else return removeChairWithMessages(mat, p);
						}
					}

					if (args[0].equalsIgnoreCase("list")){
						Message.normal("Current Chairs list :");
						for(String string : getConfig().getStringList("chairs")){
							Message.normal(string);
						}
						return true;
					}
				}
				Message.error(s, "Synntax : /chair <add/remove/list>");
				return false;
			}
		}
		return false;
	}

	public boolean removeChairWithMessages(Material mat, Player p){
		if(removeChair(mat)) {
			Message.normal(p, "Removed " + mat.name() + " from the Chairs list.");
			return true;
		}
		Message.error(p, mat.name() + " is not in the list");
		return false;
	}

	private boolean removeChair(Material mat) {
		List<String> newlist = getConfig().getStringList("chairs");
		if(newlist.contains(mat.name())) {
			newlist.remove(mat.name());
			getConfig().set("chairs", newlist);
			loadConfig();
			return true;
		}
		return false;
	}

	public boolean addChairWithMessages(Material mat, Player p){
		if(addChair(mat)) {
			Message.normal(p, "added " + mat.name() + " to the Chairs list.");
			return true;
		}
		Message.error(p, mat.name() + " is already in the list");
		return false;
	}

	private boolean addChair(Material mat) {
		List<String> newlist = getConfig().getStringList("chairs");
		if(!newlist.contains(mat.name())) {
			newlist.add(mat.name());
			getConfig().set("chairs", newlist);
			loadConfig();
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		ArrayList<String> list = new ArrayList<String>();
		if(args.length == 0 || args[0].equalsIgnoreCase("")) {
			list.add("add");
			list.add("remove");
			list.add("list");
		}else {
			if(args.length == 1 || args[1].equalsIgnoreCase("")) {
				if("add".startsWith(args[0]) && !args[0].equalsIgnoreCase("add")) list.add("add");
				if("remove".startsWith(args[0]) && !args[0].equalsIgnoreCase("remove")) list.add("remove");
				if("list".startsWith(args[0]) && !args[0].equalsIgnoreCase("list")) list.add("list");
			}else {
				if(args[0].equalsIgnoreCase("remove")) {
					list.addAll(getConfig().getStringList("chairs"));
				}
				if(args[0].equalsIgnoreCase("add")) {
					List<String> chairs = getConfig().getStringList("chairs");
					for(Material mat : Material.values()) {
						if(!chairs.contains(mat.name()) && args[1].startsWith(mat.name())
								&& !mat.name().equalsIgnoreCase(Material.AIR.name())
								&& !mat.name().equalsIgnoreCase(Material.CAVE_AIR.name())
								&& !mat.name().equalsIgnoreCase(Material.VOID_AIR.name())
								) list.add(mat.name());
					}
				}
			}
		}



		return list;
	}

	@EventHandler
	private void onSit(PlayerInteractEvent e) {
		if((e.getPlayer().getInventory().getItem(e.getHand()).getType().equals(Material.AIR) || e.getPlayer().getInventory().getItem(e.getHand()).getType() == null) 
				&& e.getClickedBlock() != null 
				&& e.getAction().equals(Action.RIGHT_CLICK_BLOCK) 
				&& !e.getPlayer().isSneaking()){
			for(Entity ent : e.getPlayer().getNearbyEntities(1, 1, 1)) if(ent instanceof Arrow && ent.getCustomName().equals("Chair") && ent.getPassengers().contains(e.getPlayer())) return;
			if(!getConfig().getStringList("chairs").contains(e.getClickedBlock().getType().name())) return;
			Arrow ar = e.getPlayer().getWorld().spawn(e.getClickedBlock().getLocation().add(new Vector(.5, .1, .5)), Arrow.class);
			ar.setPickupStatus(PickupStatus.DISALLOWED);
			ar.setCustomName("Chair");
			ar.getPersistentDataContainer().set(locationKeyX, PersistentDataType.DOUBLE, e.getPlayer().getLocation().getX());
			ar.getPersistentDataContainer().set(locationKeyY, PersistentDataType.DOUBLE, e.getPlayer().getLocation().getY());
			ar.getPersistentDataContainer().set(locationKeyZ, PersistentDataType.DOUBLE, e.getPlayer().getLocation().getZ());
			ar.getPersistentDataContainer().set(locationKeyYaw, PersistentDataType.FLOAT, e.getPlayer().getLocation().getYaw());
			ar.getPersistentDataContainer().set(locationKeyPitch, PersistentDataType.FLOAT, e.getPlayer().getLocation().getPitch());
			ar.addPassenger(e.getPlayer());
			ar.setSilent(true);
			ar.setGravity(false);
			ar.setPersistent(true);
		}
	}

	@EventHandler
	private void onStand(EntityDismountEvent e) {
		if(e.getDismounted() instanceof Arrow && e.getDismounted().getCustomName().equals("Chair")) {
			Arrow ar = (Arrow) e.getDismounted();
			Location loc = ar.getLocation();
			if(ar.getPersistentDataContainer().has(locationKeyX, PersistentDataType.DOUBLE)) loc.setX(ar.getPersistentDataContainer().get(locationKeyX, PersistentDataType.DOUBLE));
			if(ar.getPersistentDataContainer().has(locationKeyY, PersistentDataType.DOUBLE)) loc.setY(ar.getPersistentDataContainer().get(locationKeyY, PersistentDataType.DOUBLE));
			if(ar.getPersistentDataContainer().has(locationKeyZ, PersistentDataType.DOUBLE)) loc.setZ(ar.getPersistentDataContainer().get(locationKeyZ, PersistentDataType.DOUBLE));
			if(ar.getPersistentDataContainer().has(locationKeyYaw, PersistentDataType.FLOAT)) loc.setYaw(ar.getPersistentDataContainer().get(locationKeyYaw, PersistentDataType.FLOAT));
			if(ar.getPersistentDataContainer().has(locationKeyPitch, PersistentDataType.FLOAT)) loc.setPitch(ar.getPersistentDataContainer().get(locationKeyPitch, PersistentDataType.FLOAT));

			ar.remove();
			e.getEntity().teleport(loc);
		}
	}
}
