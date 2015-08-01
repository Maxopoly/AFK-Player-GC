package com.github.Kraken3.AFKPGC;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import net.minecraft.server.v1_8_R3.MinecraftServer;

/**
 * Reads TPS direct from MC instance. Note as a result this plugin works with 1.8.7 or above only, at the moment.
 * If needful, we can add NMS style server respect.
 * 
 * @author ProgrammerDan
 */
public class TpsReader {

	public static float getTPS() {
		float latestTPS = (float) ((MinecraftServer) ((CraftServer)Bukkit.getServer()).getServer()).recentTps[0];
		return (latestTPS > 20.0f) ? 20.0f : latestTPS;
	}
}
