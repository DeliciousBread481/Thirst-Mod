package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CampfireBlockEntity.class})
public class MixinCampfireBlockEntity {
   @Inject(
      method = {"particleTick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private static void waterVapour(Level level, BlockPos pos, BlockState blockState, CampfireBlockEntity campfire, CallbackInfo ci) {
      RandomSource random = level.m_213780_();
      int l = ((Direction)blockState.m_61143_(CampfireBlock.f_51230_)).m_122416_();
      boolean cancel = false;

      for(int i = 0; i < campfire.m_59065_().size(); ++i) {
         ItemStack itemstack = (ItemStack)campfire.m_59065_().get(i);
         if (WaterPurity.isWaterFilledContainer(itemstack)) {
            cancel = true;
            if (random.m_188501_() < 0.2F) {
               Direction direction = Direction.m_122407_(Math.floorMod(i + l, 4));
               float f = 0.3125F;
               double d0 = (double)pos.m_123341_() + (double)0.5F - (double)((float)direction.m_122429_() * 0.3125F) + (double)((float)direction.m_122427_().m_122429_() * 0.3125F);
               double d1 = (double)pos.m_123342_() + 0.6;
               double d2 = (double)pos.m_123343_() + (double)0.5F - (double)((float)direction.m_122431_() * 0.3125F) + (double)((float)direction.m_122427_().m_122431_() * 0.3125F);
               level.m_7106_(ParticleTypes.f_123806_, d0, d1, d2, (double)0.0F, 0.001, (double)0.0F);
            }
         }
      }

      if (cancel) {
         if (random.m_188501_() < 0.11F) {
            for(int i = 0; i < random.m_188503_(2) + 2; ++i) {
               CampfireBlock.m_51251_(level, pos, (Boolean)blockState.m_61143_(CampfireBlock.f_51228_), false);
            }
         }

         ci.cancel();
      }

   }
}
