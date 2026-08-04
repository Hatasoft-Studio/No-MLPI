package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NoMlpi implements ModInitializer {
	public static final String MOD_ID = "no-mlpi";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final Map<String, Long> loginRecord = new HashMap<>();
	// 判定间隔：3000毫秒 = 3秒
	private static final long LOGIN_INTERVAL_THRESHOLD = 3000;

	@Override
	public void onInitialize() {
		LOGGER.info("No-MLPI 机器人防护模组已加载");

		// 完整3参数签名，适配1.18.2
		ServerPlayConnectionEvents.JOIN.register((ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) -> {
			String playerName = handler.player.getGameProfile().getName();
			long now = System.currentTimeMillis();

			if (loginRecord.containsKey(playerName)) {
				long lastLogin = loginRecord.get(playerName);
				long interval = now - lastLogin;
				if (interval < LOGIN_INTERVAL_THRESHOLD) {
					LOGGER.warn("疑似机器人频繁重连：{}，间隔 {}ms", playerName, interval);
					handler.player.networkHandler.disconnect(
							Text.of("检测到异常频繁登录，疑似机器人连接，已自动踢出")
					);
				}
			}
			loginRecord.put(playerName, now);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> loginRecord.clear());
	}
}