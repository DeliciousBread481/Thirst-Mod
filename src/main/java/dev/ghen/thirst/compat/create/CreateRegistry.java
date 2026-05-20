package dev.ghen.thirst.compat.create;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.ModelGen;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.world.level.block.Block;

public class CreateRegistry {
   public static final NonNullSupplier<Registrate> REGISTRATE = NonNullSupplier.lazy(() -> Registrate.create("thirst"));
   public static final BlockEntry<SandFilterBlock> SAND_FILTER_BLOCK;
   public static final BlockEntityEntry<SandFilterTileEntity> SAND_FILTER_TE;

   public static void register() {
   }

   static {
      SAND_FILTER_BLOCK = ((BlockBuilder)((Registrate)REGISTRATE.get()).block("sand_filter", SandFilterBlock::new).initialProperties(SharedProperties::copperMetal).blockstate((ctx, prov) -> prov.simpleBlock((Block)ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov, new String[0]))).item(AssemblyOperatorBlockItem::new).transform(ModelGen.customItemModel())).register();
      SAND_FILTER_TE = ((Registrate)REGISTRATE.get()).blockEntity("sand_filter", SandFilterTileEntity::new).validBlocks(new NonNullSupplier[]{SAND_FILTER_BLOCK}).register();
   }
}
