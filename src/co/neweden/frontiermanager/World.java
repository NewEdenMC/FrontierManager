package co.neweden.frontiermanager;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class World {
	
	private Main plugin;
	private Date lastReset;
	private Date nextReset;
	
	public World(Main plugin, String name) {
		this.plugin = plugin;
		this.worldName = name;
		this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), worldName + ".yml"));
		this.lastReset = getDate(getConfig().getString("schedule.lastReset"));
		this.nextReset = getFutureDate(lastReset, getConfig().getString("schedule.timeToNextReset"));
	}
	
	private Date getDate(String configTime) {
		if (configTime == null) return null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("y M d hm");
			Date date = sdf.parse(configTime);
			return date;
		} catch (ParseException e) {
			plugin.logger.warning(String.format("[%s] World %s: date format '%s' is in an unparsable format, please check your config, management for this world will be disabled.", plugin.getDescription().getName(), getWorldName(), configTime));
			Main.worlds.remove(this);
		}
		return null;
	}
	
	private Date getFutureDate(Date absoluteDate, String referenceDate) {
		if (referenceDate == null) return null;
		try {
			String[] refDate = referenceDate.split(" "); 
			if (refDate.length != 4)
				throw new ParseException(referenceDate, 0);
			if (refDate[3].length() != 4)
				throw new ParseException(refDate[3], 0);
			
			String refHours = refDate[3].substring(0, 2);
			String refMinutes = refDate[3].substring(2, 4);
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(absoluteDate);
			cal.add(Calendar.YEAR, Integer.parseInt(refDate[0]));
			cal.add(Calendar.MONTH, Integer.parseInt(refDate[1]));
			cal.add(Calendar.DAY_OF_MONTH, Integer.parseInt(refDate[2]));
			cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(refHours));
			cal.add(Calendar.MINUTE, Integer.parseInt(refMinutes));
			return cal.getTime();
		} catch (NumberFormatException|ParseException e) {
			plugin.logger.warning(String.format("[%s] World %s: date format '%s' is in an unparsable format, please check your config, management for this world will be disabled.", plugin.getDescription().getName(), getWorldName(), referenceDate));
			Main.worlds.remove(this);
		}
		return null;
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
