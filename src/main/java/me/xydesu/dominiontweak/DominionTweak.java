package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.api.DominionAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DominionTweak extends JavaPlugin {

    private DominionAPI dominionAPI;
    private MessageManager messageManager;
    private BanManager banManager;

    public static cn.lunadeer.dominion.api.dtos.flag.PriFlag SET_HOME_FLAG;

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public BanManager getBanManager() {
        return banManager;
    }

    @Override
    public void onEnable() {
        messageManager = new MessageManager(this);
        banManager = new BanManager(this);

        if (Bukkit.getPluginManager().isPluginEnabled("Dominion")) {
            dominionAPI = DominionAPI.getInstance();
            getLogger().info(messageManager.get("plugin.api_loaded"));
            
            // 註冊自訂權限標籤
            SET_HOME_FLAG = new cn.lunadeer.dominion.api.dtos.flag.PriFlag(
                "sethome", 
                org.bukkit.ChatColor.stripColor(messageManager.get("flag.sethome.name")), 
                org.bukkit.ChatColor.stripColor(messageManager.get("flag.sethome.description")), 
                false, 
                true, 
                org.bukkit.Material.RED_BED
            );
            cn.lunadeer.dominion.api.dtos.flag.Flags.registerPriFlag(this, SET_HOME_FLAG);
            try {
                cn.lunadeer.dominion.api.dtos.flag.Flags.applyNewCustomFlags();
                getLogger().info("Successfully registered and applied custom flag 'sethome'.");
            } catch (Exception e) {
                getLogger().severe("Failed to apply custom flags: " + e.getMessage());
            }
        } else {
            getLogger().severe(messageManager.get("plugin.api_not_found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 註冊事件監聽器
        getServer().getPluginManager().registerEvents(new ConfirmListener(this), this);
        getServer().getPluginManager().registerEvents(new HomeListener(this), this);
        getServer().getPluginManager().registerEvents(new BanListener(this), this);
        
        // 註冊指令
        BanCommand banCommand = new BanCommand(this);
        getCommand("dt").setExecutor(banCommand);
        getCommand("dt").setTabCompleter(banCommand);
        
        getLogger().info(messageManager.get("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        if (messageManager != null) {
            getLogger().info(messageManager.get("plugin.disabled"));
        } else {
            getLogger().info("DominionTweak 已停用。");
        }
    }
}
