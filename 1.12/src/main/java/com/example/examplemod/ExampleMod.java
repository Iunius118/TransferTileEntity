package com.example.examplemod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION)
@EventBusSubscriber
public class ExampleMod
{
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";

    public static final String BLOCK_TEST_NAME = "block_test";
    public static final String TILEENTITY_TEST_NAME = "tile_test";

    @ObjectHolder(BLOCK_TEST_NAME)
    public static final Block BLOCK_TEST = null;

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().registerAll(new BlockTest().setRegistryName(MODID, BLOCK_TEST_NAME).setUnlocalizedName(BLOCK_TEST_NAME));
        registerTileEntityWithAlternative(TileEntityTest.class, TileEntityTestFixer.class, TILEENTITY_TEST_NAME);
    }

    public static void registerTileEntityWithAlternative(Class<? extends TileEntity> tileEntityClass, Class<? extends TileEntity> alternativeClass, String key)
    {
        GameRegistry.registerTileEntity(tileEntityClass, MODID + ":" + key);
        GameRegistry.registerTileEntity(alternativeClass, key);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        event.getRegistry().register(new ItemBlock(BLOCK_TEST).setRegistryName(BLOCK_TEST.getRegistryName()));
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BLOCK_TEST), 0, new ModelResourceLocation(BLOCK_TEST.getRegistryName(), "inventory"));
    }

    /* Block */

    public static class BlockTest extends BlockContainer
    {
        public BlockTest()
        {
            super(Material.ROCK);
            this.setCreativeTab(CreativeTabs.DECORATIONS);
        }

        @Override
        public TileEntity createNewTileEntity(World worldIn, int meta)
        {
            return new TileEntityTest();
        }

        @Override
        public EnumBlockRenderType getRenderType(IBlockState state)
        {
            return EnumBlockRenderType.MODEL;
        }
    }

    /* TileEntity */

    public static class TileEntityTest extends TileEntity implements ITickable
    {
        int tick = 0;

        @Override
        public void update()
        {
            if (++tick >= 20)
            {
                BlockPos p = this.getPos();

                if (!world.isRemote)
                {
                    // Spawn heart from Server to Client
                    ((WorldServer)world).spawnParticle(EnumParticleTypes.HEART, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 1, 0.0D, 0.0D, 0.0D, 0.0D, new int[0]);
                }
                else
                {
                    // Spawn spark in Client
                    world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 0.0D, 0.2D, 0.0D, new int[0]);
                }

                tick = 0;
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            super.readFromNBT(compound);

            if (compound.hasKey("ver", NBT.TAG_STRING))
            {
                System.out.println("Read from NBT [" + compound.getString("id") + "], ver: " + compound.getString("ver"));
            }
        }
    }

    public static class TileEntityTestFixer extends TileEntity
    {
        private NBTTagCompound m_fixedNBT;

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            super.readFromNBT(compound);

            // Copy NBT and Fix ID for genuine tile entity
            m_fixedNBT = compound.copy();
            String oldID = new ResourceLocation(m_fixedNBT.getString("id")).getResourcePath();
            if (!oldID.isEmpty())
            {
                String newID = new ResourceLocation(MODID, oldID).toString();
                m_fixedNBT.setString("id", newID);

                System.out.println("Replace (ID in NBT) [" + compound.getString("id") + "] with [" + m_fixedNBT.getString("id") + "]");
            }
        }

        @Override
        public void onLoad()
        {
            // Create genuine tile entity and Replace `this` with it
            TileEntity fixedTile = new TileEntityTest();
            fixedTile.readFromNBT(m_fixedNBT);
            world.setTileEntity(this.getPos(), fixedTile);

            System.out.println("Replace (tile entity) [" + this.getClass().getName() + "] with [" + fixedTile.getClass().getName() + "] at " + fixedTile.getPos());
        }
    }
}
