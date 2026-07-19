package me.xydesu.dominiontweak;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessageManager {

    private final DominionTweak plugin;
    private FileConfiguration langConfig;

    public MessageManager(DominionTweak plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        String lang = plugin.getConfig().getString("language", "zh-TW");
        
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile.getParentFile().mkdirs();
            // 嘗試從資源中儲存預設語言檔
            if (plugin.getResource("lang/" + lang + ".yml") != null) {
                plugin.saveResource("lang/" + lang + ".yml", false);
            }
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);

        // 如果資源中有預設檔案，讀取並設定預設值
        InputStream defConfigStream = plugin.getResource("lang/" + lang + ".yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defConfig);
        }
    }

    /**
     * 取得翻譯訊息並處理顏色代碼與變數替換
     *
     * @param key 訊息鍵值
     * @param placeholders 變數，格式為 "key1", "value1", "key2", "value2" ...
     * @return 處理過後的訊息字串
     */
    public String get(String key, String... placeholders) {
        String message = langConfig.getString(key);
        if (message == null) {
            return ChatColor.RED + "Missing translation for: " + key;
        }

        for (int i = 0; i < placeholders.length - 1; i += 2) {
            String target = "{" + placeholders[i] + "}";
            String replacement = placeholders[i + 1] != null ? placeholders[i + 1] : "null";
            message = message.replace(target, replacement);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
