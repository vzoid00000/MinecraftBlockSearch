package org.example.vzoid.minecraftBlockSearch;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class MinecraftBlockSearch extends JavaPlugin {

    private BlockSearchGame game;

    @Override
    public void onEnable() {
        getLogger().info("MinecraftBlockSearch has been enabled.");

        // Initialize the game logic
        game = new BlockSearchGame(this);

        // Register the event listener
        getServer().getPluginManager().registerEvents(new BlockCheckListener(game), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("MinecraftBlockSearch has been disabled.");
        if (game != null && game.isGameRunning()) {
            game.stopGame();
        }
    }

    // Command handling for /blocksearch start and /blocksearch stop
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("blocksearch")) {
            return false;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /blocksearch <start|stop>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("blocksearch.start")) {
                        if (!game.isGameRunning()) {
                            game.startGame();
                            player.sendMessage(ChatColor.GREEN + "Game has started! Players, find the block!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Game is already running.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to start the game.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can start the game.");
                }
                break;

            case "stop":
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("blocksearch.stop")) {
                        if (game.isGameRunning()) {
                            game.stopGame();
                            player.sendMessage(ChatColor.RED + "The game has been stopped and reset.");
                        } else {
                            player.sendMessage(ChatColor.RED + "No game is currently running.");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "You do not have permission to stop the game.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Only players can stop the game.");
                }
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Usage: /blocksearch <start|stop>");
                break;
        }
        return true;
    }
}
