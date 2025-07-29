package com.miniverse.modularinfrastructure.api.tool;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface for electric equipment
 * Based on Immersive Engineering's IElectricEquipment
 */
public interface IElectricEquipment {
    
    /**
     * Called whenever an electric source attempts to damage the player, or is close to doing so
     *
     * @param equipped The current ItemStack
     * @param eqSlot   The equipment slot the Item is in
     * @param owner    The entity wearing/holding the item
     * @param cache    A way for different IElectricEquipment items to communicate with each other
     * @param dmg      The damage source that would be used. Set the amount to 0 to prevent any damage from being done
     * @param desc     A description of the specific type of electric source
     */
    void onStrike(ItemStack equipped, EquipmentSlot eqSlot, LivingEntity owner, Map<String, Object> cache, 
                  @Nullable DamageSource dmg, ElectricSource desc);
    
    static void applyToEntity(LivingEntity entity, @Nullable DamageSource dmg, ElectricSource source) {
        Map<String, Object> cache = new HashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack s = entity.getItemBySlot(slot);
            if (!s.isEmpty() && s.getItem() instanceof IElectricEquipment) {
                ((IElectricEquipment) s.getItem()).onStrike(s, slot, entity, cache, dmg, source);
            }
        }
    }
    
    /**
     * Source of electric damage
     * This isn't just a float so it can be overridden for special sources
     */
    class ElectricSource {
        /**
         * How strong the source is. Negative numbers should indicate that no damage will be done.
         * .25 is a low power TC, .5, 1, 1.5 are LV, MV and HV wires respectively, 2 is a high power TC
         * Anything >=1.75 will destroy Faraday suits
         */
        public final float level;
        
        public ElectricSource(float level) {
            this.level = level;
        }
    }
}