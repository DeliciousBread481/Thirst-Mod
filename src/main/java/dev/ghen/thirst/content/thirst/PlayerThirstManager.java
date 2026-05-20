package dev.ghen.thirst.content.thirst;

import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.common.item.DrinkableItem;
import dev.ghen.thirst.foundation.config.CommonConfig;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.PotionItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class PlayerThirstManager {
   @SubscribeEvent
   public static void attachCapabilityToEntityHandler(AttachCapabilitiesEvent<Entity> event) {
      if (event.getObject() instanceof Player) {
         final IThirst playerThirstCap = new PlayerThirst();
         final LazyOptional<IThirst> capOptional = LazyOptional.of(() -> playerThirstCap);
         final Capability<IThirst> capability = ModCapabilities.PLAYER_THIRST;
         ICapabilityProvider provider = new ICapabilitySerializable<CompoundTag>() {
            @Nonnull
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction direction) {
               return cap == capability ? capOptional.cast() : LazyOptional.empty();
            }

            public CompoundTag serializeNBT() {
               return playerThirstCap.serializeNBT();
            }

            public void deserializeNBT(CompoundTag nbt) {
               playerThirstCap.deserializeNBT(nbt);
            }
         };
         event.addCapability(Thirst.asResource("thirst"), provider);
      }

   }

   @SubscribeEvent
   public static void drinkByHand(PlayerInteractEvent.RightClickBlock event) {
      if ((Boolean)CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().m_9236_().f_46443_) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DrinkByHandClient::drinkByHand);
      }

   }

   @SubscribeEvent
   public static void drinkByHand(PlayerInteractEvent.RightClickEmpty event) {
      if ((Boolean)CommonConfig.CAN_DRINK_BY_HAND.get() && event.getEntity().m_9236_().f_46443_) {
         DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DrinkByHandClient::drinkByHand);
      }

   }

   @SubscribeEvent
   public static void drink(LivingEntityUseItemEvent.Finish event) {
      if (event.getEntity() instanceof Player && ThirstHelper.itemRestoresThirst(event.getItem())) {
         if (event.getItem().m_41720_() instanceof PotionItem) {
            return;
         }

         if (event.getItem().m_41720_().m_41472_()) {
            return;
         }

         if (event.getItem().m_41720_() instanceof DrinkableItem) {
            return;
         }

         PlayerThirst.drink(event.getItem(), (Player)event.getEntity());
      }

   }

   @SubscribeEvent
   public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
      if (event.phase == Phase.START) {
         Player var2 = event.player;
         if (var2 instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)var2;
            serverPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent((cap) -> cap.tick(serverPlayer));
         }
      }

   }

   @SubscribeEvent
   public static void endFix(PlayerEvent.Clone event) {
      if (!event.getEntity().m_9236_().f_46443_) {
         Player oldPlayer = event.getOriginal();
         oldPlayer.reviveCaps();
         if (!event.isWasDeath()) {
            event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent((cap) -> {
               LazyOptional var10000 = oldPlayer.getCapability(ModCapabilities.PLAYER_THIRST);
               Objects.requireNonNull(cap);
               var10000.ifPresent(cap::copy);
            });
         } else {
            event.getEntity().getCapability(ModCapabilities.PLAYER_THIRST).ifPresent((cap) -> oldPlayer.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent((oldCap) -> cap.setShouldTickThirst(oldCap.getShouldTickThirst())));
         }

         oldPlayer.invalidateCaps();
      }

   }

   @SubscribeEvent
   public static void initDrinks(ServerStartedEvent event) {
      ThirstHelper.init();
   }
}
