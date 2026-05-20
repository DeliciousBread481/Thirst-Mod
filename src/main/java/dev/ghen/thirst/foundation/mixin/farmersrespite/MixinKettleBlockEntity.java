package dev.ghen.thirst.foundation.mixin.farmersrespite;

import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.config.CommonConfig;
import java.util.Optional;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.crafting.KettlePouringRecipe;

@Mixin(
   value = {KettleBlockEntity.class},
   remap = false
)
public class MixinKettleBlockEntity {
   @Redirect(
      method = {"processBrewing"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraftforge/fluids/capability/templates/FluidTank;setFluid(Lnet/minecraftforge/fluids/FluidStack;)V"
)
   )
   private void ProcessBrewing(FluidTank instance, FluidStack stack) {
      int purity = Math.min(WaterPurity.getPurity(instance.getFluid()) + ((Number)CommonConfig.KETTLE_PURIFICATION_LEVELS.get()).intValue(), 3);
      instance.setFluid(WaterPurity.addPurity(stack, purity));
   }

   @Redirect(
      method = {"canBrew"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraftforge/fluids/FluidStack;areFluidStackTagsEqual(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/FluidStack;)Z"
)
   )
   private boolean canBrew(FluidStack stack1, FluidStack other) {
      FluidStack stack = stack1.copy();
      stack.removeChildTag("Purity");
      return WaterPurity.matchRecipe(stack, other);
   }

