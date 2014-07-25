package me.fetusdip.LapisPortals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class FileHandler {
	public EnderPortals plugin;
	Logger log = Logger.getLogger("Minecraft");
	List<EnderPortal> portals = new ArrayList<EnderPortal>();
	List<EnderPortal> globalportals = new ArrayList<EnderPortal>();

	public FileHandler(EnderPortals newPlugin) {
		this.plugin = newPlugin;

		this.portals = loadFile("Portals.dat", 0);
		this.globalportals = loadFile("Global.dat", 1);
	}

	public List<EnderPortal> loadFile(String filename, int type) {
		List<EnderPortal> portals = new ArrayList<EnderPortal>();
		try {
			File file = new File(this.plugin.getDataFolder().getPath()
					+ File.separatorChar + filename);
			file.getParentFile().mkdirs();
			if (file.createNewFile()) {
				this.log.info(filename + " not found: creating");
			}
			BufferedReader reader = new BufferedReader(new FileReader(
					this.plugin.getDataFolder().getPath() + File.separatorChar
							+ filename));
			ArrayList<String> list = new ArrayList<String>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				list.add(line);
			}
			for (String string : list) {
				String[] tokens = string.split("[:]");
				String[] values = new String[7];
				values = tokens[0].split("[.]");
				EnderPortal enderPortal = new EnderPortal(values[0],
						Integer.parseInt(values[1]),
						Integer.parseInt(values[2]),
						Integer.parseInt(values[3]),
						Integer.parseInt(values[4]),
						Integer.parseInt(values[5]), type);
				if (type == 1) {
					for (int jjj = 1; jjj < tokens.length; jjj++) {
						values = tokens[jjj].split("[.]");
						EnderPortal portal = new EnderPortal(values[1],
								Integer.parseInt(values[2]),
								Integer.parseInt(values[3]),
								Integer.parseInt(values[4]),
								Integer.parseInt(values[5]),
								Integer.parseInt(values[6]));
						enderPortal.setPlayerPortal(values[0], portal);
					}
				}
				if (enderPortal != null) {
					portals.add(enderPortal);
				}
			}
			if (reader != null) {
				reader.close();
			}
			return portals;
		} catch (IOException e) {
			this.log.severe("ERROR LOADING/CREATING " + filename
					+ "! PLEASE REPORT THIS TO FETUSDIP");
		}
		return portals;
	}

	public void save() {
		purgeInvalidPortals();
		try {
			File file = new File(this.plugin.getDataFolder().getPath()
					+ File.separatorChar + "Portals.dat");
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			if (this.portals != null) {
				for (EnderPortal p : this.portals) {
					writer.println(p.getWorldName() + "." + p.getF() + "."
							+ p.getX() + "." + p.getY() + "." + p.getZ() + "."
							+ p.getHash());
				}
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			this.log.severe("An error occured while saving Portals.dat, please report this to fetusdip");
		}
		try {
			File file = new File(this.plugin.getDataFolder().getPath()
					+ File.separatorChar + "Global.dat");
			PrintWriter writer = new PrintWriter(new FileWriter(file));
			if (this.globalportals != null) {
				for (EnderPortal gp : this.globalportals) {
					writer.print(gp.getWorldName() + "." + gp.getF() + "."
							+ gp.getX() + "." + gp.getY() + "." + gp.getZ()
							+ "." + gp.getHash());
					for (EnderPortal.PlayerFromPortal pfp : gp.getFromList()) {
						EnderPortal listPort = pfp.portal;
						writer.print(":" + pfp.playerName + "."
								+ listPort.getWorldName() + "."
								+ Integer.toString(listPort.getF()) + "."
								+ Integer.toString(listPort.getX()) + "."
								+ Integer.toString(listPort.getY()) + "."
								+ Integer.toString(listPort.getZ()) + "."
								+ Integer.toString(listPort.getHash()));
					}
					writer.print("\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			this.log.severe("An error occured while saving Global.dat, please report this to fetusdip");
		}
	}

	public void purgeInvalidPortals() {
		ListIterator<EnderPortal> itr = null;

		itr = this.globalportals.listIterator();
		while (itr.hasNext()) {
			EnderPortal p = (EnderPortal) itr.next();
			Material type = p.getLocation().clone().add(0.0D, 1.0D, 0.0D)
					.getBlock().getType();

			if ((p.isWorldLoaded())
					&& (type != Material.WOODEN_DOOR && type != Material.IRON_DOOR_BLOCK)) {
				itr.remove();
				purgePortals(p.getHash());
			}
		}

		itr = this.portals.listIterator();
		while (itr.hasNext()) {
			EnderPortal p = (EnderPortal) itr.next();
			Material type = p.getLocation().clone().add(0.0D, 1.0D, 0.0D)
					.getBlock().getType();

			if ((p.isWorldLoaded())
					&& (type != Material.WOODEN_DOOR && type != Material.IRON_DOOR_BLOCK)) {
				itr.remove();
			}
		}
	}

	public void purgePortals(int hash) {
		ListIterator<EnderPortal> itr = this.portals.listIterator();
		while (itr.hasNext()) {
			EnderPortal p = (EnderPortal) itr.next();
			if (p.getHash() == hash) {
				itr.remove();
			}
		}
	}

	public boolean isPortal(Block block) {
		Coords coords = new Coords(block.getLocation());

		boolean isPortal = false;

		ListIterator<EnderPortal> itr = this.globalportals.listIterator();
		while ((itr.hasNext()) && (!isPortal)) {
			EnderPortal p = (EnderPortal) itr.next();
			Location temp = p.getLocation();
			if ((temp.equals(coords.getBlockRel(0.0D, -1.0D, 0.0D)))
					|| (temp == coords.getBlockRel(0.0D, -2.0D, 0.0D))) {
				isPortal = true;
			}
		}
		itr = this.portals.listIterator();
		while ((itr.hasNext()) && (!isPortal)) {
			EnderPortal p = (EnderPortal) itr.next();
			Location temp = p.getLocation();
			if ((temp == coords.getBlockRel(0.0D, -1.0D, 0.0D))
					|| (temp == coords.getBlockRel(0.0D, -2.0D, 0.0D))) {
				isPortal = true;
			}
		}
		return isPortal;
	}

	public EnderPortal onPortal(Player p) {
		Location pLoc = p.getLocation().add(0.0D, -1.0D, 0.0D).getBlock()
				.getLocation();
		for (EnderPortal portal : this.globalportals) {
			if (portal.isWorldLoaded()) {
				Location portBlock = portal.getLocation().getBlock()
						.getLocation();
				if (pLoc.equals(portBlock)) {
					return portal;
				}
			}
		}

		Iterator<EnderPortal> pitr = portals.iterator();
		while (pitr.hasNext()) {
			EnderPortal portal = pitr.next();
			if (portal.isWorldLoaded()) {
				Location portBlock = portal.getLocation().getBlock()
						.getLocation();
				if (pLoc.equals(portBlock)) {
					return portal;
				}
			}
		}
		return null;
	}

	public EnderPortal getPortalBlock(Block block) {
		for (EnderPortal port : this.globalportals) {
			if (port.contains(block)) {
				return port;
			}
		}
		for (EnderPortal port : this.portals) {
			if (port.contains(block)) {
				return port;
			}
		}
		return null;
	}
	
	public boolean isPortalDoor(Block door) {
		Block block = door.getRelative(BlockFace.DOWN);
		
		for (EnderPortal port : this.portals) {
			if (block.equals(port.getMainBlock()))
				return true;
		}
		
		for (EnderPortal port : this.globalportals) {
			if (block.equals(port.getMainBlock()))
				return true;
		}
		
		return false;
	}

	public void removePortal(EnderPortal portal) {
		if (portal.isWorldLoaded()) {
			ListIterator<EnderPortal> itr = this.portals.listIterator();
			while (itr.hasNext()) {
				EnderPortal port = (EnderPortal) itr.next();
				if (portal.equals(port)) {
					itr.remove();
					return;
				}
			}
			itr = this.globalportals.listIterator();
			while (itr.hasNext()) {
				EnderPortal port = (EnderPortal) itr.next();
				if (portal.equals(port)) {
					purgePortals(port.getHash());
					itr.remove();
					return;
				}
			}
		}
	}

	public boolean canUseHash(int hash) {
		int hashCount = 0;
		for (EnderPortal port : this.portals) {
			if (port.getHash() == hash) {
				hashCount++;
			}
		}
		if (hashCount > 1) {
			return false;
		}
		return true;
	}

	public Location getTpLocation(EnderPortal portal) {
		EnderPortal port = getTpPortal(portal);
		if (port != null) {
			return port.getLocation().clone().add(0.5D, 1.0D, 0.5D);
		}
		return null;
	}

	public EnderPortal getTpPortal(EnderPortal portal) {
		for (EnderPortal port : this.globalportals) {
			if ((port.isWorldLoaded()) && (!port.equals(portal))
					&& (port.getHash() == portal.getHash())) {
				return port;
			}
		}
		for (EnderPortal port : this.portals) {
			if ((port.isWorldLoaded()) && (!port.equals(portal))
					&& (port.getHash() == portal.getHash())) {
				return port;
			}
		}
		return null;
	}

	public boolean addPortal(String world, int f, double x, double y, double z,
			int hash) {
		EnderPortal portal = new EnderPortal(world, f, x, y, z, hash);
		if (portal.getLocation().add(0.0D, 4.0D, 0.0D).getBlock().getType() != Material.REDSTONE_TORCH_ON) {
			this.portals.add(portal);
		} else {
			if (isGlobalHash(hash)) {
				return false;
			}
			this.globalportals.add(new EnderPortal(world, f, x, y, z, hash, 1));
		}
		return true;
	}

	public boolean isGlobalHash(int hash) {
		for (EnderPortal portal : this.globalportals) {
			if (portal.getHash() == hash) {
				return true;
			}
		}
		return false;
	}
}
