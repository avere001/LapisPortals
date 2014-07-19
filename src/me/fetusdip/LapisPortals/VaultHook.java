package me.fetusdip.LapisPortals;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook
{
  static boolean isEnabled;
  public static EnderPortals plugin;
  public static Economy econ;
  public static Permission perms;
  
  public static enum Perm
  {
    CREATE("create", true),  TELEPORT("teleport", true),  LIGHTNING("teleport.lightning", true),  NO_SICKNESS("teleport.nosick", false),  FREE("teleport.free", false),  NO_DELAY("teleport.nodelay", false);
    
    private String name;
    private boolean defaultValue;
    
    private Perm(String name, boolean defaultValue)
    {
      this.name = name;
      this.defaultValue = defaultValue;
    }
    
    public String getName()
    {
      return "LapisPortals." + this.name;
    }
    
    public boolean getDefault()
    {
      return this.defaultValue;
    }
  }
  
  public static void enable(EnderPortals enderPortals)
  {
    try
    {
      plugin = enderPortals;
      isEnabled = false;
      boolean bEcon = setupPermissions();
      boolean bPerms = setupEconomy();
      if ((!bEcon) || (!bPerms)) {
        Messenger.info("No Econ/Perm found: perm/econ disabled for this plugin!");
      } else if (!plugin.getConfig().getBoolean("UsePermsAndEcon")) {
        Messenger.info("disabling permissions and economy for this plugin");
      } else {
        isEnabled = true;
      }
    }
    catch (NoClassDefFoundError e)
    {
      Messenger.info("No Vault found: perm/econ disabled for this plugin!");
    }
  }
  
  private static boolean setupPermissions()
  {
    RegisteredServiceProvider<Permission> rsp = plugin.getServer()
      .getServicesManager().getRegistration(Permission.class);
    perms = (Permission)rsp.getProvider();
    return perms != null;
  }
  
  private static boolean setupEconomy()
  {
    if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = plugin.getServer()
      .getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = (Economy)rsp.getProvider();
    return econ != null;
  }
  
  public static boolean charge(Player player, double price)
  {
    if (isEnabled)
    {
      if ((price == 0.0D) || 
        (hasPermission(player, Perm.FREE)))
      {
        Messenger.tell(player, Messenger.Phrase.TELEPORT_SUCCESS_FREE);
        return true;
      }
      OfflinePlayer oplayer = player.getPlayer();
      if (econ.getBalance(oplayer) >= price)
      {
        econ.withdrawPlayer(oplayer, price);
        Messenger.tell(player, 
          ChatColor.GREEN + "You have been charged " + price + 
          " " + econ.currencyNamePlural() + 
          ". new balance: " + econ.getBalance(oplayer));
        return true;
      }
      Messenger.tell(player, ChatColor.RED + 
        "You don't have the funds! you need at least " + price + 
        " " + econ.currencyNamePlural());
      return false;
    }
    return true;
  }
  
  public static boolean hasPermission(Player player, Perm perm)
  {
    if (isEnabled) {
      return perms.has(player, perm.getName());
    }
    return (perm.getDefault()) || (player.isOp());
  }
}