package dev.ghen.thirst.api;

import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.Temperature.Type;
import dev.ghen.thirst.content.purity.ContainerWithPurity;
import dev.ghen.thirst.content.purity.WaterPurity;
import dev.ghen.thirst.foundation.common.event.ThirstEventFactory;
import dev.ghen.thirst.foundation.config.CommonConfig;
import dev.ghen.thirst.foundation.config.ContainerConfig;
import dev.ghen.thirst.foundation.config.ItemSettingsConfig;
import dev.ghen.thirst.foundation.config.KeyWordConfig;
import dev.ghen.thirst.foundation.util.ConfigHelper;
import dev.ghen.thirst.foundation.util.LoadedValue;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class ThirstHelper {
   private static boolean useColdSweatCaps = false;
   private static final float MODIFIER_HARSHNESS = 0.5F;
   public static Map<Item, Number[]> VALID_DRINKS = (Map)LoadedValue.of(() -> ConfigHelper.getItemsWithValues((List)ItemSettingsConfig.DRINKS.get())).get();
   public static Map<Item, Number[]> VALID_FOODS = (Map)LoadedValue.of(() -> ConfigHelper.getItemsWithValues((List)ItemSettingsConfig.FOODS.get())).get();
   public static List<Item> containers = (List)LoadedValue.<Item>of(() -> ConfigHelper.getItems((List)ContainerConfig.CONTAINERS.get())).get();
   public static String keywordBlackList;
   public static String keywordDrink;
   public static String keywordSoup;
   public static String keywordFruit;

   public static void init() {
      ThirstEventFactory.onRegisterThirstValue();

      for(Item item : containers) {
         if (!item.equals(Items.f_41852_)) {
            WaterPurity.addContainer(new ContainerWithPurity(new ItemStack(item)));
         }
      }

      VALID_DRINKS.forEach((itemx, numbers) -> {
         if (itemx.m_41473_() != null && !(Boolean)CommonConfig.ENABLE_DRINKS_NUTRITION.get()) {
            itemx.m_41473_().f_38723_ = 0;
         }

      });
   }

   public static boolean itemRestoresThirst(ItemStack itemStack) {
      return isDrink(itemStack) || isFood(itemStack) || checkKeywords(itemStack);
   }

   public static boolean isDrink(ItemStack itemStack) {
      return !((List)ItemSettingsConfig.ITEMS_BLACKLIST.get()).contains(itemStack.m_41720_().toString()) && VALID_DRINKS.containsKey(itemStack.m_41720_());
   }

   public static boolean isFood(ItemStack itemStack) {
      return !((List)ItemSettingsConfig.ITEMS_BLACKLIST.get()).contains(itemStack.m_41720_().toString()) && VALID_FOODS.containsKey(itemStack.m_41720_());
   }

   /** @deprecated */
   @Deprecated
   public static void addFood(Item item, int thirst, int quenched) {
   }

   /** @deprecated */
   @Deprecated
   public static void addDrink(Item item, int thirst, int quenched) {
   }

   public static int getThirst(ItemStack itemStack) {
      Item item = itemStack.m_41720_();
      return VALID_DRINKS.containsKey(item) ? ((Number[])VALID_DRINKS.get(item))[0].intValue() : ((Number[])VALID_FOODS.get(item))[0].intValue();
   }

   public static int getQuenched(ItemStack itemStack) {
      Item item = itemStack.m_41720_();
      return VALID_DRINKS.containsKey(item) ? ((Number[])VALID_DRINKS.get(item))[1].intValue() : ((Number[])VALID_FOODS.get(item))[1].intValue();
   }

   public static int getPurity(ItemStack item) {
      if (!WaterPurity.hasPurity(item)) {
         return (Integer)CommonConfig.DEFAULT_PURITY.get();
      } else {
         assert item.m_41783_() != null;

         return item.m_41783_().m_128451_("Purity");
      }
   }

   public static void shouldUseColdSweatCaps(boolean should) {
      useColdSweatCaps = should;
   }

   public static float getExhaustionFireProtModifier(Player player) {
      float perLevelMultiplier = 0.0625F;
      int totalLevels = EnchantmentHelper.m_44856_(player.m_6168_(), player.m_269291_().m_269549_()) / 2;
      if (totalLevels > 12) {
         totalLevels = 12;
      }

      return 1.0F - (float)totalLevels * 0.0625F * 0.75F;
   }

   public static float getExhaustionFireResistanceModifier(Player player) {
      return player.m_21023_(MobEffects.f_19607_) ? (float)(Integer)CommonConfig.FIRE_RESISTANCE_DEHYDRATION.get() / 100.0F : 1.0F;
   }

   public static float getExhaustionBiomeModifier(Player player) {
      BlockPos pos = player.m_20097_();
      Level level = player.m_9236_();
      if (level.m_6042_().f_63857_()) {
         return ((Double)CommonConfig.NETHER_THIRST_DEPLETION_MODIFIER.get()).floatValue();
      } else {
         Biome biome = (Biome)level.m_204166_(pos).m_203334_();
         float humidity = biome.getModifiedClimateSettings().f_47683_() + 0.6F;
         if ((double)humidity <= 0.6) {
            humidity = (float)((double)humidity + (double)0.5F);
         }

         float temp = biome.m_47554_() + 0.2F;
         if (useColdSweatCaps) {
            temp = (float)(Temperature.get(player, Type.BODY) / (double)100.0F);
         } else if (temp <= 0.0F) {
            temp = (float)Math.exp((double)temp);
         } else if (temp > 1.0F) {
            temp /= 2.0F;
         }

         float thirstModifier = ((Number)CommonConfig.THIRST_DEPLETION_MODIFIER.get()).floatValue() * (temp / humidity);
         if (thirstModifier < 1.0F) {
            float modifierOffset = 1.0F - thirstModifier;
            modifierOffset *= 0.5F;
            thirstModifier = 1.0F - modifierOffset;
         }

         return thirstModifier;
      }
   }

   private static boolean checkKeywords(ItemStack itemStack) {
      if (!(Boolean)KeyWordConfig.ENABLE_KEYWORD_CONFIG.get()) {
         return false;
      } else if (!itemStack.m_41614_()) {
         return false;
      } else {
         String pattern = keywordBlackList;
         Matcher matcher = Pattern.compile(pattern, 2).matcher(itemStack.m_41778_());
         if (matcher.find()) {
            return false;
         } else {
            pattern = keywordDrink;
            matcher = Pattern.compile(pattern, 2).matcher(itemStack.m_41778_());
            boolean hasWater = matcher.find();
            if (hasWater) {
               VALID_DRINKS.put(itemStack.m_41720_(), new Number[]{KeyWordConfig.getDrinkHydration(), KeyWordConfig.getDrinkQuenchness()});
               return true;
            } else {
               pattern = keywordSoup;
               matcher = Pattern.compile(pattern, 2).matcher(itemStack.m_41778_());
               hasWater = matcher.find();
               if (hasWater) {
                  VALID_FOODS.put(itemStack.m_41720_(), new Number[]{KeyWordConfig.getSoupHydration(), KeyWordConfig.getSoupQuenchness()});
                  return true;
               } else {
                  pattern = keywordFruit;
                  matcher = Pattern.compile(pattern, 2).matcher(itemStack.m_41778_());
                  hasWater = matcher.find();
                  if (hasWater) {
                     VALID_FOODS.put(itemStack.m_41720_(), new Number[]{KeyWordConfig.getFruitHydration(), KeyWordConfig.getFruitQuenchness()});
                  }

                  return hasWater;
               }
            }
         }
      }
   }

   static {
      keywordBlackList = (String)KeyWordConfig.KEYWORD_BLACKLIST.get();
      keywordDrink = (String)KeyWordConfig.KEYWORD_DRINK.get();
      keywordSoup = (String)KeyWordConfig.KEYWORD_SOUP.get();
      keywordFruit = (String)KeyWordConfig.KEYWORD_FRUIT.get();
   }
}
