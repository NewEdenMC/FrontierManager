package co.neweden.frontiermanager;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.bukkit.Material;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class World implements Listener {
	
	private Main plugin;
	private Calendar lastReset;
	private Calendar nextReset;
	protected BukkitTask scheduler;
	
	public World(Main plugin, String name) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.worldName = name;
		this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), worldName + ".yml"));
		updateTimes();
		if (lastReset == null || nextReset == null) return;
		
		scheduler = new BukkitRunnable() {
			@Override
			public void run() {
				Calendar now = Calendar.getInstance();
				now.clear(Calendar.SECOND);
				now.clear(Calendar.MILLISECOND);
				nextReset.clear(Calendar.SECOND);
				nextReset.clear(Calendar.MILLISECOND);
				if (now.compareTo(nextReset) == 0) {
					autoReset();
				}
			}
		}.runTaskTimer(plugin, 0L, 1200L);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event) {
		if (getConfig().getBoolean("warnStorage", true) == false ||
			!event.getPlayer().getWorld().getName().equals(worldName))
		{
			return;
		}
		if (event.getBlock().getType() == Material.CHEST ||
			event.getBlock().getType() == Material.TRAPPED_CHEST ||
			event.getBlock().getType() == Material.FURNACE ||
			event.getBlock().getType() == Material.DISPENSER ||
			event.getBlock().getType() == Material.HOPPER ||
			event.getBlock().getType() == Material.DROPPER ||
			event.getBlock().getType() == Material.ENDER_CHEST)
		{
			event.getPlayer().sendMessage(
					"�dWARNING!\n" +
					"�dIt is not recommended to store items in this frontier world.\n" +
					"�dThis world will be automatically reset on " + nextReset.getTime()
			);
		}
	}
	
	private void updateTimes() {
		lastReset = getTime(getConfig().getString("schedule.lastReset"));
		nextReset = getFutureTime(lastReset, getConfig().getString("schedule.timeToNextReset"));
	}
	
	private Calendar getTime(String configTime) {
		if (configTime == null) return null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("y M d h:m");
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(configTime));
			return cal;
		} catch (ParseException e) {
			plugin.logger.warning(String.format("[%s] World %s: date format '%s' is in an unparsable format, please check your config, management for this world will be disabled.", plugin.getDescription().getName(), getWorldName(), configTime));
			Main.worlds.remove(this);
		}
		return null;
	}
	
	private Calendar getFutureTime(Calendar startAt, String timeToAdd) {
		if (timeToAdd == null) return null;
		try {
			String[] add = timeToAdd.split(" "); 
			if (add.length != 4)
				throw new ParseException(timeToAdd, 0);
			if (add[3].length() != 5)
				throw new ParseException(add[3], 0);
			
			String addHours = add[3].substring(0, 2);
			String addMinutes = add[3].substring(3, 5);
			
			startAt.add(Calendar.YEAR, Integer.parseInt(add[0]));
			startAt.add(Calendar.MONTH, Integer.parseInt(add[1]));
			startAt.add(Calendar.DAY_OF_MONTH, Integer.parseInt(add[2]));
			startAt.add(Calendar.HOUR_OF_DAY, Integer.parseInt(addHours));
			startAt.add(Calendar.MINUTE, Integer.parseInt(addMinutes));
			return startAt;
		} catch (NumberFormatException|ParseException e) {
			plugin.logger.warning(String.format("[%s] World %s: date format '%s' is in an unparsable format, please check your config, management for this world will be disabled.", plugin.getDescription().getName(), getWorldName(), timeToAdd));
			Main.worlds.remove(this);
		}
		return null;
	}
	
	private String worldName;
	public String getWorldName() { return this.worldName; }
	
	private FileConfiguration config;
	public FileConfiguration getConfig() { return this.config; }
	
	public void saveConfig() {
		try {
			getConfig().save(plugin.getDataFolder().getPath() + File.separator + worldName + ".yml");
		} catch (IOException e) {
			plugin.logger.warning(String.format("[%s] Unable to save data to config file %s, see the error below", plugin.getDescription().getName(), worldName + ".yml"));
			e.printStackTrace();
		}
	}
	
	private void autoReset() {
		getConfig().set("schedule.lastReset", nextReset.get(Calendar.YEAR) + " " + (nextReset.get(Calendar.MONTH) + 1) + " " + nextReset.get(Calendar.DAY_OF_MONTH) + " " + nextReset.get(Calendar.HOUR) + ":" + nextReset.get(Calendar.MINUTE));
		saveConfig();
		updateTimes();
		
		startReset();
	}
	
	public boolean startReset() {
		plugin.logger.info(String.format("[%s] Starting reset of world %s", plugin.getDescription().getName(), worldName));
		
		org.bukkit.World world = plugin.getServer().getWorld(worldName);
		org.bukkit.World.Environment env = world.getEnvironment();
		org.bukkit.generator.ChunkGenerator gen = world.getGenerator();
		org.bukkit.WorldType type = world.getWorldType();
		
		File wFolder = plugin.getServer().getWorld(worldName).getWorldFolder();
		plugin.getServer().unloadWorld(worldName, true);
		try {
			FileUtils.deleteDirectory(wFolder);
		} catch (IOException e) {
			plugin.logger.info(String.format("[%s] Could not delete world foldr for %s, this is likely due to file permissions not being set correctly on the world folder, see stack trace below. ", plugin.getDescription().getName(), worldName));
			e.printStackTrace();
			return false;
		}
		
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
