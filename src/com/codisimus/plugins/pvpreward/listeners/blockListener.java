package com.codisimus.plugins.pvpreward.listeners;

import com.codisimus.plugins.pvpreward.Record;
import com.codisimus.plugins.pvpreward.SaveSystem;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

/**
 * Listens for Players breaking Tombstone Signs
 * 
 * @author Codisimus
 */
public class blockListener extends BlockListener {

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        //Return if the Block is not a Sign
        if (block.getTypeId() != 323)
            return;

        Sign sign = (Sign)block;

        for (Record record: SaveSystem.records)
            if (record.tombstone.equals(sign)) {
                event.setCancelled(true);
                break;
            }
    }
}