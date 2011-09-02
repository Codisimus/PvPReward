
package PvPReward;

import java.util.LinkedList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class PvPRewardBlockListener extends BlockListener {

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType().equals(Material.SIGN)) {
            Sign sign = (Sign)block;
            LinkedList<Record> records = SaveSystem.getRecords();
            for(Record record : records) {
                if (record.tombstone.equals(sign))
                    event.setCancelled(true);
            }
        }
    }
}