   @Overwrite
   public ItemStack fluidExtract(KettleBlockEntity kettle, ItemStack slotIn, ItemStack slotOut) {
      Item container = slotIn.m_41720_();
      ItemStack output = ItemStack.f_41583_;
      Optional<KettlePouringRecipe> recipe = kettle.getPouringRecipe(container, kettle.getFluidTank().getFluid());
      boolean changed = false;
      if (recipe.isPresent() && (kettle.getFluidTank().isEmpty() || kettle.getFluidTank().getFluid().getFluid().m_6212_(((KettlePouringRecipe)recipe.get()).getFluid()))) {
         if (container.equals(((KettlePouringRecipe)recipe.get()).getContainer().m_41720_()) && ((KettlePouringRecipe)recipe.get()).getAmount() <= kettle.getFluidTank().getFluidAmount()) {
            for(int purity = WaterPurity.getPurity(kettle.getFluidTank().getFluid()); kettle.getFluidTank().getFluidAmount() >= ((KettlePouringRecipe)recipe.get()).getAmount() && (output.m_41619_() && slotOut.m_41619_() || ItemHandlerHelper.canItemStacksStack(slotOut, WaterPurity.addPurity(((KettlePouringRecipe)recipe.get()).getOutput().m_255036_(output.m_41613_() + 1), purity))); changed = true) {
               if (slotOut.m_41613_() + 1 > slotOut.m_41741_() || slotIn.m_41613_() == 0 || slotIn.m_41619_() || slotIn.m_41613_() < ((KettlePouringRecipe)recipe.get()).getOutput().m_41613_()) {
                  break;
               }

               kettle.getFluidTank().drain(new FluidStack(kettle.getFluidTank().getFluid(), ((KettlePouringRecipe)recipe.get()).getAmount()), FluidAction.EXECUTE);
               slotIn.m_41774_(((KettlePouringRecipe)recipe.get()).getContainer().m_41613_());
               if (output.m_41619_()) {
                  output = ((KettlePouringRecipe)recipe.get()).getOutput().m_41777_();
                  WaterPurity.addPurity(output, purity);
               } else {
                  output.m_41769_(((KettlePouringRecipe)recipe.get()).getOutput().m_41613_());
               }
            }

            if (changed) {
               if (kettle.m_58904_().m_5776_()) {
                  kettle.m_58904_().m_245747_(kettle.m_58899_(), SoundEvents.f_11770_, SoundSource.BLOCKS, 1.0F, 1.0F, false);
               }

               kettle.m_6596_();
            }
         } else if (container.equals(((KettlePouringRecipe)recipe.get()).getOutput().m_41720_()) && kettle.getFluidTank().getFluidAmount() + ((KettlePouringRecipe)recipe.get()).getAmount() <= kettle.getFluidTank().getCapacity()) {
            for(; kettle.getFluidTank().getFluidAmount() + ((KettlePouringRecipe)recipe.get()).getAmount() <= kettle.getFluidTank().getCapacity() && slotIn.m_41613_() != 0 && !slotIn.m_41619_() && slotIn.m_41613_() >= ((KettlePouringRecipe)recipe.get()).getContainer().m_41613_() && (output.m_41619_() && slotOut.m_41619_() || ItemHandlerHelper.canItemStacksStack(slotOut, ((KettlePouringRecipe)recipe.get()).getContainer().m_255036_(output.m_41613_() + 1))); changed = true) {
               if (slotOut.m_41613_() + 1 > slotOut.m_41741_()) {
                  break;
               }

               kettle.getFluidTank().fill(new FluidStack(((KettlePouringRecipe)recipe.get()).getFluid(), ((KettlePouringRecipe)recipe.get()).getAmount()), FluidAction.EXECUTE);
               slotIn.m_41774_(((KettlePouringRecipe)recipe.get()).getOutput().m_41613_());
               if (output.m_41619_()) {
                  output = ((KettlePouringRecipe)recipe.get()).getContainer().m_41777_();
               } else {
                  output.m_41769_(((KettlePouringRecipe)recipe.get()).getContainer().m_41613_());
               }
            }

            if (changed) {
               if (kettle.m_58904_().m_5776_()) {
                  kettle.m_58904_().m_245747_(kettle.m_58899_(), SoundEvents.f_11770_, SoundSource.BLOCKS, 1.0F, 1.0F, false);
               }

               kettle.m_6596_();
            }
         }
      }

      LazyOptional<IFluidHandlerItem> fluidHandler = slotIn.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM);
      IFluidHandlerItem iFluidItemHandler = (IFluidHandlerItem)fluidHandler.orElse((Object)null);
      if (fluidHandler.isPresent() && !slotIn.m_41619_()) {
         if (!kettle.getFluidTank().getFluid().isFluidEqual(iFluidItemHandler.getFluidInTank(0)) && !kettle.getFluidTank().getFluid().isEmpty()) {
            if (!kettle.getFluidTank().getFluid().isEmpty() && iFluidItemHandler.isFluidValid(0, kettle.getFluidTank().getFluid())) {
               int amountToDrain = kettle.getFluidTank().getFluidAmount();
               int amount = iFluidItemHandler.fill(new FluidStack(kettle.getFluidTank().getFluid(), amountToDrain), FluidAction.SIMULATE);
               if (amount > 0) {
                  iFluidItemHandler.fill(new FluidStack(kettle.getFluidTank().getFluid(), amountToDrain), FluidAction.EXECUTE);
                  kettle.getFluidTank().drain(amountToDrain, FluidAction.EXECUTE);
                  if (amount <= amountToDrain) {
                     slotIn.m_41764_(0);
                     if (output.m_41619_()) {
                        output = iFluidItemHandler.getContainer().m_41777_();
                     } else {
                        output.m_41769_(iFluidItemHandler.getContainer().m_41613_());
                     }

                     kettle.m_6596_();
                  }
               }
            }
         } else {
            int amountToDrain = kettle.getFluidTank().getCapacity() - kettle.getFluidTank().getFluidAmount();
            int amount = iFluidItemHandler.drain(amountToDrain, FluidAction.SIMULATE).getAmount();
            if (amount > 0) {
               kettle.getFluidTank().fill(iFluidItemHandler.drain(amountToDrain, FluidAction.EXECUTE), FluidAction.EXECUTE);
               if (amount <= amountToDrain) {
                  slotIn.m_41764_(0);
                  if (output.m_41619_()) {
                     output = iFluidItemHandler.getContainer().m_41777_();
                  } else {
                     output.m_41769_(iFluidItemHandler.getContainer().m_41613_());
                  }

                  kettle.m_6596_();
               }
            }
         }
      }

      return output;
   }
}
