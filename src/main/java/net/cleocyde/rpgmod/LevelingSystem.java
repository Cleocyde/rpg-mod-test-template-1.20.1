package net.cleocyde.rpgmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class LevelingSystem {
    private int experience;
    private int level;
    private int experienceNeededToLevelUp;
    private int experienceNeededToLevelUpFromPreviousLevel;
    private ScoreboardObjective levelObjective;
    private ScoreboardObjective totalXpObjective;
    private ScoreboardObjective xpToNextLevelObjective;

    public LevelingSystem(MinecraftServer server) {
        this.experience = 0;
        this.level = 1;
        this.experienceNeededToLevelUp = 110;
        this.experienceNeededToLevelUpFromPreviousLevel = 0;


        // Create new objectives on the scoreboard for the player's level, total XP, and XP to next level.
        Scoreboard scoreboard = server.getScoreboard();
        this.levelObjective = getOrCreateObjective(scoreboard, "playerLevel", "Level");
        this.totalXpObjective = getOrCreateObjective(scoreboard, "totalXp", "Total XP");
        this.xpToNextLevelObjective = getOrCreateObjective(scoreboard, "xpToNextLevel", "XP to Next Level");

        // Set the display slots of the objectives to the sidebar.
        scoreboard.setObjectiveSlot(1, this.levelObjective);
        scoreboard.setObjectiveSlot(1, this.totalXpObjective);
        scoreboard.setObjectiveSlot(1, this.xpToNextLevelObjective);
    }

    private ScoreboardObjective getOrCreateObjective(Scoreboard scoreboard, String name, String displayName) {
        ScoreboardObjective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective = scoreboard.addObjective(name, ScoreboardCriterion.DUMMY, Text.literal(displayName), ScoreboardCriterion.RenderType.INTEGER);
        }
        return objective;
    }

    public void addExperience(int amount, PlayerEntity player) {
        // Check if the player's level is already at 200.
        if (this.level >= 200) {
            return; // If it is, don't add more experience and return immediately.
        }

        this.experience += amount;

        while (this.experience >= getExperienceNeededToLevelUp()) {
            this.experience -= getExperienceNeededToLevelUp();
            int nextLevel = this.level + 1;
            this.experienceNeededToLevelUp = calculateExperienceNeededToLevelUp(nextLevel); // Calculate the experience needed for the next level
            this.level++;

            // If the player's level has reached 200 after leveling up.
            if (this.level >= 200) {
                this.level = 200; // Ensure the level does not exceed 200.
                this.experience = 0; // Reset the experience to 0 as no more experience can be gained.
                break; // Break the loop as no more leveling up is needed.
            }
        }

        // Update the player's scores on the scoreboard.
        updateScore(player, this.levelObjective, this.level);
        updateScore(player, this.totalXpObjective, this.experience);
        updateScore(player, this.xpToNextLevelObjective, getExperienceNeededToLevelUp() - this.experience);
    }


    private void updateScore(PlayerEntity player, ScoreboardObjective objective, int score) {
        ScoreboardPlayerScore scoreboardScore = player.getScoreboard().getPlayerScore(player.getName().getString(), objective);
        scoreboardScore.setScore(score);
    }

    private int calculateExperienceNeededToLevelUp(int level) {
        int newExperienceNeededToLevelUp;

        if (level < 180) {
            newExperienceNeededToLevelUp = this.experienceNeededToLevelUp + (level * 8 + (level * level) * 10);
        } else if (level >= 180 && level < 199) {
            newExperienceNeededToLevelUp = this.experienceNeededToLevelUp + (level * 15 + (level * level) * 20);
        } else if (level == 199) {
            newExperienceNeededToLevelUp = this.experienceNeededToLevelUp + (level * 75 + (level * level) * 50);
        } else {
            newExperienceNeededToLevelUp = this.experienceNeededToLevelUp; // For level 200 and above, keep the XP requirement the same.
        }

        return newExperienceNeededToLevelUp;
    }



    private int getExperienceNeededToLevelUp() {
        // This is just an example. You might want a more complex formula for leveling up.

        return this.experienceNeededToLevelUp;
    }
    public void setLevel(int level, PlayerEntity player) {
        while (this.level < level && this.level < 200) {
            addExperience(getExperienceNeededToLevelUp() - this.experience + 1, player);
        }

        // Update the player's scores on the scoreboard.
        updateScore(player, this.levelObjective, this.level);
        updateScore(player, this.totalXpObjective, this.experience);
        updateScore(player, this.xpToNextLevelObjective, getExperienceNeededToLevelUp() - this.experience);
    }


    public void updateActionBar(PlayerEntity player) {
        Text message = Text.literal("Level: " + this.level);
        player.sendMessage(message, true); // The second parameter indicates that this is an action bar message.
    }

    // Add more methods for handling other events that should give experience.
}