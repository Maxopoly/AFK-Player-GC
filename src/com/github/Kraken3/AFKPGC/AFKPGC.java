package com.github.Kraken3.AFKPGC;

import java.io.File;
import java.util.logging.Logger;
import java.util.*;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Kraken3.AFKPGC.commands.CommandHandler;

public class AFKPGC extends JavaPlugin {
	public static CommandHandler commandHandler;
	public static Logger logger;
	public static JavaPlugin plugin;
	public static boolean enabled = true;
	public static Set<UUID> immuneAccounts;
	public static boolean debug = false;

	@Override
	public void onEnable() {
		// setting a couple of static fields so that they are available
		// elsewhere
		logger = getLogger();
		plugin = this;
		commandHandler = new CommandHandler(this);
		BotDetector.banfile=new File(plugin.getDataFolder().getAbsolutePath()+"/banlist.txt");
		BotDetector.parseBanlist();
		// Reads Config.yml - false as an answer indicated unrecoverable error
		AFKPGC.enabled = ConfigurationReader.readConfig();
		if (!AFKPGC.enabled) {
			logger.warning("Plugin is not running");
		}

		getServer().getPluginManager()
				.registerEvents(new EventHandlers(), this);

		// Checks whether to 'garbage collect' AFKers every 20 ticks (1
		// seconds);
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Kicker(), 0, 20L); // TODO make this configurable

		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Runnable() {
					public void run() {
						LastActivity.currentTime = System.currentTimeMillis();
					}
				}, 0, 1L);

		getServer().getScheduler().scheduleSyncDelayedTask(this,
				new BotDetector(), BotDetector.frequency);

		// Because bukkit..
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Runnable() {
					public void run() {
						LastActivity.FixInconsistencies();
					}
				}, 0, 6000L); // TODO make this configurable
		
		logger.info("AFK Player Garbage Collector has been enabled");
	}

	@Override
	public void onDisable() {
		BotDetector.freeEveryone(); // players wont get freed if the server
									// crashed, should the bans of this plugin
									// also be stored in an external file?
		logger.info( "AFK Player Garbage Collector has been disabled");
	}

	public static void removerPlayer(UUID uuid) {
		if (LastActivity.lastActivities.containsKey(uuid)) {
			LastActivity.lastActivities.remove(uuid);
		}
	}

	public static LastActivity addPlayer(Player p) {
		if (p == null) {
			return null;
		}
		if (AFKPGC.immuneAccounts != null && AFKPGC.immuneAccounts.contains(p.getUniqueId())) {
			return null;
		}
		UUID uuid = p.getUniqueId();

		LastActivity la;
		if (LastActivity.lastActivities.containsKey(uuid)) {
			la = LastActivity.lastActivities.get(uuid);
		} else {
			la = new LastActivity();
			LastActivity.lastActivities.put(uuid, la);
			la.playerName = uuid;
			la.timeOfLastKickerPass = LastActivity.currentTime;
		}
		la.timeOfLastActivity = LastActivity.currentTime;
		return la;
	}

	public static void debug(Object...msg) {
		if (AFKPGC.debug) {
			StringBuffer sb = new StringBuffer();
			for (Object obj : msg) {
				sb.append(obj.toString());
			}
			logger.info("DEBUG: " + sb.toString());
		}
	}
}
