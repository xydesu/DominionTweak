package me.xydesu.dominiontweak;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.PlayerDTO;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BanCommand implements CommandExecutor, TabCompleter {

    private final DominionTweak plugin;

    public BanCommand(DominionTweak plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        MessageManager msg = plugin.getMessageManager();

        if (args.length > 0) {
            String subCmd = args[0].toLowerCase();
            
            if (subCmd.equals("reload")) {
                if (!sender.hasPermission("dominiontweak.admin")) {
                    sender.sendMessage(msg.get("admin.no_permission"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getMessageManager().loadLanguage();
                plugin.getBanManager().loadBans();
                sender.sendMessage(msg.get("admin.reload_success"));
                return true;
            }
            
            if (subCmd.equals("admin")) {
                if (!sender.hasPermission("dominiontweak.admin")) {
                    sender.sendMessage(msg.get("admin.no_permission"));
                    return true;
                }
                
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "用法: /dt admin <ban|unban|list|clear> <擁有者> [目標玩家]");
                    return true;
                }
                
                String adminAction = args[1].toLowerCase();
                String ownerName = args[2];
                DominionAPI api = DominionAPI.getInstance();
                PlayerDTO ownerDTO = api != null ? api.getPlayer(ownerName) : null;
                
                if (ownerDTO == null) {
                    sender.sendMessage(msg.get("command.player_not_found", "player", ownerName));
                    return true;
                }
                
                BanManager banManager = plugin.getBanManager();
                
                if (adminAction.equals("clear")) {
                    banManager.clearBans(ownerDTO.getUuid());
                    sender.sendMessage(msg.get("admin.clear_success", "player", ownerName));
                    return true;
                }
                
                if (adminAction.equals("list")) {
                    List<String> banned = banManager.getBannedPlayers(ownerDTO.getUuid());
                    sender.sendMessage(msg.get("admin.list_header", "player", ownerName));
                    if (banned.isEmpty()) {
                        sender.sendMessage(msg.get("admin.list_empty"));
                    } else {
                        for (String n : banned) {
                            sender.sendMessage(ChatColor.GRAY + "- " + n);
                        }
                    }
                    return true;
                }
                
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "用法: /dt admin " + adminAction + " " + ownerName + " <目標玩家>");
                    return true;
                }
                
                String targetName = args[3];
                PlayerDTO targetDTO = api.getPlayer(targetName);
                if (targetDTO == null) {
                    sender.sendMessage(msg.get("command.player_not_found", "player", targetName));
                    return true;
                }
                
                if (adminAction.equals("ban")) {
                    banManager.banPlayer(ownerDTO.getUuid(), targetDTO.getUuid());
                    sender.sendMessage(msg.get("admin.ban_success", "owner", ownerName, "target", targetName));
                } else if (adminAction.equals("unban")) {
                    banManager.unbanPlayer(ownerDTO.getUuid(), targetDTO.getUuid());
                    sender.sendMessage(msg.get("admin.unban_success", "owner", ownerName, "target", targetName));
                }
                return true;
            }
        }

        // 一般玩家功能
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.get("command.player_only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(msg.get("command.usage"));
            return true;
        }

        String action = args[0].toLowerCase();
        String targetName = args[1];

        if (!action.equals("ban") && !action.equals("unban")) {
            player.sendMessage(msg.get("command.unknown_subcommand"));
            return true;
        }

        DominionAPI api = DominionAPI.getInstance();
        if (api == null) {
            player.sendMessage(msg.get("command.api_error"));
            return true;
        }

        PlayerDTO targetDTO = api.getPlayer(targetName);
        if (targetDTO == null) {
            player.sendMessage(msg.get("command.player_not_found", "player", targetName));
            return true;
        }

        BanManager banManager = plugin.getBanManager();
        boolean changed = false;

        if (action.equals("ban")) {
            changed = banManager.banPlayer(player.getUniqueId(), targetDTO.getUuid());
            if (changed) {
                player.sendMessage(msg.get("command.ban_success", "player", targetName));
            } else {
                player.sendMessage(ChatColor.YELLOW + targetName + " 已經在您的黑名單中了。");
            }
        } else {
            changed = banManager.unbanPlayer(player.getUniqueId(), targetDTO.getUuid());
            if (changed) {
                player.sendMessage(msg.get("command.unban_success", "player", targetName));
            } else {
                player.sendMessage(ChatColor.YELLOW + targetName + " 不在您的黑名單中。");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("ban", "unban"));
            if (sender.hasPermission("dominiontweak.admin")) {
                suggestions.add("reload");
                suggestions.add("admin");
            }
            return suggestions.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("dominiontweak.admin")) {
                return Arrays.asList("ban", "unban", "list", "clear").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("unban")) {
                String prefix = args[1].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("dominiontweak.admin")) {
                String prefix = args[2].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(prefix))
                        .collect(Collectors.toList());
            }
        } else if (args.length == 4) {
            if (args[0].equalsIgnoreCase("admin") && sender.hasPermission("dominiontweak.admin")) {
                String subCmd = args[1].toLowerCase();
                if (subCmd.equals("ban") || subCmd.equals("unban")) {
                    String prefix = args[3].toLowerCase();
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(prefix))
                            .collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
