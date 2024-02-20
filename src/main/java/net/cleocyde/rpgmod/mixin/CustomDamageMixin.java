package net.cleocyde.rpgmod.mixin;

import net.cleocyde.rpgmod.CustomHealthSystem;
import net.cleocyde.rpgmod.RPGMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(PlayerEntity.class)
public abstract class CustomDamageMixin {
    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo ci) {
        // Get the player entity
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Get the CustomHealthSystem instance for the player
        CustomHealthSystem healthSystem = RPGMod.getHealthSystems().get(player);
        if (healthSystem != null) {
            if(source == player.getDamageSources().outOfWorld()) {
            return;
            }

            // Apply the damage to the custom health system
            healthSystem.takeDamage(player, source, amount);

            // Cancel the original damage
            ci.cancel();

            // Apply null amount of damage for knockback
            player.damage(player.getDamageSources().generic(), 0f);
        }
    }

}