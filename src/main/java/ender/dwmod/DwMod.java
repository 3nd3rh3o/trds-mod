package ender.dwmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ender.dwmod.portals.PortalManager;

public class DwMod implements ModInitializer {
	public static final String MOD_ID = "dwmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);



	// TODO - MOVE ME INSIDE A PERSISTENT STATE / WORLD CAPABILITY !!!
	// UGLY

	private static PortalManager portalManager;


	
	public static PortalManager getPortalManager(MinecraftServer server)
	{
		if (portalManager == null)
			portalManager = new PortalManager(server);
		return portalManager;
	}
	// ENDUGLY

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
	}
}