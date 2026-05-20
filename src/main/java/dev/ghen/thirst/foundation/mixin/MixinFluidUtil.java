package dev.ghen.thirst.foundation.mixin;

import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(
   value = {FluidUtil.class},
   remap = false
)
public class MixinFluidUtil {
   @Overwrite
   public static @NotNull ItemStack getFilledBucket(@NotNull FluidStack fluidStack) {
      Fluid fluid = fluidStack.getFluid();
      if (fluidStack.hasTag() && !fluidStack.getTag().m_128456_()) {
         if (WaterPurity.hasPurity(fluidStack)) {
            return WaterPurity.addPurity(new ItemStack(fluidStack.getFluid().m_6859_()), WaterPurity.getPurity(fluidStack));
         }
      } else {
         if (fluid == Fluids.f_76193_) {
            return new ItemStack(Items.f_42447_);
         }

         if (fluid == Fluids.f_76195_) {
            return new ItemStack(Items.f_42448_);
         }
      }

      return new ItemStack(fluidStack.getFluid().m_6859_());
   }
}
