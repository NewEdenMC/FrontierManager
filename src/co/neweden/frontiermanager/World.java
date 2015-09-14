package co.neweden.frontiermanager;

import java.io.File;

import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class World {
	
	private Main plugin;
	
	public World(Main plugin, String name) {
		this.plugin = plugin;
		this.worldName = name;
		this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), worldName + ".yml"));
	}
	
	private String worldName;
	public String getWorldName() { return this.worldName; }
	
	private FileConfiguration config;
	public FileConfiguration getConfig() { return this.config; }
	
	public boolean startReset() {
		plugin.logger.info(String.format("[%s] Starting reset of world %s", plugin.getDescription().getName(), worldName));
		
		org.bukkit.World world = plugin.getServer().getWorld(worldName);
		org.bukkit.World.Environment env = world.getEnvironment();
		org.bukkit.generator.ChunkGenerator gen = world.getGenerator();
		org.bukkit.WorldType type = world.getWorldType();
		
		File wFolder = plugin.getServer().getWorld(worldName).getWorldFolder();
		plugin.getServer().unloadWorld(worldName, true);
		wFolder.delete();
		
		WorldCreator newWorld = new WorldCreator(worldName);
		Long seed = getConfig().getLong("seed", 0);
		if (seed == 0) seed = (long) (Math.random() * 1000000 + 1);
		
		newWorld.environment(env);
		newWorld.generator(gen);
		newWorld.seed(seed);
		newWorld.type(type);
		
		if (newWorld.createWorld() != null) {
			plugin.logger.info(String.format("[%s] World %s has successfully been reset", plugin.getDescription().getName(), worldName));
			return true;
		} else {
			plugin.logger.info(String.format("[%s] Reset of world %s has failed", plugin.getDescription().getName(), worldName));
			return false;
		}
	}
	
}
