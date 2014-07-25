package me.fetusdip.LapisPortals;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnderPortal {
	@SuppressWarnings("unused")
	private static EnderPortals plugin;
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger("Minecraft");
	private String worldName;
	private int f;
	private int x;
	private int y;
	private int z;
	private int chestHash;
	private Block[] lapis;
	private List<PlayerFromPortal> fromList;
	private int type;
	private boolean inited = false;

	public EnderPortal(String newWorld, int newF, double newX, double newY,
			double newZ, int hash) {
		initialize(newWorld, newF, newX, newY, newZ, hash, 0);
	}

	public EnderPortal(String newWorld, int newF, double newX, double newY,
			double newZ, int hash, int type) {
		initialize(newWorld, newF, newX, newY, newZ, hash, type);
	}

	public EnderPortal(Location loc) {
		initialize(loc.getWorld().toString(), 0, loc.getX(), loc.getY(),
				loc.getZ(), -1, 1);
	}

	public void initialize(String world, int f, double x, double y, double z,
			int hash, int type) {
		this.f = f;
		setWorldName(world);
		this.x = ((int) x);
		this.y = ((int) y);
		this.z = ((int) z);
		this.chestHash = hash;
		this.fromList = new ArrayList<PlayerFromPortal>();
		this.type = type;
		this.lapis = new Block[8];
	}

	private void initDependentFields() {
		Location loc = new Location(
				Bukkit.getServer().getWorld(this.worldName), this.x, this.y,
				this.z);
		Coords coords = new Coords(loc);
		this.lapis[7] = loc.getBlock();
		this.lapis[0] = loc.clone().add(0.0D, 3.0D, 0.0D).getBlock();
		int index = 1;
		for (int jjj = 0; jjj < 2; jjj++) {
			coords.setLocation(coords.getLocation().add(0.0D, jjj, 0.0D));
			int iii = (this.f + 1) % 4;
			while (iii != this.f) {
				this.lapis[index] = coords.getBlockDir(iii, 1.0D);
				index++;
				iii = (iii + 1) % 4;
			}
		}
		this.inited = true;
	}

	public static void initialize(EnderPortals plugin) {
		EnderPortal.plugin = plugin;
	}

	public List<PlayerFromPortal> getFromList() {
		return this.fromList;
	}

	public boolean isGlobal() {
		return this.type == 1;
	}

	public EnderPortal getPlayerPortal(Player player) {
		for (PlayerFromPortal playerPort : this.fromList) {
			if (playerPort.playerName.equals(player.getName())) {
				return playerPort.portal;
			}
		}
		return null;
	}

	public void setPlayerPortal(Player player, EnderPortal portal) {
		setPlayerPortal(player.getName(), portal);
	}

	public void setPlayerPortal(String player, EnderPortal portal) {
		ListIterator<PlayerFromPortal> itr = this.fromList.listIterator();
		while (itr.hasNext()) {
			PlayerFromPortal playerPort = (PlayerFromPortal) itr.next();
			if (playerPort.playerName.equals(player)) {
				itr.remove();
				break;
			}
		}
		this.fromList.add(new PlayerFromPortal(player, portal));
	}

	public Location getLocation() {
		if (Bukkit.getServer().getWorld(getWorldName()) == null) {
			return null;
		}
		Location loc = new Location(
				Bukkit.getServer().getWorld(getWorldName()), this.x, this.y,
				this.z, this.f * 90, 0.0F);
		return loc;
	}

	public int getF() {
		return this.f;
	}
	
	public Block getMainBlock() {
		return lapis[7];
	}

	public static ValidPortalReturn validateLocation(Location loc, int facing,
			EnderPortals plugin) {
		Material portalMaterial = Material.getMaterial(plugin.getConfig()
				.getString("PortalMaterial").toUpperCase());
		ValidPortalReturn returnVal = new ValidPortalReturn();

		Location[] aLoc = new Location[3];
		for (int iii = 0; iii < 3; iii++) {
			aLoc[iii] = loc.clone().add(0.0D, iii + 1, 0.0D);
		}
		boolean valid = true;
		if ((loc.getBlock().getType() == portalMaterial)
				&& (aLoc[0].getBlock().getType() == Material.WOODEN_DOOR || aLoc[0].getBlock().getType() == Material.IRON_DOOR_BLOCK)) {
			if ((aLoc[1].getBlock().getType() == Material.WOODEN_DOOR || aLoc[1].getBlock().getType() == Material.IRON_DOOR_BLOCK)
					&& (aLoc[2].getBlock().getType() == portalMaterial)) {
				
				for (int iii = 0; iii < 3; iii++) {
					Coords coords = new Coords(loc);
					int tmpFacing = (facing + 1 + iii) % 4;
					Block temp = coords.getBlockDir(tmpFacing, 1.0D);
					if ((temp.getType() != portalMaterial)
							|| (temp.getRelative(0, 1, 0).getType() != portalMaterial)) {
						valid = false;
						break;
					}
				}
				if (valid) {
					Coords chestLoc = new Coords(new Coords(loc.clone().add(
							0.0D, 2.0D, 0.0D)).getBlockDir((facing + 2) % 4,
							1.0D).getLocation());
					int hash = 0;
					Block identifier = chestLoc.getLocation().getBlock();
					Material mat = identifier.getType();
					if (mat == Material.CHEST) {
						ItemStack[] inv = ((Chest) identifier.getState())
								.getInventory().getContents();
						for (ItemStack items : inv) {
							if (items != null) {
								hash += items.hashCode();
							}
						}
					} else if ((mat == Material.WOOL)  
							|| (mat == Material.WOOD) 
							|| (mat == Material.LOG)
							|| (mat == Material.LEAVES)
							|| (mat == Material.SMOOTH_BRICK)
							|| (mat == Material.GLASS)) 
					{
						hash = identifier.getState().getData().hashCode();
					} else {
						valid = false;
					}
					if (valid) {
						boolean canUse = EnderPortals.getFileHandler().canUseHash(hash);
						boolean isGlobal = EnderPortals.getFileHandler().isGlobalHash(hash);
						if (canUse || isGlobal) {
							returnVal.setHash(hash);
							returnVal.setValid();
						} else {
							returnVal.setHash(-1);
						}
					}
					returnVal.setFace(facing);
				}
			}
		}
		return returnVal;
	}

	public boolean contains(Block block) {
		if (!isWorldLoaded()) {
			return false;
		}
		Location loc = getLocation();
		if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR) {
			if ((loc.add(0.0D, 1.0D, 0.0D).getBlock().equals(block))
					|| (loc.add(0.0D, 1.0D, 0.0D).getBlock().equals(block))) {
				return true;
			}
		} else {
			for (Block lapisBlock : this.lapis) {
				if (lapisBlock.equals(block)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isStillValid(EnderPortals plugin) {
		for (Block block : this.lapis) {
			if (block.getType() != Material.getMaterial(plugin.getConfig()
					.getString("PortalMaterial").toUpperCase())) {
				return false;
			}
		}
		return true;
	}

	public int getHash() {
		return this.chestHash;
	}

	String getWorldName() {
		return this.worldName;
	}

	void setWorldName(String worldName) {
		this.worldName = worldName;
	}

	public class PlayerFromPortal {
		public String playerName;
		public EnderPortal portal;

		PlayerFromPortal(Player player, EnderPortal portal) {
			this.playerName = player.getName();
			this.portal = portal;
		}

		PlayerFromPortal(String player, EnderPortal portal) {
			this.playerName = player;
			this.portal = portal;
		}
	}

	public boolean isWorldLoaded() {
		if (Bukkit.getServer().getWorld(getWorldName()) != null) {
			if (!this.inited) {
				initDependentFields();
			}
			return true;
		}
		return false;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}
}