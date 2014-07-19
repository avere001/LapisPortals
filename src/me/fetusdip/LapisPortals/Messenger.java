package me.fetusdip.LapisPortals;

import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Messenger {
	@SuppressWarnings("unused")
	private static EnderPortals plugin = null;
	private static boolean init = false;
	private static final Logger log = Logger.getLogger("Minecraft");
	private static final String prefix = ChatColor.DARK_AQUA + "["
			+ ChatColor.AQUA + "LapisPortals" + ChatColor.DARK_AQUA + "] "
			+ ChatColor.WHITE;

	public static enum Phrase {
		CREATE_SUCCESS(ChatColor.GREEN + "Portal created!"), CREATE_FAIL(
				ChatColor.RED + "Could not create portal: inventory in use"), TELEPORT_SUCCESS_FREE(
				ChatColor.GREEN + "Price to teleport: free"), TELEPORT_FAIL_UNBOUND(
				ChatColor.RED + "You have not yet bound yourself to a portal!");

		private String phrase;

		private Phrase(String phrase) {
			this.phrase = phrase;
		}

		public String get() {
			return this.phrase;
		}
	}

	public static void init(EnderPortals plugin) {
		if (!init) {
			Messenger.plugin = plugin;
			init = true;
		}
	}

	public static void info(Object o) {
		log.info("[LapisPortals] " + o.toString());
	}

	public static void severe(Object o) {
		log.severe("[LapisPortals] " + o.toString());
	}

	public static void tell(Player p, Phrase phrase) {
		p.sendMessage(prefix + phrase.get());
	}

	public static void tell(Player p, String string) {
		p.sendMessage(prefix + string);
	}

	public static void broadcast(Object o) {
		Bukkit.getServer().broadcastMessage(prefix + o.toString());
	}
}
