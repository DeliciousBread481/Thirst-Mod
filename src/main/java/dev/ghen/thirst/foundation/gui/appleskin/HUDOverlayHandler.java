package dev.ghen.thirst.foundation.gui.appleskin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ghen.thirst.Thirst;
import dev.ghen.thirst.api.ThirstHelper;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.config.ClientConfig;
import dev.ghen.thirst.foundation.gui.ThirstBarRenderer;
import java.util.Random;
import java.util.Vector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import squeek.appleskin.ModConfig;
import squeek.appleskin.util.IntPoint;

@OnlyIn(Dist.CLIENT)
public class HUDOverlayHandler {
   private static float unclampedFlashAlpha = 0.0F;
   private static float flashAlpha = 0.0F;
   private static byte alphaDir = 1;
   protected static int foodIconsOffset;
   public static final Vector<IntPoint> foodBarOffsets = new Vector();
   private static final Random random = new Random();
   private static final ResourceLocation modIcons = Thirst.asResource("textures/gui/appleskin_icons.png");
   static ResourceLocation THIRST_LEVEL_ELEMENT = Thirst.asResource("thirst_level");

   public static void init() {
      MinecraftForge.EVENT_BUS.register(new HUDOverlayHandler());
   }

   @SubscribeEvent
   public void onRenderGuiOverlayPre(RenderGuiOverlayEvent.Pre event) {
      if (event.getOverlay() == GuiOverlayManager.findOverlay(THIRST_LEVEL_ELEMENT)) {
         Minecraft mc = Minecraft.m_91087_();
         ForgeGui gui = (ForgeGui)mc.f_91065_;
         boolean isMounted = mc.f_91074_.m_20202_() instanceof LivingEntity;
         boolean isAlive = mc.f_91074_.m_6084_();
         if (isAlive && (Boolean)ModConfig.SHOW_FOOD_EXHAUSTION_UNDERLAY.get() && !isMounted && !mc.f_91066_.f_92062_ && gui.shouldDrawSurvivalElements() && !ThirstBarRenderer.cancelRender) {
            renderExhaustion(gui, event.getGuiGraphics());
         }
      }

   }

