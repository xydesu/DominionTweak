package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import cn.lunadeer.dominion.api.dtos.flag.Flags;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BypassListener implements Listener {

    private final DominionTweak plugin;

    public BypassListener(DominionTweak plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!event.isCancelled()) return;
        
        Player player = event.getPlayer();
        if (!player.hasPermission("dominiontweak.bypass.walk") && !player.hasPermission("dominiontweak.bypass")) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        
        // 確保玩家確實有移動方塊，減少不必要的 API 呼叫
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        DominionDTO toDominion = DominionAPI.getInstance().getDominion(to);
        if (toDominion != null) {
            // 如果玩家在目標領地沒有 MOVE 權限，代表移動事件很可能是被 Dominion 攔截
            boolean hasMoveFlag = DominionAPI.getInstance().checkPrivilegeFlagSilence(toDominion, Flags.MOVE, player);
            if (!hasMoveFlag) {
                // 玩家擁有 bypass 權限，因此我們將事件解除取消，允許進入
                event.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.isCancelled()) return;
        
        Player player = event.getPlayer();
        if (!player.hasPermission("dominiontweak.bypass.tp") && !player.hasPermission("dominiontweak.bypass")) {
            return;
        }

        Location to = event.getTo();
        if (to == null) return;

        DominionDTO toDominion = DominionAPI.getInstance().getDominion(to);
        if (toDominion != null) {
            boolean hasTpFlag = DominionAPI.getInstance().checkPrivilegeFlagSilence(toDominion, Flags.TELEPORT, player);
            if (!hasTpFlag) {
                event.setCancelled(false);
            }
        }
    }
}
