package dev.ghen.thirst.foundation.common.event;

import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.content.purity.ContainerWithPurity;
import dev.ghen.thirst.content.purity.WaterPurity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

public class RegisterThirstValueEvent extends Event {
   public void addFood(Item item, int thirst, int quenched) {
      ThirstHelper.VALID_FOODS.putIfAbsent(item, new Number[]{thirst, quenched});
   }

   public void addDrink(Item item, int thirst, int quenched) {
      ThirstHelper.VALID_DRINKS.putIfAbsent(item, new Number[]{thirst, quenched});
   }

   public void addContainer(ContainerWithPurity container) {
      WaterPurity.addContainer(container);
   }

   public void addContainer(Item item) {
      WaterPurity.addContainer(new ContainerWithPurity(new ItemStack(item)));
   }

   public boolean isCancelable() {
      return false;
   }
}