   @SubscribeEvent
   public void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
      if (event.getOverlay() == GuiOverlayManager.findOverlay(THIRST_LEVEL_ELEMENT)) {
         Minecraft mc = Minecraft.m_91087_();
         ForgeGui gui = (ForgeGui)mc.f_91065_;
         boolean isMounted = mc.f_91074_.m_20202_() instanceof LivingEntity;
         boolean isAlive = mc.f_91074_.m_6084_();
         if (isAlive && !isMounted && !mc.f_91066_.f_92062_ && gui.shouldDrawSurvivalElements() && !ThirstBarRenderer.cancelRender) {
            renderThirstOverlay(event.getGuiGraphics());
         }
      }

   }

   public static void renderExhaustion(ForgeGui gui, GuiGraphics mStack) {
      foodIconsOffset = gui.rightHeight;
      Minecraft mc = Minecraft.m_91087_();
      Player player = mc.f_91074_;

      assert player != null;

      int right = mc.m_91268_().m_85445_() / 2 + 91 + (Integer)ClientConfig.THIRST_BAR_X_OFFSET.get();
      int top = mc.m_91268_().m_85446_() - foodIconsOffset + (Integer)ClientConfig.THIRST_BAR_Y_OFFSET.get();
      float exhaustion = ((IThirst)player.getCapability(ModCapabilities.PLAYER_THIRST).orElse((Object)null)).getExhaustion();
      drawExhaustionOverlay(exhaustion, mStack, right, top);
   }

   public static void renderThirstOverlay(GuiGraphics guiGraphics) {
      if (shouldRenderAnyOverlays()) {
         Minecraft mc = Minecraft.m_91087_();
         Player player = mc.f_91074_;

         assert player != null;

         IThirst thirstData = (IThirst)player.getCapability(ModCapabilities.PLAYER_THIRST).orElse((Object)null);
         int top = mc.m_91268_().m_85446_() - foodIconsOffset + (Integer)ClientConfig.THIRST_BAR_Y_OFFSET.get();
         int right = mc.m_91268_().m_85445_() / 2 + 91 + (Integer)ClientConfig.THIRST_BAR_X_OFFSET.get();
         generateHungerBarOffsets(top, right, mc.f_91065_.m_93079_(), player);
         if ((Boolean)ModConfig.SHOW_SATURATION_OVERLAY.get()) {
            drawSaturationOverlay(0.0F, (float)thirstData.getQuenched(), guiGraphics, right, top, 1.0F);
         }

         ItemStack heldItem = player.m_21205_();
         if ((Boolean)ModConfig.SHOW_FOOD_VALUES_OVERLAY_WHEN_OFFHAND.get() && !ThirstHelper.itemRestoresThirst(heldItem)) {
            heldItem = player.m_21206_();
         }

         boolean shouldRenderHeldItemValues = !heldItem.m_41619_() && ThirstHelper.itemRestoresThirst(heldItem);
         if (!shouldRenderHeldItemValues) {
            resetFlash();
         } else {
            ThirstValues thirstValues = new ThirstValues(ThirstHelper.getThirst(heldItem), (float)ThirstHelper.getQuenched(heldItem));
            int drinkThirst = thirstValues.thirst;
            float thirstQuenchedIncrement = thirstValues.getQuenchedIncrement();
            if (thirstData.getThirst() < 20) {
               drawHungerOverlay(drinkThirst, thirstData.getThirst(), guiGraphics, right, top, flashAlpha);
            }

            if (!ThirstHelper.isFood(heldItem) || player.m_36324_().m_38702_() < 20) {
               drawSaturationOverlay(thirstValues.quenchedModifier, (float)thirstData.getQuenched(), guiGraphics, right, top, flashAlpha);
            }

         }
      }
   }

   public static void drawSaturationOverlay(float saturationGained, float saturationLevel, GuiGraphics guiGraphics, int right, int top, float alpha) {
      if (!(saturationLevel + saturationGained < 0.0F)) {
         enableAlpha(alpha);
         RenderSystem.setShaderTexture(0, modIcons);
         float modifiedSaturation = Math.max(0.0F, Math.min(saturationLevel + saturationGained, 20.0F));
         int startSaturationBar = 0;
         int endSaturationBar = (int)Math.ceil((double)(modifiedSaturation / 2.0F));
         if (saturationGained != 0.0F) {
            startSaturationBar = (int)Math.max(saturationLevel / 2.0F, 0.0F);
         }

         int iconSize = 9;

         for(int i = startSaturationBar; i < endSaturationBar; ++i) {
            IntPoint offset = (IntPoint)foodBarOffsets.get(i);
            if (offset != null) {
               int x = right + offset.x;
               int y = top + offset.y;
               int v = 0;
               int u = 0;
               float effectiveSaturationOfBar = modifiedSaturation / 2.0F - (float)i;
               if (effectiveSaturationOfBar >= 1.0F) {
                  u = 3 * iconSize;
               } else if ((double)effectiveSaturationOfBar > (double)0.5F) {
                  u = 2 * iconSize;
               } else if ((double)effectiveSaturationOfBar > (double)0.25F) {
                  u = iconSize;
               }

               guiGraphics.m_280218_(modIcons, x, y, u, v, iconSize, iconSize);
            }
         }

         RenderSystem.setShaderTexture(0, ThirstBarRenderer.MC_ICONS);
         disableAlpha();
      }
   }

   public static void drawHungerOverlay(int hungerRestored, int foodLevel, GuiGraphics guiGraphics, int right, int top, float alpha) {
      if (hungerRestored > 0) {
         enableAlpha(alpha);
         RenderSystem.setShaderTexture(0, ThirstBarRenderer.THIRST_ICONS);
         int modifiedFood = Math.max(0, Math.min(20, foodLevel + hungerRestored));
         int startFoodBars = Math.max(0, foodLevel / 2);
         int endFoodBars = (int)Math.ceil((double)((float)modifiedFood / 2.0F));
         int iconStartOffset = 5;
         int iconSize = 9;

         for(int i = startFoodBars; i < endFoodBars; ++i) {
            IntPoint offset = (IntPoint)foodBarOffsets.get(i);
            if (offset != null) {
               int x = right + offset.x;
               int y = top + offset.y;
               int v = 3 * iconSize;
               int u = iconStartOffset + 4 * iconSize;
               if (i * 2 + 1 == modifiedFood) {
                  u -= iconSize - 1;
               }

               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
               guiGraphics.m_280163_(ThirstBarRenderer.THIRST_ICONS, x, y, (float)u, (float)v, iconSize, iconSize, 25, 9);
            }
         }

         disableAlpha();
      }
   }

   public static void drawExhaustionOverlay(float exhaustion, GuiGraphics guiGraphics, int right, int top) {
      RenderSystem.setShaderTexture(0, modIcons);
      float maxExhaustion = 4.0F;
      float ratio = Math.min(1.0F, Math.max(0.0F, exhaustion / maxExhaustion));
      int width = (int)(ratio * 81.0F);
      int height = 9;
      enableAlpha(0.75F);
      guiGraphics.m_280218_(modIcons, right - width, top, 81 - width, 18, width, height);
      disableAlpha();
      RenderSystem.setShaderTexture(0, ThirstBarRenderer.MC_ICONS);
   }

   public static void enableAlpha(float alpha) {
      RenderSystem.enableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.blendFunc(770, 771);
   }

   public static void disableAlpha() {
      RenderSystem.disableBlend();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   @SubscribeEvent
   public void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase == Phase.END) {
         unclampedFlashAlpha += (float)alphaDir * 0.125F;
         if (unclampedFlashAlpha >= 1.5F) {
            alphaDir = -1;
         } else if (unclampedFlashAlpha <= -0.5F) {
            alphaDir = 1;
         }

         flashAlpha = Math.max(0.0F, Math.min(1.0F, unclampedFlashAlpha)) * 0.65F;
      }
   }

   public static void resetFlash() {
      flashAlpha = 0.0F;
      unclampedFlashAlpha = 0.0F;
      alphaDir = 1;
   }

   private static boolean shouldRenderAnyOverlays() {
      return true;
   }

   private static void generateHungerBarOffsets(int top, int right, int ticks, Player player) {
      int preferFoodBars = 10;
      IThirst thirstData = (IThirst)player.getCapability(ModCapabilities.PLAYER_THIRST).orElse((Object)null);
      float quenched = (float)thirstData.getQuenched();
      int thirst = thirstData.getThirst();
      boolean shouldAnimatedFood = quenched <= 0.0F && ticks % (thirst * 3 + 1) == 0;
      if (foodBarOffsets.size() != 10) {
         foodBarOffsets.setSize(10);
      }

      for(int i = 0; i < 10; ++i) {
         int x = right - i * 8 - 9;
         int y = top;
         if (shouldAnimatedFood) {
            y = top + (random.nextInt(3) - 1);
         }

         IntPoint point = (IntPoint)foodBarOffsets.get(i);
         if (point == null) {
            point = new IntPoint();
            foodBarOffsets.set(i, point);
         }

         point.x = x - right;
         point.y = y - top;
      }

   }
}
