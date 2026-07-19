package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BanListener implements Listener {

    private final DominionTweak plugin;

    public BanListener(DominionTweak plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getPlayer().hasPermission("dominiontweak.bypass.ban")) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // 只有跨越不同方塊時才檢查，以節省效能
        if (to == null || (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ())) {
            return;
        }

        DominionAPI api = DominionAPI.getInstance();
        if (api == null) return;

        DominionDTO toDom = api.getDominion(to);
        DominionDTO fromDom = api.getDominion(from);

        // 如果目標位置在領地內，且與原本位置不在同一個領地 (跨越邊界)
        if (toDom != null && (fromDom == null || !fromDom.getId().equals(toDom.getId()))) {
            if (plugin.getBanManager().isBanned(toDom.getOwner(), event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getMessageManager().get("ban.banned_move"));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getPlayer().hasPermission("dominiontweak.bypass.ban")) return;

        Location to = event.getTo();
        if (to == null) return;

        DominionAPI api = DominionAPI.getInstance();
        if (api == null) return;

        DominionDTO toDom = api.getDominion(to);
        if (toDom != null) {
            if (plugin.getBanManager().isBanned(toDom.getOwner(), event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(plugin.getMessageManager().get("ban.banned_teleport"));
            }
        }
    }
}
