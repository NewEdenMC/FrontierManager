package co.neweden.frontiermanager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	public static final Logger logger = Logger.getLogger("Minecraft");
	private static Set<World> worlds = new HashSet< World>();
	
	@Override
	public void onEnable() {
		// Create config directory
		getDataFolder().mkdir();
		File firstConfig = new File(getDataFolder(), "frontier.yml");
		if (firstConfig.exists() == false) {
			copyConfigFile(getResource("frontier.yml"), firstConfig);
		}
		
		this.loadWorlds();
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
