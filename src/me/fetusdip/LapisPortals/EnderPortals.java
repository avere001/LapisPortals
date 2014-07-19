package me.fetusdip.LapisPortals;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EnderPortals extends JavaPlugin {
	public final PlayerListener playerListener = new PlayerListener(this);
	private static FileHandler fileHandler;

	public void onDisable() {
		getFileHandler().save();
		reloadConfig();
		saveConfig();
		PluginDescriptionFile pdfFile = getDescription();
		Messenger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " is now disabled.");
	}

	public void onEnable() {
		loadConfig();
		VaultHook.enable(this);
		EnderPortal.initialize(this);
		Messenger.init(this);

		setFileHandler(new FileHandler(this));
		PluginDescriptionFile pdfFile = getDescription();
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.playerListener, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Runnable() {
					public void run() {
						EnderPortals.getFileHandler().save();
					}
				}, 600L, 600L);
		Messenger.info(pdfFile.getName() + " " + pdfFile.getVersion()
				+ " is now enabled.");
	}

	public static FileHandler getFileHandler() {
		return fileHandler;
	}

	public static void setFileHandler(FileHandler fileHandler) {
		EnderPortals.fileHandler = fileHandler;
	}

	public void loadConfig() {
		
		String PORTAL_MAT = "lapis_block";
		int PRICE = 0;
		boolean LIGHTNING_ENABLED = true;
		boolean SICKNESS_ENABLED = true;
		boolean PERMISSIONS_ECON_ENABLED = true;
		int TELEPORT_DELAY = 0;

		getConfig().addDefault("PortalMaterial", PORTAL_MAT);
		getConfig().addDefault("Price", Integer.valueOf(PRICE));
		getConfig().addDefault("Lightning", Boolean.valueOf(LIGHTNING_ENABLED));
		getConfig().addDefault("TeleSickness", Boolean.valueOf(SICKNESS_ENABLED));
		getConfig().addDefault("UsePermsAndEcon", Boolean.valueOf(PERMISSIONS_ECON_ENABLED));
		getConfig().addDefault("TeleDelay", Integer.valueOf(TELEPORT_DELAY));
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
}