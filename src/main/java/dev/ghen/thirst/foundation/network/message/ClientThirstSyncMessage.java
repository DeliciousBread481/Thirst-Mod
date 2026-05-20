package dev.ghen.thirst.foundation.network.message;

import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

@OnlyIn(Dist.CLIENT)
class ClientThirstSyncMessage {
   public static void handlePacket(PlayerThirstSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
      Player player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent((cap) -> {
            cap.setThirst(message.thirst);
            cap.setQuenched(message.quenched);
            cap.setExhaustion(message.exhaustion);
            cap.setShouldTickThirst(message.enable);
         });
      }

   }
}
