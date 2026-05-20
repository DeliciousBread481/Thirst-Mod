package dev.ghen.thirst.content.thirst;

import de.teamlapen.vampirism.util.Helper;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.common.damagesource.ModDamageSource;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.foundation.network.message.PlayerThirstSyncMessage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class PlayerThirst implements IThirst {
   public static boolean checkTombstoneEffects = false;
   public static boolean checkFDEffects = false;
   public static boolean checkLetsDoBakeryEffects = false;
   public static boolean checkLetsDoBreweryEffects = false;
   public static boolean checkVampirismEffects = false;
   int thirst = 20;
   int quenched = 5;
   float exhaustion = 0.0F;
   int damageTimer = 0;
   int syncTimer = 0;
   float prevTickExhaustion = 0.0F;
   boolean justHealed = false;
   boolean shouldTickThirst = true;
   boolean exhaustionRecalculate = false;
   boolean init = true;

   public static void drink(ItemStack item, Player player) {
      if (ThirstHelper.itemRestoresThirst(item)) {
         player.getCapability(ModCapabilities.PLAYER_THIRST, (Direction)null).ifPresent((cap) -> {
            if (WaterPurity.givePurityEffects(player, item)) {
               cap.drink(player, ThirstHelper.getThirst(item), ThirstHelper.getQuenched(item));
            }

         });
      }

   }

   public int getThirst() {
      return this.thirst;
   }

   public void setThirst(int value) {
      this.thirst = value;
   }

   public int getQuenched() {
      return this.quenched;
   }

   public void setQuenched(int value) {
      this.quenched = value;
   }

   public float getExhaustion() {
      return this.exhaustion;
   }

   public void setExhaustion(float value) {
      this.exhaustion = value;
   }

   public void setShouldTickThirst(boolean value) {
      this.shouldTickThirst = value;
   }

   public boolean getShouldTickThirst() {
      return this.shouldTickThirst;
   }

   public void drink(Player player, int thirst, int quenched) {
      int extra_quenched = Math.max(this.thirst + thirst - 20, 0);
      if (!(Boolean)CommonConfig.EXTRA_HYDRATION_CONVERT_TO_QUENCHED.get()) {
         extra_quenched = 0;
      }

      this.thirst = Math.min(this.thirst + thirst, 20);
      this.quenched = Math.min(this.quenched + quenched + extra_quenched, this.thirst);
   }

   public void tick(Player player) {
      Difficulty difficulty = player.m_9236_().m_46791_();
      if (!player.m_150110_().f_35934_) {
         if (!this.shouldTickThirst) {
            if (this.init) {
               this.init = false;
               this.updateThirstData(player);
            }

         } else if (!checkTombstoneEffects || !player.m_21220_().stream().anyMatch((e) -> e.m_19576_().contains("ghostly_shape"))) {
            if (!checkVampirismEffects || !Helper.isVampire(player)) {
               boolean isNourished = checkFDEffects && player.m_21023_((MobEffect)ModEffects.NOURISHMENT.get());
               boolean isHunger = player.m_21023_(MobEffects.f_19612_);
               boolean isStuffed = checkLetsDoBakeryEffects && player.m_21220_().stream().anyMatch((e) -> e.m_19576_().contains("stuffed"));
               boolean isSaturated = checkLetsDoBreweryEffects && player.m_21220_().stream().anyMatch((e) -> e.m_19576_().contains("saturated"));
               boolean isSitting = player.m_20159_();
               if ((Boolean)CommonConfig.DEPLETES_WHEN_NAUSEA.get() && player.m_21220_().stream().anyMatch((e) -> e.m_19544_().equals(MobEffects.f_19604_))) {
                  this.addExhaustion(player, 0.06F);
               }

               if (isHunger) {
                  this.exhaustion -= 0.005F * (float)(player.m_21124_(MobEffects.f_19612_).m_19564_() + 1) * ThirstHelper.getExhaustionBiomeModifier(player) * ThirstHelper.getExhaustionFireProtModifier(player) * ThirstHelper.getExhaustionFireResistanceModifier(player);
               }

               if (!isSitting && !isNourished && !isStuffed && !isSaturated) {
                  this.updateExhaustion(player);
               }

               if (this.exhaustion > 4.0F) {
                  this.exhaustion -= 4.0F;
                  if (this.quenched > 0) {
                     --this.quenched;
                  } else if (difficulty != Difficulty.PEACEFUL || (Boolean)CommonConfig.THIRST_DEPLETION_IN_PEACEFUL.get()) {
                     this.thirst = Math.max(this.thirst - 1, 0);
                  }
               }

               ++this.syncTimer;
               if (this.syncTimer > 10 && !player.m_9236_().m_5776_()) {
                  if (difficulty == Difficulty.PEACEFUL && !(Boolean)CommonConfig.THIRST_DEPLETION_IN_PEACEFUL.get()) {
                     this.thirst = Math.min(this.thirst + 1, 20);
                  }

                  float angle = Mth.m_14177_(player.m_146909_());
                  if (angle <= -80.0F && player.m_9236_().m_46758_(player.m_20183_().m_7494_()) && (Boolean)CommonConfig.CAN_DRINK_RAIN_WATETR.get()) {
                     this.thirst = Math.min(this.thirst + 1, 20);
                     this.quenched = Math.min(this.quenched + 1, 20);
                  }

                  this.updateThirstData(player);
                  this.syncTimer = 0;
               }

               if (this.thirst <= 0) {
                  ++this.damageTimer;
                  if (this.damageTimer >= 40) {
                     if (player.m_21223_() > 10.0F || difficulty == Difficulty.HARD || player.m_21223_() > 0.0F && difficulty == Difficulty.NORMAL) {
                        player.m_6469_(ModDamageSource.getDamageSource(player.m_9236_(), ModDamageSource.DIE_OF_THIRST_KEY), 1.0F);
                     }

                     this.damageTimer = 0;
                  }
               }

            }
         }
      }
   }

   void updateExhaustion(Player player) {
      float hungerExhaustion = player.m_36324_().m_150380_();
      float normalizedHungerExhaustion = hungerExhaustion < this.prevTickExhaustion ? (this.exhaustionRecalculate ? hungerExhaustion + 4.0F : hungerExhaustion) : hungerExhaustion;
      if (this.exhaustionRecalculate) {
         this.exhaustionRecalculate = false;
      }

      float deltaExhaustion = normalizedHungerExhaustion - this.prevTickExhaustion;
      this.addExhaustion(player, deltaExhaustion);
      this.prevTickExhaustion = hungerExhaustion;
   }

   public void updateThirstData(Player player) {
      ThirstModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new PlayerThirstSyncMessage(this.thirst, this.quenched, this.exhaustion, this.shouldTickThirst));
   }

   public void setJustHealed() {
      this.justHealed = true;
   }

   public void ExhaustionRecalculate() {
      this.exhaustionRecalculate = true;
   }

   public void copy(IThirst cap) {
      this.thirst = cap.getThirst();
      this.quenched = cap.getQuenched();
      this.exhaustion = cap.getExhaustion();
      this.shouldTickThirst = cap.getShouldTickThirst();
   }

   public void addExhaustion(Player player, float amount) {
      if (!(Boolean)CommonConfig.HEALTH_REGEN_DEPLETES_HYDRATION.get() && this.justHealed) {
         amount = 0.0F;
      }

      if (!(Boolean)CommonConfig.HEALTH_REGEN_DEHYDRATION_IS_BIOME_DEPENDENT.get() && this.justHealed) {
         this.exhaustion += amount;
      } else {
         this.exhaustion += amount * ThirstHelper.getExhaustionBiomeModifier(player) * ThirstHelper.getExhaustionFireProtModifier(player) * ThirstHelper.getExhaustionFireResistanceModifier(player);
      }

      if (this.justHealed) {
         this.justHealed = false;
      }

      this.updateThirstData(player);
   }

   public CompoundTag serializeNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.m_128405_("thirst", this.thirst);
      nbt.m_128405_("quenched", this.quenched);
      nbt.m_128350_("exhaustion", this.exhaustion);
      nbt.m_128379_("enable", this.shouldTickThirst);
      return nbt;
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.thirst = nbt.m_128451_("thirst");
      this.quenched = nbt.m_128451_("quenched");
      this.exhaustion = nbt.m_128457_("exhaustion");
      this.shouldTickThirst = !nbt.m_128441_("enable") || nbt.m_128471_("enable");
   }
}
