package dev.ghen.thirst.foundation.common.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public interface IThirst {
   int getThirst();

   void setThirst(int var1);

   int getQuenched();

   void setQuenched(int var1);

   float getExhaustion();

   void setExhaustion(float var1);

   void addExhaustion(Player var1, float var2);

   void tick(Player var1);

   void drink(Player var1, int var2, int var3);

   void updateThirstData(Player var1);

   void setJustHealed();

   void ExhaustionRecalculate();

   void setShouldTickThirst(boolean var1);

   boolean getShouldTickThirst();

   void copy(IThirst var1);

   CompoundTag serializeNBT();

   void deserializeNBT(CompoundTag var1);
}
