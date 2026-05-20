package dev.ghen.thirst.foundation.network.message;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.CommonConfig;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

public class DrinkByHandMessage {
   public BlockPos pos;

   public DrinkByHandMessage(BlockPos pos) {
      this.pos = pos;
   }

   public static void encode(DrinkByHandMessage message, FriendlyByteBuf buffer) {
      buffer.m_130064_(message.pos);
   }

   public static DrinkByHandMessage decode(FriendlyByteBuf buffer) {
      return new DrinkByHandMessage(buffer.m_130135_());
   }

   public static void handle(DrinkByHandMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
      NetworkEvent.Context context = (NetworkEvent.Context)contextSupplier.get();
      if (context.getDirection().getReceptionSide().isServer()) {
         context.enqueueWork(() -> {
            Player player = context.getSender();
            Level level = player.m_9236_();
            player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent((cap) -> {
               if (cap.getThirst() != 20) {
                  int purity = WaterPurity.getBlockPurity(level, message.pos);
                  level.m_6263_(player, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11911_, SoundSource.NEUTRAL, 1.0F, 1.0F);
                  if (WaterPurity.givePurityEffects(player, purity)) {
                     cap.drink(player, ((Number)CommonConfig.HAND_DRINKING_HYDRATION.get()).intValue(), ((Number)CommonConfig.HAND_DRINKING_QUENCHED.get()).intValue());
                  }

               }
            });
         });
      }

      context.setPacketHandled(true);
   }
}
