package dev.ghen.thirst.foundation.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class TickHelper {
   private static final Map<Integer, List<Runnable>> tickTasks = new HashMap();
   private static int tickTimerFsr = 0;

   public static void addTask(int tick, Runnable task) {
      if (!tickTasks.containsKey(tick)) {
         tickTasks.put(tick, new ArrayList());
      }

      ((List)tickTasks.get(tick)).add(task);
   }

   public static void nextTick(Level level, Runnable task) {
      addTask(((MinecraftServer)Objects.requireNonNull(level.m_7654_())).m_129921_() + 1, task);
   }

   public static void TickLater(Level level, int tickNumber, Runnable task) {
      addTask(((MinecraftServer)Objects.requireNonNull(level.m_7654_())).m_129921_() + tickNumber, task);
   }

   @SubscribeEvent
   static void runTasks(TickEvent.LevelTickEvent event) {
      if (event.level instanceof ServerLevel && tickTimerFsr == 0 && tickTasks.containsKey(event.level.m_7654_().m_129921_())) {
         ((List)tickTasks.get(event.level.m_7654_().m_129921_())).forEach(Runnable::run);
         tickTasks.remove(event.level.m_7654_().m_129921_());
         tickTimerFsr += 3;
      } else if (tickTimerFsr > 0) {
         --tickTimerFsr;
      }

   }
}
