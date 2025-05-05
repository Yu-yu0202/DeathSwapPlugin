package com.yuyu0202.deathswap;

import org.jetbrains.annotations.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public final class DeathSwap extends JavaPlugin {
    private final Map<Player, Player> playerPairs = new HashMap<>();
    private int swapTime = 300;
    private BukkitRunnable currentTask;

    @Override
    public void onEnable() {
        getLogger().info("DeathSwap Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("DeathSwap Plugin disabled!");
        if (currentTask != null) {
            currentTask.cancel();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("deathswap")) {
            if (args.length < 1) {
                sender.sendMessage("Usage: /deathswap <add|start|stop|config>");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "add":
                    handleAddCommand(sender, args);
                    break;
                case "start":
                    handleStartCommand(sender);
                    break;
                case "stop":
                    handleStopCommand(sender);
                    break;
                case "config":
                    handleConfigCommand(sender, args);
                    break;
                case "delete":
                    handleDeleteCommand(sender, args);
                    break;
                case "clear":
                    handleClearCommand(sender);
                    break;
                default:
                    sender.sendMessage("Unknown command. Usage: /deathswap <add|start|stop|config>");
                    break;
            }
            return true;
        }
        return false;
    }

    private void handleAddCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /deathswap add <player1> <player2>");
            return;
        }

        Player player1 = Bukkit.getPlayer(args[1]);
        Player player2 = Bukkit.getPlayer(args[2]);

        if (player1 == null || player2 == null) {
            sender.sendMessage("One or both players are not online.");
            return;
        }

        playerPairs.put(player1, player2);
        sender.sendMessage(player1.getName() + " and " + player2.getName() + " are now linked for DeathSwap.");
        return;
    }

    private void handleStartCommand(CommandSender sender) {
        if (playerPairs.isEmpty()) {
            sender.sendMessage("No player pairs to start DeathSwap.");
            return;
        }

        sender.sendMessage("DeathSwap starting with " + playerPairs.size() + " pairs!");
        if (currentTask != null) {
            sender.sendMessage("DeathSwap is already running. Use /deathswap stop to stop it.");
            return;
        }

        currentTask = new BukkitRunnable() {
            private int countdown = swapTime;

            @Override
            public void run() {
                if (countdown > 0) {
                    if (countdown <= 5) {
                        Bukkit.broadcastMessage("Swap in " + countdown + " seconds...");
                    }
                    countdown--;
                } else {
                    for (Map.Entry<Player, Player> entry : playerPairs.entrySet()) {
                        Player player1 = entry.getKey();
                        Player player2 = entry.getValue();

                        if (player1.isOnline() && player2.isOnline()) {
                            Location loc1 = player1.getLocation();
                            Location loc2 = player2.getLocation();

                            player1.teleport(loc2);
                            player2.teleport(loc1);

                            player1.sendMessage("You have swapped!");
                            player2.sendMessage("You have swapped!");
                        }
                    }
                    countdown = swapTime;
                }
            }
        };
        currentTask.runTaskTimer(this, 0L, 20L);
        sender.sendMessage("DeathSwap started!");
        return;
    }

    private void handleStopCommand(CommandSender sender) {
        if (currentTask != null) {
            currentTask.cancel();
            currentTask = null;
            sender.sendMessage("DeathSwap stopped.");
            return;
        } else {
            sender.sendMessage("There is no running DeathSwap.");
            return;
        }
    }

    private void handleConfigCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /deathswap config <time> <value>");
            return;
        }

        if (args[1].equalsIgnoreCase("time")) {
            try {
                swapTime = Integer.parseInt(args[2]);
                sender.sendMessage("Swap time set to " + swapTime + " seconds.");
                return;
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid time value.");
                return;
            }
        }
    }

    private void handleDeleteCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /deathswap delete <player1> <player2>");
            return;
        }
        if (currentTask != null) {
            sender.sendMessage("This operation cannot be performed unless it is stopped");
            return;
        }
       try{
           playerPairs.remove(Bukkit.getPlayer(args[1]));
       } catch (Exception e) {
           try {
               playerPairs.remove(Bukkit.getPlayer(args[2]));
           } catch (Exception exception){
               sender.sendMessage("An error has occurred");
               getLogger().severe("[DeathSwap] Command Running Error:" + exception);
               return;
           }
       }
       sender.sendMessage("Successfully delete pair.");
       return;
    }

    private void handleClearCommand(CommandSender sender) {
        if (currentTask != null) {
            sender.sendMessage("This operation cannot be performed unless it is stopped");
            return;
        }
        try{
            playerPairs.clear();
        } catch (Exception exception){
            sender.sendMessage("An error has occurred");
            getLogger().severe("[DeathSwap] Command Running Error:" + exception);
            return;
        }
        sender.sendMessage("Successfully clear pair.");
        return;
    }
}