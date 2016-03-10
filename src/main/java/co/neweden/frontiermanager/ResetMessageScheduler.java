package co.neweden.frontiermanager;

import java.util.Date;
import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.scheduler.BukkitRunnable;

public class ResetMessageScheduler {
	
	protected static Main plugin;
	private static ArrayList<ResetMessageObject> resetMessages;
	
	protected static void init(final Main plugin) {
		ResetMessageScheduler.plugin = plugin;
		resetMessages = new ArrayList<ResetMessageObject>();
		new BukkitRunnable() {
			@Override
			public void run() {
				long timeStamp = (new Date()).getTime() / 1000;
				ArrayList<String> messages = new ArrayList<String>();
				for (Iterator<ResetMessageObject> i = resetMessages.iterator(); i.hasNext();) {
					ResetMessageObject rmo = i.next();
					if (rmo.timeStamp == timeStamp) {
						messages.add(String.format("%s will reset in %s", rmo.world.getWorldName(), rmo.humanReadable));
						i.remove();
					}
				}
				String message = "";
				if (messages.size() == 0) return;
				if (messages.size() == 1)
					message = messages.get(0);
				if (messages.size() == 2)
					message = messages.get(0) + ", " + messages.get(1);
				if (messages.size() > 2) {
					for (String m : messages)
						message += m + "\n";
				}
				plugin.getServer().broadcastMessage(String.format("§dAttention!\n%s", message));
			}
		}.runTaskTimer(plugin, 0L, 20L);
	}
	
	public static void scheduleMessage(ResetMessageObject rmo) {
		resetMessages.add(rmo);
	}
	
	public static void removeMessagesForWorld(World world) {
		for (Iterator<ResetMessageObject> i = resetMessages.iterator(); i.hasNext();) {
			ResetMessageObject rmo = i.next();
			if (rmo.world == world)
				i.remove();
		}
	}
	
}
