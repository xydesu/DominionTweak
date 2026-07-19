package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class HomeListener implements Listener {

    private final DominionTweak plugin;

    // 支援 HuskHomes, CMI, EssentialsX 的設定家指令
    private static final List<String> SET_HOME_COMMANDS = Arrays.asList(
            "/sethome", "/esethome", "/createhome", "/ecreatehome", 
            "/cmi sethome", "/cmi:sethome", "/huskhomes:sethome", "/essentials:sethome",
            "/essentials:esethome", "/essentials:createhome", "/essentials:ecreatehome"
    );

    public HomeListener(DominionTweak plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        
        boolean isSetHomeCommand = false;
        for (String cmd : SET_HOME_COMMANDS) {
            // 檢查是否完全吻合或帶有參數 (例如 /sethome myhome)
            if (message.equals(cmd) || message.startsWith(cmd + " ")) {
                isSetHomeCommand = true;
                break;
            }
        }

        if (!isSetHomeCommand) {
            return;
        }

        Player player = event.getPlayer();

        DominionAPI api = DominionAPI.getInstance();
        if (api == null) {
            return;
        }

        DominionDTO dom = api.getDominion(player.getLocation());
        if (dom != null) {
            boolean hasPermission = false;

            // 1. 檢查是否為主人或管理員
            if (player.getUniqueId().equals(dom.getOwner()) || player.isOp() || player.hasPermission("dominion.admin") || player.hasPermission("dominiontweak.bypass.sethome")) {
                hasPermission = true;
            } else {
                // 2. 判斷玩家是否為領地成員
                cn.lunadeer.dominion.api.dtos.MemberDTO member = api.getMember(dom, player.getUniqueId());
                if (member != null) {
                    // 取出成員專屬設定
                    Boolean flagVal = member.getFlagValue(DominionTweak.SET_HOME_FLAG);
                    if (flagVal != null) {
                        hasPermission = flagVal;
                    } else {
                        // 繼承群組設定
                        cn.lunadeer.dominion.api.dtos.GroupDTO group = api.getGroup(member);
                        if (group != null) {
                            Boolean groupFlag = group.getFlagValue(DominionTweak.SET_HOME_FLAG);
                            hasPermission = groupFlag != null ? groupFlag : false;
                        } else {
                            // 都沒有的話，預設為 false
                            hasPermission = false;
                        }
                    }
                } else {
                    // 訪客，直接取訪客的旗標設定
                    hasPermission = dom.getGuestFlagValue(DominionTweak.SET_HOME_FLAG);
                }
            }

            if (!hasPermission) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessageManager().get("home.no_permission"));
            }
        }
    }
}
