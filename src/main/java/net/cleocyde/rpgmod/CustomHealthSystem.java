package net.cleocyde.rpgmod;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;


public class CustomHealthSystem {
    private float maxHealth;
    private float currentHealth;
    private boolean isDying; // Add this line

    public CustomHealthSystem() {
        this.maxHealth = 65.0f; // Players start with 65 health at level 1
        this.currentHealth = this.maxHealth;
        this.isDying = false; // Initialize the flag to false
    }

    public void levelUp(PlayerEntity player, int level) {
        this.maxHealth = 65.0f + level * 5.0f; // Increase max health by 5 each time player levels up
        this.currentHealth = this.maxHealth; // Restore current health to max health
        updateActionBar(player);
    }

    public void takeDamage(PlayerEntity player, DamageSource source, float amount) {
        if (player.isDead() || this.isDying) { // Check the flag here
            return;
        }
        this.currentHealth -= amount;
        if (this.currentHealth <= 0) {
            // If health falls at or under 0, kill the player.
            if(player.isAlive()) {
                this.isDying = true; // Set the flag to true before killing the player
                player.setHealth(0f);
                this.isDying = false; // Reset the flag after the player is killed
            }
        } else if (this.currentHealth < this.maxHealth * 0.05f) {
            // If health is between 1 and 5%, show half a heart.
            player.setHealth(0.5f);
        } else {
            // Otherwise, set the player's health to represent the percentage of current health to max health.
            player.setHealth(20.0f * (this.currentHealth / this.maxHealth));
        }
    }

    public void heal(float amount) {
        this.currentHealth += amount;
        if (this.currentHealth > this.maxHealth) {
            this.currentHealth = this.maxHealth;
        }
    }

    public void setHealth(PlayerEntity player, float health) {
        this.currentHealth = health;
        if (this.currentHealth > this.maxHealth) {
            this.currentHealth = this.maxHealth;
        } else if (this.currentHealth <= 0) {
            player.kill();
        } else if (this.currentHealth < this.maxHealth * 0.05f) {
            player.setHealth(0.5f);
        } else {
            player.setHealth(20.0f * (this.currentHealth / this.maxHealth));
        }
        updateActionBar(player);
    }

    public void updateActionBar(PlayerEntity player) {
        Text message = Text.literal("HP: " + (int)this.currentHealth + "/" + (int)this.maxHealth);
        player.sendMessage(message, true); // The second parameter indicates that this is an action bar message.
    }

    public float getMaxHealth() {
        return this.maxHealth;
    }
}
