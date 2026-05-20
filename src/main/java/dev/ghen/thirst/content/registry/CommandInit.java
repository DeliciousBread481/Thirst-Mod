package dev.ghen.thirst.content.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import dev.ghen.thirst.foundation.network.ThirstModPacketHandler;
import dev.ghen.thirst.foundation.network.message.PlayerThirstSyncMessage;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber(
   modid = "thirst"
)
public class CommandInit {
   @SubscribeEvent
   public static void RegisterCommand(RegisterCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.m_82127_("thirst").requires((cs) -> cs.m_6761_(2))).then(Commands.m_82127_("query").then(Commands.m_82129_("Player", EntityArgument.m_91466_()).executes((context) -> {
         ServerPlayer player = EntityArgument.m_91474_(context, "Player");
         IThirst iThirst = (IThirst)player.getCapability(ModCapabilities.PLAYER_THIRST).orElse((Object)null);
         Object[] arg = new Object[2];
         arg[0] = iThirst.getThirst();
         arg[1] = iThirst.getQuenched();
         ((CommandSourceStack)context.getSource()).m_288197_(() -> MutableComponent.m_237204_(new TranslatableContents("command.thirst.query", "command.thirst.query", arg)), false);
         return 0;
      })))).then(Commands.m_82127_("set").then(Commands.m_82129_("Player", EntityArgument.m_91466_()).then(Commands.m_82129_("thirst", IntegerArgumentType.integer(0, 20)).then(Commands.m_82129_("quenched", IntegerArgumentType.integer(0, 20)).executes((context) -> {
         ServerPlayer player = EntityArgument.m_91474_(context, "Player");
         IThirst iThirst = (IThirst)player.getCapability(ModCapabilities.PLAYER_THIRST).orElse((Object)null);
         Object[] arg = new Object[2];
         arg[0] = IntegerArgumentType.getInteger(context, "thirst");
         arg[1] = IntegerArgumentType.getInteger(context, "quenched");
         iThirst.setThirst((Integer)arg[0]);
         iThirst.setQuenched((Integer)arg[1]);
         ((CommandSourceStack)context.getSource()).m_288197_(() -> MutableComponent.m_237204_(new TranslatableContents("command.thirst.set", "command.thirst.set", arg)), false);
         return 0;
      })))))).then(Commands.m_82127_("enable").then(Commands.m_82129_("Player", EntityArgument.m_91470_()).then(Commands.m_82129_("bool", BoolArgumentType.bool()).executes((context) -> {
         Collection<ServerPlayer> players = EntityArgument.m_91477_(context, "Player");
         boolean shouldTick = BoolArgumentType.getBool(context, "bool");
         Collection<Component> playersName = new ArrayList();

         for(ServerPlayer player : players) {
            IThirst thirstData = (IThirst)player.getCapability(ModCapabilities.PLAYER_THIRST).orElse((Object)null);
            thirstData.setShouldTickThirst(shouldTick);
            ThirstModPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PlayerThirstSyncMessage(shouldTick));
            playersName.add(player.m_7755_());
         }

         if (shouldTick) {
            ((CommandSourceStack)context.getSource()).m_288197_(() -> MutableComponent.m_237204_(new TranslatableContents("command.thirst.enable", "command.thirst.enable", playersName.toArray())), false);
         } else {
            ((CommandSourceStack)context.getSource()).m_288197_(() -> MutableComponent.m_237204_(new TranslatableContents("command.thirst.disable", "command.thirst.disable", playersName.toArray())), false);
         }

         return 0;
      })))));
   }
}
