package me.fetusdip.LapisPortals;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Directional;
import org.bukkit.material.Openable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {
	private static final Logger log = Logger.getLogger("Minecraft");
	EnderPortals plugin;

	public PlayerListener(EnderPortals newPlugin) {
		this.plugin = newPlugin;
	}

	@EventHandler
	public void onPlayerPlace(BlockPlaceEvent event) {

		if ((event.getBlockPlaced().getType() == Material.WOODEN_DOOR)
				&& (VaultHook.hasPermission(event.getPlayer(),
						VaultHook.Perm.CREATE))) {
			int facing = (((Directional) event.getBlockPlaced().getState().getData())
					.getFacing().ordinal() + 2) % 4;
			Location tmp = event.getBlock().getLocation();
			Location loc = new Location(tmp.getWorld(), tmp.getX(),
					tmp.getY() - 1.0D, tmp.getZ());
			Player p = event.getPlayer();
			ValidPortalReturn returned = EnderPortal.validateLocation(loc,
					facing, this.plugin);
			if (returned.isValid()) {
				if (EnderPortals.getFileHandler().addPortal(
						loc.getWorld().getName(), facing, loc.getX(),
						loc.getY(), loc.getZ(), returned.getHash())) {
					Messenger.tell(p, Messenger.Phrase.CREATE_SUCCESS);
				} else {
					Messenger.tell(p, Messenger.Phrase.CREATE_FAIL);
				}
			} else if (returned.getHash() == -1) {
				Messenger.tell(p, Messenger.Phrase.CREATE_FAIL);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		EnderPortal portal = EnderPortals.getFileHandler().onPortal(
				event.getPlayer());
		
		if (portal == null) return;
		
		if (		(portal.isStillValid(this.plugin))
				&& (event.getClickedBlock() != null)
				&& (VaultHook.hasPermission(event.getPlayer(),
						VaultHook.Perm.TELEPORT))) {
			if ((event.getClickedBlock().getType() == Material.WOODEN_DOOR)
					&& (event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
				Block tmpBlock = event.getClickedBlock();
				if (tmpBlock.getRelative(BlockFace.DOWN).getType() == Material.WOODEN_DOOR)
					tmpBlock = tmpBlock.getRelative(BlockFace.DOWN);
				Openable door = (Openable) tmpBlock.getState().getData();
				if (door.isOpen()) {
					Player p = event.getPlayer();
					Location toLoc = null;
					if (!portal.isGlobal()) {
						toLoc = EnderPortals.getFileHandler().getTpLocation(
								portal);
						if (toLoc != null) {
							EnderPortal toPortal = EnderPortals
									.getFileHandler().getTpPortal(portal);
							if (toPortal != null) {
								toPortal.setPlayerPortal(p, portal);
							}
						}
					} else {
						EnderPortal tmpPortal = portal.getPlayerPortal(p);
						if (tmpPortal != null) {
							toLoc = tmpPortal.getLocation();
						}
						if (toLoc == null) {
							Messenger.tell(p,
									Messenger.Phrase.TELEPORT_FAIL_UNBOUND);
						} else {
							toLoc = toLoc.clone().add(0.5D, 1.0D, 0.5D);
						}
					}
					if (toLoc != null) {
						if ((event.getPlayer().hasMetadata("lpLastTele"))
								&& (!VaultHook.hasPermission(event.getPlayer(),
										VaultHook.Perm.NO_DELAY))) {
							long dt = (System.currentTimeMillis() - ((MetadataValue) event
									.getPlayer().getMetadata("lpLastTele")
									.get(0)).asLong()) / 1000L;
							double delayRequired = this.plugin.getConfig()
									.getDouble("TeleDelay");
							if (dt < delayRequired) {
								Messenger.tell(event.getPlayer(), ChatColor.RED
										+ "You must wait "
										+ (int) (delayRequired - dt)
										+ " more seconds");
								return;
							}
						}
						if (VaultHook.charge(event.getPlayer(), this.plugin
								.getConfig().getDouble("Price"))) {
							if (VaultHook.hasPermission(p,
									VaultHook.Perm.LIGHTNING)) {
								if (this.plugin.getConfig().getBoolean(
										"Lightning")) {
									portal.getLocation()
											.getWorld()
											.strikeLightningEffect(
													portal.getLocation()
															.clone()
															.add(0.0D, 3.0D,
																	0.0D));
									toLoc.getWorld()
											.strikeLightningEffect(
													toLoc.clone().add(0.0D,
															3.0D, 0.0D));
								}
							}
							if (!VaultHook.hasPermission(p,
									VaultHook.Perm.NO_SICKNESS)) {
								if (this.plugin.getConfig().getBoolean(
										"TeleSickness")) {
									p.addPotionEffect(new PotionEffect(
											PotionEffectType.CONFUSION, 300, 1));
									p.addPotionEffect(new PotionEffect(
											PotionEffectType.HUNGER, 300, 1));
									p.addPotionEffect(new PotionEffect(
											PotionEffectType.WEAKNESS, 300, 1));
								}
							}
							p.teleport(toLoc);
							BlockState toDoorState = toLoc.getBlock().getState();
							Openable toDoor = (Openable)toDoorState.getData();
							toDoor.setOpen(true);
							toDoorState.update();
							toLoc.getBlock()
									.getWorld()
									.playEffect(toLoc.getBlock().getLocation(),
											Effect.DOOR_TOGGLE, 0);
							p.setMetadata(
									"lpLastTele",
									new FixedMetadataValue(this.plugin,
											Long.valueOf(System
													.currentTimeMillis())));
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerBreak(BlockBreakEvent event) {
		EnderPortal port = EnderPortals.getFileHandler().getPortalBlock(
				event.getBlock());
		if (port != null) {
			EnderPortals.getFileHandler().removePortal(port);
		}
	}

	public static Logger getLog() {
		return log;
	}
}