package com.codisimus.plugins.pvpreward;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;

/**
 * Holds PvPReward data and is used to load/save data
 *
 * @author Codisimus
 */
public class SaveSystem {
    public static LinkedList<Record> records = new LinkedList<Record>();
    public static boolean save = true;

    /**
     * Reads save file to load PvPReward data
     * Saving is turned off if an error occurs
     */
    public static void load() {
        try {
            new File("plugins/PvPReward/pvpreward.save").createNewFile();
            BufferedReader bReader = new BufferedReader(new FileReader("plugins/PvPReward/pvpreward.save"));
            String line = bReader.readLine();
            while (line != null) {
                String[] split = line.split(";");
                
                String player = split[0];
                int kills = Integer.parseInt(split[1]);
                int deaths = Integer.parseInt(split[2]);
                int karma = Integer.parseInt(split[3]);
                
                records.add(new Record(player, kills, deaths, karma));
                line = bReader.readLine();
            }
            
            bReader.close();
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
    public static void save() {
        //Cancel if saving is turned off
        if (!save)
            return;

        try {
            BufferedWriter bWriter = new BufferedWriter(new FileWriter("plugins/PvPReward/pvpreward.save"));
            for(Record record: records) {
                //Write data in the format "name;kills;deaths;karma"
                bWriter.write(record.name.concat(";"));
                bWriter.write(record.kills+";");
                bWriter.write(record.deaths+";");
                bWriter.write(record.karma);
                
                //Write each Record on a new line
                bWriter.newLine();
            }

            bWriter.close();
        }
        catch (Exception saveFailed) {
            System.err.println("[PvPReward] Save Failed!");
            saveFailed.printStackTrace();
        }
    }

    /**
     * Returns the Record for the given Player
     * A new Record is created if one is not found
     * 
     * @param player The name of the Player
     * @return The Record of the Player
     */
    public static Record findRecord(String player) {
        for(Record record: records)
            if (record.name.equals(player))
                return record;

        //Create a new Record
        Record newRecord = new Record(player);
        records.add(newRecord);
        return newRecord;
    }
}
