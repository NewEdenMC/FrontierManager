package co.neweden.frontiermanager;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class WorldEdit {

    Main plugin;
    World fWorld;
    Plugin wePlugin;

    protected WorldEdit(Main plugin, World fWorld) {
        this.plugin = plugin;
        this.fWorld = fWorld;
        wePlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");
    }

    public void pasteSchematic() {
		String schematicName = fWorld.getConfig().getString("spawnSchematic.fileName", null);
		Boolean noAir = fWorld.getConfig().getBoolean("spawnSchematic.noAir", true);
		if (schematicName == null) return;

		if (wePlugin == null) {
			plugin.getLogger().info("WorldEdit is not install, so not schematic will be pasted for world " + fWorld.getWorldName());
			return;
		}

		try {
			File dir = new File(wePlugin.getDataFolder() + File.separator + "schematics" + File.separator, schematicName);
			EditSession editSession = new EditSession(new BukkitWorld(fWorld.spawnLocation.getWorld()), 999999999);
			editSession.enableQueue();
			SchematicFormat schematic = SchematicFormat.getFormat(dir);
			CuboidClipboard clipboard = schematic.load(dir);
			clipboard.paste(editSession, BukkitUtil.toVector(fWorld.spawnLocation), noAir);
			editSession.flushQueue();
            plugin.getLogger().info("Spawn Schematic for world " + fWorld.getWorldName() + " pasted into world.");
		} catch (MaxChangedBlocksException | DataException | IOException ex) {
			plugin.getLogger().log(Level.SEVERE, "An error has occurred when attempting to paste a schematic.", ex);
		}
    }

}
