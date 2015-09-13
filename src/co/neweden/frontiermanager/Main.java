package co.neweden.frontiermanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	public static final Logger logger = Logger.getLogger("Minecraft");
	private static Set<World> worlds = new HashSet< World>();
	
	@Override
	public void onEnable() {
		this.loadWorlds();
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (!command.getName().equalsIgnoreCase("frontiermanager"))
			return false;
		
		if (args.length == 0) {
			if (sender.hasPermission("frontiermanager.help")) {
				sender.sendMessage(String.format("§b%s version %s", getDescription().getName(), getDescription().getVersion()));
				sender.sendMessage("§aSub-commands:");
				if (sender.hasPermission("frontiermanager.addworld"))
					sender.sendMessage("§a- addworld <worldname>§e: Adds this world to management and creates a config file");
			} else
				sender.sendMessage("§cYou do not have permission to run this command.");
		} else {
			switch (args[0]) {
			case "addworld": addWorld(sender, args);
				break;
			case "reload": reload(sender);
				break;
			default: sender.sendMessage(String.format("§cUnknown sub-command %s", args[0]));
			}
		}
		return true;
	}
	
	private void addWorld(CommandSender sender, String args[]) {
		if (!sender.hasPermission("frontiermanager.addworld")) {
			sender.sendMessage("§cYou do not have permission to run this sub-command.");
			return;
		}
		if (args.length != 2 || Bukkit.getWorld(args[1]) == null) {
			sender.sendMessage("§cYou did not specify a world or the world you specified is not loaded.");
			return;
		}
		getDataFolder().mkdir();
		File config = new File(getDataFolder(), args[1] + ".yml");
		if (config.exists()) {
			sender.sendMessage(String.format("§cCannot create config for world %s, the config already exists.", args[1]));
			return;
		}
		copyConfigFile(getResource("default_config.yml"), config);
		World world = new World(args[1]);
		worlds.add(world);
		if (worlds.contains(world)) {
			sender.sendMessage(String.format("§aConfig created and world %s now under management.", args[1]));
		} else {
			sender.sendMessage(String.format("§cCould not start managing world %s", args[1]));
		}
	}
	
	private void reload(CommandSender sender) {
		if (!sender.hasPermission("frontiermanager.reload")) {
			sender.sendMessage("§cYou do not have permission to run this sub-command.");
			return;
		}
		worlds.clear();
		loadWorlds();
		sender.sendMessage("§aReloaded settings from configs");
	}
	
	private boolean copyConfigFile(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while((len=in.read(buf))>0){
				out.write(buf,0,len);
			}
			out.close();
			in.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void onDisable() {
		logger.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
	}
	
	private void loadWorlds() {
		logger.info(String.format("[%s] Starting management for worlds", getDescription().getName()));
		getDataFolder().mkdir();
		File[] files;
		try {
			files = getDataFolder().listFiles();
		} catch (NullPointerException e) { return; }
		
		for (int i = 0; i < files.length; i++) {
			String fName = files[i].getName();
			// Skip to next file if this is not a file and isn't a YML file
			if (!files[i].isFile() || !fName.substring(fName.length() - 4, fName.length()).equals(".yml")) {
				continue;
			}
			String name = fName.substring(0, fName.length() - 4);
			// Skip to next if this file name is not a world name
			if (Bukkit.getWorld(name) == null) {
				continue;
			}
			if (worlds.add(new World(name)) == true) {
				logger.info(String.format("[%s] World %s now under management", getDescription().getName(), name));
			}
		}
		
		if (worlds.isEmpty()) {
			logger.info(String.format("[%s] No worlds to manage", getDescription().getName()));
		}
	}
	
}
