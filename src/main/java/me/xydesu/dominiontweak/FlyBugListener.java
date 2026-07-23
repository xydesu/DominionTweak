package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.DominionDTO;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class FlyBugListener implements Listener {

    private final DominionTweak plugin;

    public FlyBugListener(DominionTweak plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        checkAndDisableFly(event.getPlayer(), event.getTo());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        checkAndDisableFly(event.getPlayer(), event.getPlayer().getLocation());
    }

    private void checkAndDisableFly(Player player, Location location) {
        if (location == null) return;
        
        // 如果玩家是創造或旁觀模式，則不影響其飛行狀態
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        
        DominionAPI api = DominionAPI.getInstance();
        if (api == null) return;

        DominionDTO dom = api.getDominion(location);
        
        // 如果不在任何領地內，強制關閉飛行
        if (dom == null) {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }
}
