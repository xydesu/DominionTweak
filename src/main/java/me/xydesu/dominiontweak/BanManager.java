package me.xydesu.dominiontweak;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BanManager {

    private final DominionTweak plugin;
    private File banFile;
    private FileConfiguration banConfig;

    public BanManager(DominionTweak plugin) {
        this.plugin = plugin;
        loadBans();
    }

    public void loadBans() {
        banFile = new File(plugin.getDataFolder(), "bans.yml");
        if (!banFile.exists()) {
            banFile.getParentFile().mkdirs();
            try {
                banFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("無法建立 bans.yml: " + e.getMessage());
            }
        }
        banConfig = YamlConfiguration.loadConfiguration(banFile);
    }

    public void saveBans() {
        try {
            banConfig.save(banFile);
        } catch (IOException e) {
            plugin.getLogger().warning("無法儲存 bans.yml: " + e.getMessage());
        }
    }

    /**
     * 封鎖玩家
     * @param owner 領地主人 UUID
     * @param target 被封鎖玩家 UUID
     * @return 如果原本未封鎖並成功封鎖，回傳 true；若已封鎖，回傳 false
     */
    public boolean banPlayer(UUID owner, UUID target) {
        String key = owner.toString();
        List<String> bannedList = banConfig.getStringList(key);
        if (bannedList == null) {
            bannedList = new ArrayList<>();
        }
        
        String targetStr = target.toString();
        if (bannedList.contains(targetStr)) {
            return false;
        }
        
        bannedList.add(targetStr);
        banConfig.set(key, bannedList);
        saveBans();
        return true;
    }

    /**
     * 解除封鎖
     * @param owner 領地主人 UUID
     * @param target 被解除封鎖玩家 UUID
     * @return 如果原本有封鎖並成功解除，回傳 true；若未封鎖，回傳 false
     */
    public boolean unbanPlayer(UUID owner, UUID target) {
        String key = owner.toString();
        List<String> bannedList = banConfig.getStringList(key);
        if (bannedList == null) {
            return false;
        }
        
        String targetStr = target.toString();
        if (!bannedList.contains(targetStr)) {
            return false;
        }
        
        bannedList.remove(targetStr);
        banConfig.set(key, bannedList.isEmpty() ? null : bannedList);
        saveBans();
        return true;
    }

    /**
     * 檢查是否被封鎖
     * @param owner 領地主人 UUID
     * @param target 欲檢查的玩家 UUID
     * @return 是否被封鎖
     */
    public boolean isBanned(UUID owner, UUID target) {
        List<String> bannedList = banConfig.getStringList(owner.toString());
        return bannedList != null && bannedList.contains(target.toString());
    }

    /**
     * 取得某位玩家的黑名單玩家名稱列表
     * @param owner 領地主人 UUID
     * @return 名稱列表
     */
    public List<String> getBannedPlayers(UUID owner) {
        List<String> bannedList = banConfig.getStringList(owner.toString());
        List<String> names = new ArrayList<>();
        if (bannedList != null) {
            for (String uuidStr : bannedList) {
                try {
                    UUID u = UUID.fromString(uuidStr);
                    String name = org.bukkit.Bukkit.getOfflinePlayer(u).getName();
                    names.add(name != null ? name : uuidStr);
                } catch (IllegalArgumentException e) {
                    names.add(uuidStr);
                }
            }
        }
        return names;
    }

    /**
     * 清空某位玩家的黑名單
     * @param owner 領地主人 UUID
     * @return 成功清空回傳 true，若原本就是空的回傳 false
     */
    public boolean clearBans(UUID owner) {
        String key = owner.toString();
        if (banConfig.contains(key)) {
            banConfig.set(key, null);
            saveBans();
            return true;
        }
        return false;
    }
}
