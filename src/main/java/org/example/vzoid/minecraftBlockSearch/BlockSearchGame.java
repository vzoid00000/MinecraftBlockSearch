package org.example.vzoid.minecraftBlockSearch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockSearchGame {

    private final MinecraftBlockSearch plugin;
    private final Map<UUID, Integer> playerScores;
    private boolean gameRunning;
    private Material currentBlockType;
    private final List<Material> standableBlocks;
    private BukkitTask scheduledTask;

    private static final int WINNING_SCORE = 10;
    private static final long ROUND_DURATION_TICKS = 20L * 60 * 20; // 20 minutes

    public BlockSearchGame(MinecraftBlockSearch plugin) {
        this.plugin = plugin;
        this.playerScores = new ConcurrentHashMap<>();
        this.gameRunning = false;
        this.standableBlocks = initializeStandableBlocks();
    }

    // Initialize and cache the list of standable blocks
    private List<Material> initializeStandableBlocks() {
        List<Material> blocks = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isSolid() && material != Material.AIR && material.isBlock()) {
                blocks.add(material);
            }
        }
        return Collections.unmodifiableList(blocks);
    }

    // Starts the game
    public synchronized void startGame() {
        if (gameRunning) return;

        gameRunning = true;
        playerScores.clear();
        Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Block Search game has started!");
        startNextRound();
    }

    // Stops the game and resets everything
    public synchronized void stopGame() {
        if (!gameRunning) return;

        gameRunning = false;
        playerScores.clear();
        cancelScheduledTask();
        Bukkit.getServer().broadcastMessage(ChatColor.RED + "The game has been stopped and reset.");
    }

    // Starts the next round with a new random standable block
    private synchronized void startNextRound() {
        if (!gameRunning) return;

        // Randomly select a standable block from the cached list
        Random random = new Random();
        currentBlockType = standableBlocks.get(random.nextInt(standableBlocks.size()));

        String formattedBlockName = formatBlockName(currentBlockType);
        Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Stand on: " + ChatColor.BOLD + formattedBlockName);

        // Schedule the round timeout
        scheduledTask = Bukkit.getScheduler().runTaskLater(plugin, this::handleRoundTimeout, ROUND_DURATION_TICKS);
    }

    // Handles the round timeout when no player has found the block in time
    private synchronized void handleRoundTimeout() {
        if (!gameRunning) return;

        Bukkit.getServer().broadcastMessage(ChatColor.RED + "Round timed out! Starting a new round.");
        startNextRound();
    }

    // Formats the block name for better readability
    private String formatBlockName(Material material) {
        String name = material.toString().replace("_", " ").toLowerCase();
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if(!word.isEmpty()){
                formatted.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return formatted.toString().trim();
    }

    // Check if a specific player is standing on the correct block
    public synchronized void checkForWinner(Player player) {
        if (!gameRunning || currentBlockType == null) return;

        Block blockUnderPlayer = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
        Material blockType = blockUnderPlayer.getType();

        if (blockType == currentBlockType) {
            UUID playerId = player.getUniqueId();
            int score = playerScores.getOrDefault(playerId, 0) + 1;
            playerScores.put(playerId, score);

            player.sendMessage(ChatColor.AQUA + "You have earned a point! " + ChatColor.GOLD + score + " points");
            Bukkit.getServer().broadcastMessage(ChatColor.GREEN + player.getName() + ChatColor.YELLOW + " has earned a point! " + ChatColor.GOLD + score + " points");

            if (score >= WINNING_SCORE) {
                Bukkit.getServer().broadcastMessage(ChatColor.GOLD + player.getName() + ChatColor.RED + " has won the game!");
                stopGame();
                return;
            }

            // Proceed to the next round
            cancelScheduledTask();
            startNextRound();
        }
    }

    // Cancels the currently scheduled task if any
    private synchronized void cancelScheduledTask() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel();
        }
    }

    // Getter method to check if the game is running
    public boolean isGameRunning() {
        return gameRunning;
    }

    // Getter method to get the current block type
    public Material getCurrentBlockType() {
        return currentBlockType;
    }
}
