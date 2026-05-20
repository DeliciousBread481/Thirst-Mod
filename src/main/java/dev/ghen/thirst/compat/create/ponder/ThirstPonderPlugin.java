package dev.ghen.thirst.compat.create.ponder;

import com.simibubi.create.foundation.ponder.PonderWorldBlockEntityFix;
import net.createmod.ponder.api.level.PonderLevel;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ThirstPonderPlugin implements PonderPlugin {
   public @NotNull String getModId() {
      return "thirst";
   }

   public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
      ThirstPonders.registerScenes(helper);
   }

   public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
      ThirstPonders.registerTags(helper);
   }

   public void onPonderLevelRestore(@NotNull PonderLevel ponderLevel) {
      PonderWorldBlockEntityFix.fixControllerBlockEntities(ponderLevel);
   }
}
