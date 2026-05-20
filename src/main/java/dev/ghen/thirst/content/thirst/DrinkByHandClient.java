package dev.ghen.thirst.content.thirst;

import dev.ghen.thirst.foundation.config.ClientConfig;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.foundation.network.message.DrinkByHandMessage;
import dev.ghen.thirst.foundation.util.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrinkByHandClient {
   public static void drinkByHand() {
      Minecraft mc = Minecraft.m_91087_();
      Player player = mc.f_91074_;
      Level level = mc.f_91073_;
      BlockPos blockPos = MathHelper.getPlayerPOVHitResult(level, player, Fluid.ANY).m_82425_();
      if (level.m_6425_(blockPos).m_205070_(FluidTags.f_13131_) && player.m_6047_() && !player.m_20147_()) {
         boolean HandAvailable;
         if (!(Boolean)ClientConfig.DRINK_BOTH_HAND_NEEDED.get()) {
            HandAvailable = player.m_21120_(InteractionHand.MAIN_HAND).m_41619_();
         } else {
            HandAvailable = player.m_21120_(InteractionHand.MAIN_HAND).m_41619_() && player.m_21120_(InteractionHand.OFF_HAND).m_41619_();
         }

         if (HandAvailable) {
            level.m_6263_(player, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11911_, SoundSource.NEUTRAL, 1.0F, 1.0F);
            ThirstModPacketHandler.INSTANCE.sendToServer(new DrinkByHandMessage(blockPos));
         }
      }

   }
}
