package net.cleocyde.rpgmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.cleocyde.rpgmod.item.ModItems;
import java.util.HashMap;
import java.util.Map;


public class RPGMod implements ModInitializer {
	public static final String MOD_ID = "rpgmod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private Map<ServerPlayerEntity, LevelingSystem> levelingSystems;
	private static Map<ServerPlayerEntity, CustomHealthSystem> healthSystems;

	public static Map<ServerPlayerEntity, CustomHealthSystem> getHealthSystems() {
		return healthSystems;
	}
	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		// REGISTER ITEMS
		ModItems.registerModItems();


		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			newPlayer.experienceLevel = oldPlayer.experienceLevel;
			newPlayer.experienceProgress = oldPlayer.experienceProgress;
			newPlayer.totalExperience = oldPlayer.totalExperience;
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			CustomHealthSystem healthSystem = getHealthSystems().get(newPlayer);
			if (healthSystem != null) {
				healthSystem.heal(healthSystem.getMaxHealth());
			}
		});


		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			dispatcher.register(CommandManager.literal("setHealth")
					.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("health", IntegerArgumentType.integer())
									.executes(this::executeSetHealth))));
		});

		//LEVELING SYSTEM
		levelingSystems = new HashMap<>();
		healthSystems = new HashMap<>();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			CustomHealthSystem healthSystem = new CustomHealthSystem();
			healthSystems.put(player, healthSystem);
			levelingSystems.put(player, new LevelingSystem(server, healthSystem));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			dispatcher.register(CommandManager.literal("giveXp")
					.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("xp", IntegerArgumentType.integer())
									.executes(this::executeGiveXp))));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			dispatcher.register(CommandManager.literal("setLevel")
					.then(CommandManager.argument("player", EntityArgumentType.player())
							.then(CommandManager.argument("level", IntegerArgumentType.integer())
									.executes(this::executeSetLevel))));
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				LevelingSystem levelingSystem = levelingSystems.get(player);
				CustomHealthSystem healthSystem = healthSystems.get(player);
				if (levelingSystem != null && healthSystem != null) {
					//levelingSystem.updateActionBar(player);
					healthSystem.updateActionBar(player);
				}
			}
		});
	}

	private int executeGiveXp(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		int xp = IntegerArgumentType.getInteger(context, "xp");

		// Use your LevelingSystem class to give the player XP.
		LevelingSystem levelingSystem = levelingSystems.get(player);
		if (levelingSystem != null) {
			levelingSystem.addExperience(xp, player);
		}

		return 1; // Return a success result.
	}

	private int executeSetLevel(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		int level = IntegerArgumentType.getInteger(context, "level");

		// Use your LevelingSystem class to set the player's level.
		LevelingSystem levelingSystem = levelingSystems.get(player);
		CustomHealthSystem healthSystem = healthSystems.get(player);
		if (levelingSystem != null && healthSystem != null) {
			levelingSystem.setLevel(level, player);
			healthSystem.levelUp(player, level);
			//levelingSystem.updateActionBar(player);
			healthSystem.updateActionBar(player);
		}

		return 1; // Return a success result.
	}



	private int executeSetHealth(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		int health = IntegerArgumentType.getInteger(context, "health");

		// Use your CustomHealthSystem class to set the player's health.
		CustomHealthSystem healthSystem = healthSystems.get(player);
		if (healthSystem != null) {
			healthSystem.setHealth(player, health);
			healthSystem.updateActionBar(player);
		}

		return 1; // Return a success result.
	}

}
