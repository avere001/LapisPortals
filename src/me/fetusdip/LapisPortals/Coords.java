package me.fetusdip.LapisPortals;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class Coords {
	private Location location;

	Coords(Location loc) {
		setLocation(loc);
	}

	public double getX() {
		return this.location.getX();
	}

	public double getY() {
		return this.location.getY();
	}

	public double getZ() {
		return this.location.getZ();
	}

	public Block getBlockRel(double x, double y, double z) {
		Location loc = getLocation();
		Block block = loc.add(x, y, z).getBlock();
		return block;
	}

	public Block getBlockDir(int dir, double x) {
		Block block = null;
		Location loc = getLocation();
		if (dir == 0) {
			block = loc.add(0.0D, 1.0D, x).getBlock();
			loc.add(0.0D, -1.0D, -x);
		} else if (dir == 1) {
			block = loc.add(-x, 1.0D, 0.0D).getBlock();
			loc.add(x, -1.0D, 0.0D);
		} else if (dir == 2) {
			block = loc.add(0.0D, 1.0D, -x).getBlock();
			loc.add(0.0D, -1.0D, x);
		} else if (dir == 3) {
			block = loc.add(x, 1.0D, 0.0D).getBlock();
			loc.add(-x, -1.0D, 0.0D);
		}
		return block;
	}

	public void setLocation(Location loc) {
		this.location = loc.clone();
	}

	public Location getLocation() {
		return new Location(this.location.getWorld(), this.location.getX(),
				this.location.getY(), this.location.getZ(),
				this.location.getYaw(), this.location.getPitch());
	}
}