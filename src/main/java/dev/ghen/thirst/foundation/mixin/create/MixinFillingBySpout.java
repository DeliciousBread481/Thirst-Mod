package dev.ghen.thirst.foundation.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.ghen.thirst.content.purity.WaterPurity;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {FillingBySpout.class},
   remap = false
)
public class MixinFillingBySpout {
   @Final
   @Shadow
   private static RecipeWrapper WRAPPER;

   @Inject(
      method = {"fillItem"},
      at = {@At("HEAD")},
      cancellable = true,
      remap = false
   )
   private static void fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
      FluidStack toFill = availableFluid.copy();
      toFill.setAmount(requiredAmount);
      WRAPPER.m_6836_(0, stack);
      if (WaterPurity.hasPurity(availableFluid)) {
         int purity = WaterPurity.getPurity(availableFluid);
         FillingRecipe fillingRecipe = (FillingRecipe)SequencedAssemblyRecipe.getRecipe(world, WRAPPER, AllRecipeTypes.FILLING.getType(), FillingRecipe.class).filter((fr) -> fr.getRequiredFluid().test(toFill)).orElseGet(() -> {
            for(Recipe<RecipeWrapper> recipe : world.m_7465_().m_44056_(AllRecipeTypes.FILLING.getType(), WRAPPER, world)) {
               FillingRecipe fr = (FillingRecipe)recipe;
               FluidIngredient requiredFluid = fr.getRequiredFluid();
               if (requiredFluid.test(toFill)) {
                  return fr;
               }
            }

            return null;
         });
         if (fillingRecipe != null) {
            List<ItemStack> results = fillingRecipe.rollResults();
            availableFluid.shrink(requiredAmount);
            stack.m_41774_(1);
            cir.setReturnValue(results.isEmpty() ? ItemStack.f_41583_ : (WaterPurity.isWaterFilledContainer((ItemStack)results.get(0)) ? WaterPurity.addPurity((ItemStack)results.get(0), purity) : (ItemStack)results.get(0)));
         } else {
            ItemStack output = GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid);
            cir.setReturnValue(output);
         }
      }

   }
}
