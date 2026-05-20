package dev.ghen.thirst.foundation.mixin.toughasnails;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.util.MathHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toughasnails.api.block.TANBlocks;
import toughasnails.block.RainCollectorBlock;
import toughasnails.item.EmptyCanteenItem;

@Mixin(
   value = {EmptyCanteenItem.class},
   remap = false
)
public abstract class MixinEmptyCanteenItem {
   @Shadow(
      remap = false
   )
   protected abstract ItemStack replaceCanteen(ItemStack var1, Player var2, ItemStack var3);

   @Shadow
   public abstract Item getPurifiedWaterCanteen();

   @Shadow
   public abstract Item getWaterCanteen();

   @Shadow
   public abstract Item getDirtyWaterCanteen();

   @Inject(
      method = {"use"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void use(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
      ItemStack stack = player.m_21120_(hand);
      Level level = player.m_9236_();
      BlockPos blockPos = MathHelper.getPlayerPOVHitResult(level, player, Fluid.ANY).m_82425_();
      BlockState state = world.m_8055_(blockPos);
      if (!world.m_7966_(player, blockPos)) {
         cir.setReturnValue(InteractionResultHolder.m_19098_(stack));
      }

      if (level.m_6425_(blockPos).m_205070_(FluidTags.f_13131_)) {
         SoundEvent sound = SoundEvents.f_11770_;
         level.m_6263_(player, player.m_20185_(), player.m_20186_(), player.m_20189_(), sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
         level.m_142346_(player, GameEvent.f_157816_, blockPos);
         int purity = WaterPurity.getBlockPurity(level, blockPos);
         ItemStack filledItem;
         if (purity == 3) {
            filledItem = this.getPurifiedWaterCanteen().m_7968_();
         } else if (purity == 2) {
            filledItem = this.getWaterCanteen().m_7968_();
         } else {
            filledItem = this.getDirtyWaterCanteen().m_7968_();
         }

         ItemStack result = ItemUtils.m_41813_(stack, player, filledItem);
         cir.setReturnValue(InteractionResultHolder.m_19092_(this.replaceCanteen(stack, player, result), world.m_5776_()));
      } else if (state.m_60734_() instanceof RainCollectorBlock) {
         int waterLevel = (Integer)state.m_61143_(RainCollectorBlock.LEVEL);
         if (waterLevel > 0 && !world.m_5776_()) {
            world.m_6263_(player, player.m_20185_(), player.m_20186_(), player.m_20189_(), SoundEvents.f_11770_, SoundSource.NEUTRAL, 1.0F, 1.0F);
            ((RainCollectorBlock)TANBlocks.RAIN_COLLECTOR).setWaterLevel(world, blockPos, state, waterLevel - 1);
            cir.setReturnValue(InteractionResultHolder.m_19090_(this.replaceCanteen(stack, player, this.getPurifiedWaterCanteen().m_7968_())));
         }
      }

   }
}
