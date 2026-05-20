package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({LocalPlayer.class})
public class MixinLocalPlayer {
   @Redirect(
      method = {"hasEnoughFoodToStartSprinting"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/food/FoodData;getFoodLevel()I"
)
   )
   public int hasEnoughThirstToStartSprinting(FoodData instance) {
      int Food = instance.m_38702_();
      if (!(Boolean)CommonConfig.MOVE_SLOW_WHEN_THIRSTY.get()) {
         return Food;
      } else if ((float)Food < 6.0F) {
         return Food;
      } else {
         Food = (Integer)Minecraft.m_91087_().f_91074_.getCapability(ModCapabilities.PLAYER_THIRST).lazyMap(IThirst::getThirst).orElse(Food);
         return Food;
      }
   }
}
