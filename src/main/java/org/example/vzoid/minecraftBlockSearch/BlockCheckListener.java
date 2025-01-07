package org.example.vzoid.minecraftBlockSearch;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class BlockCheckListener implements Listener {

    private final BlockSearchGame game;

    public BlockCheckListener(BlockSearchGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!game.isGameRunning()) return;

        Player player = event.getPlayer();

        // Check if the player has moved to a new block to prevent redundant checks
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Material blockType = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();

        if (blockType == game.getCurrentBlockType()) {
            game.checkForWinner(player);
        }
    }
}
