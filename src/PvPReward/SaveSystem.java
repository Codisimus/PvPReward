
package PvPReward;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;

/**
 *
 * @author Cody
 */
class SaveSystem {
    private static LinkedList<Record> records = new LinkedList<Record>();
    private static boolean save = true;

    /**
     * Reads save file to load PvPReward data
     * Saving is turned off if an error occurs
     */
    protected static void loadFromFile() {
        BufferedReader bReader = null;
        try {
            new File("plugins/PvPReward").mkdir();
            new File("plugins/PvPReward/pvpreward.save").createNewFile();
            bReader = new BufferedReader(new FileReader("plugins/PvPReward/pvpreward.save"));
            String line = "";
            while ((line = bReader.readLine()) != null) {
                String[] split = line.split(";");
                String player = split[0];
                int kills = Integer.parseInt(split[1]);
                int deaths = Integer.parseInt(split[2]);
                int karma = Integer.parseInt(split[3]);
                Record record = new Record(player, kills, deaths, karma);
                records.add(record);
            }
        }
        catch (Exception e) {
            save = false;
            System.out.println("[PvPReward] Load failed, saving turned off to prevent loss of data");
            e.printStackTrace();
        }
    }

    /**
     * Writes data to save file
     * Old file is overwritten
     */
    protected static void save() {
        //cancels if saving is turned off
        if (!save)
            return;
        BufferedWriter bWriter = null;
        try {
            bWriter = new BufferedWriter(new FileWriter("plugins/PvPReward/pvpreward.save"));
            for(Record record : records) {
                bWriter.write(record.player.concat(";"));
                bWriter.write(record.kills+";");
                bWriter.write(record.deaths+";");
                bWriter.write(record.karma+";");
                bWriter.newLine();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                bWriter.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the LinkedList of saved Records
     * 
     * @return the LinkedList of saved Records
     */
    public static LinkedList<Record> getRecords() {
        return records;
    }

    /**
     * Returns the Record for the given Player
     * A new Record is created if none exists
     * 
     * @param player The name of the Player
     * @return The Record of the Player
     */
    public static Record findRecord(String player) {
        for(Record record : records) {
            if (record.player.equals(player))
                return record;
        }
        Record newRecord = new Record(player, 0, 0, 0);
        addRecord(newRecord);
        return newRecord;
    }
    
    /**
     * Adds the Record to the LinkedList of saved Records
     * 
     * @param record The Record to be added
     */
    protected static void addRecord(Record record) {
        try {
            records.add(record);
            save();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
