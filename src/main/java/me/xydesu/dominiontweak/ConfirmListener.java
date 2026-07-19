package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.events.dominion.DominionCreateEvent;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfirmListener implements Listener {

    private final DominionTweak plugin;
    private static class CommandCache {
        String command;
        long timestamp;

        CommandCache(String command) {
            this.command = command;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private final Map<UUID, CommandCache> lastCommand = new HashMap<>();
    
    // 儲存待確認的操作
    private final Map<UUID, PendingAction> pendingActions = new HashMap<>();

    public ConfirmListener(DominionTweak plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastCommand.remove(uuid);
        pendingActions.remove(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage();
        // 攔截並快取玩家最後一次輸入的 Dominion 指令
        if (cmd.startsWith("/dom ") || cmd.startsWith("/dominion ") || cmd.equals("/dom") || cmd.equals("/dominion")) {
            lastCommand.put(event.getPlayer().getUniqueId(), new CommandCache(cmd));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDominionCreate(DominionCreateEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getOperator() instanceof Player)) return; // 若非玩家操作則不攔截

        // 檢查是否由 SweetAutoResidence 觸發，如果是則直接放行（因為它有自己的確認機制）
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().contains("top.mrxiaom.sweet.autores")) {
                return;
            }
        }

        Player player = (Player) event.getOperator();

        UUID uuid = player.getUniqueId();
        PendingAction pending = pendingActions.get(uuid);
        CommandCache cache = lastCommand.get(uuid);
        String currentCommand = cache != null ? cache.command : null;

        if (pending != null && pending.isValid() && pending.actionType.equals("CREATE")) {
            // 確認他們執行的指令是否與待確認的指令相同（或是 fallback 的預設指令）
            if (currentCommand != null && currentCommand.equals(pending.rawCommand)) {
                // 確認成功！允許事件繼續進行
                pendingActions.remove(uuid);
                player.sendMessage(plugin.getMessageManager().get("confirm.success"));
                return;
            }
        }

        // 尚未確認，取消事件並要求確認
        event.setCancelled(true);

        // 判斷指令是否為玩家剛手動輸入的原生創建指令。
        // 若是在 2000 毫秒內執行的指令，且包含 create，判定為手動指令；否則判定為透過 GUI 觸發，給予 auto_create 作為確認指令。
        String cmdToConfirm;
        if (cache != null && (System.currentTimeMillis() - cache.timestamp) < 2000 && currentCommand.contains("create")) {
            cmdToConfirm = currentCommand;
        } else {
            cmdToConfirm = "/dom auto_create " + event.getName();
        }
        
        pendingActions.put(uuid, new PendingAction("CREATE", cmdToConfirm));

        sendConfirmMessage(player, "建立領地 '" + event.getName() + "'", cmdToConfirm);
    }



    private void sendConfirmMessage(Player player, String actionText, String cmd) {
        MessageManager msg = plugin.getMessageManager();
        player.sendMessage(msg.get("confirm.attempt_action", "action", actionText));
        
        // 注意：ClickEvent 等需要 TextComponent 的還是可以用原生的字串或從 config 取
        // 但這裡 msg.get() 回傳包含顏色的字串，對於 Bungee API 來說可直接放入 TextComponent(舊版相容寫法)
        TextComponent confirmMsg = new TextComponent(msg.get("confirm.click_to_confirm"));
        
        // 設定點擊事件：直接由玩家再次送出該指令
        confirmMsg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
        confirmMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(msg.get("confirm.hover_run", "command", cmd)).create()));

        TextComponent fullMsg = new TextComponent(msg.get("confirm.time_limit"));
        fullMsg.addExtra(confirmMsg);
        
        player.spigot().sendMessage(fullMsg);
    }

    private static class PendingAction {
        long timestamp;
        String actionType;
        String rawCommand;

        PendingAction(String actionType, String rawCommand) {
            this.timestamp = System.currentTimeMillis();
            this.actionType = actionType;
            this.rawCommand = rawCommand;
        }

        boolean isValid() {
            // 確認有效期限為 60 秒
            return (System.currentTimeMillis() - timestamp) < 60000;
        }
    }
}
