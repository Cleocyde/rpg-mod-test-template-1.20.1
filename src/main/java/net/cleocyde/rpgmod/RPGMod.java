package net.cleocyde.rpgmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.cleocyde.rpgmod.item.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class RPGMod implements ModInitializer {
	public static final String MOD_ID = "rpgmod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private Map<ServerPlayerEntity, LevelingSystem> levelingSystems;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
				player.getHungerManager().setFoodLevel(20);
			}
		});

		// REGISTER ITEMS
		ModItems.registerModItems();


		//LEVELING SYSTEM
		levelingSystems = new HashMap<>();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			levelingSystems.put(player, new LevelingSystem(server));
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
				if (levelingSystem != null) {
					levelingSystem.updateActionBar(player);
				}
			}
		});

		//PLAYER HEALTH
		ServerTickEvents.START_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				adjustHealth(player);
			}
		});

	}
	private void adjustHealth(ServerPlayerEntity player) {
		int level = player.experienceLevel;
		float maxHealth = 60.0F + level * 5.0F;
		Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(maxHealth);
		player.heal(0f);

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
		if (levelingSystem != null) {
			levelingSystem.setLevel(level, player);
			levelingSystem.updateActionBar(player);
		}

		return 1; // Return a success result.
	}

	//
}