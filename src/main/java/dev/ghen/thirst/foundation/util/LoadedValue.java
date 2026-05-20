package dev.ghen.thirst.foundation.util;

import java.util.function.Supplier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LoadedValue<T> {
   T value;
   Supplier<T> valueCreator;

   public LoadedValue(Supplier<T> valueCreator) {
      this.valueCreator = valueCreator;
      this.value = (T)valueCreator.get();
      MinecraftForge.EVENT_BUS.register(this);
   }

   public static <V> LoadedValue<V> of(Supplier<V> valueCreator) {
      return new LoadedValue<V>(valueCreator);
   }

   @SubscribeEvent
   public void onLoaded(ServerStartedEvent event) {
      this.value = (T)this.valueCreator.get();
   }

   public T get() {
      return this.value;
   }
}
