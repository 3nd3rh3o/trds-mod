package ender.dwmod;

import ender.dwmod.portals.PortalsClientWorldManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public class DwModClient implements ClientModInitializer {
	public static PortalsClientWorldManager WORLD_MANAGER;


	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		Minecraft client = Minecraft.getInstance();
		WORLD_MANAGER = new PortalsClientWorldManager(client);

		ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> 
		{
			WORLD_MANAGER.setMainWorld(mc.level);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> 
		{
			WORLD_MANAGER.setMainWorld(null);
		});

		ClientTickEvents.END_CLIENT_TICK.register(mc -> 
		{
			if (WORLD_MANAGER != null)
				WORLD_MANAGER.tickExtraWorlds();
		});

	}
}