package com.example.examplemod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = ExampleMod.MODID, version = ExampleMod.VERSION)
public class ExampleMod
{
    public static final String MODID = "examplemod";
    public static final String VERSION = "1.0";

    public static Block blockTest;
    public static final String BLOCK_TEST_NAME = "block_test";
    public static final String TILEENTITY_TEST_NAME = "tile_test";

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        blockTest = new BlockTest().setUnlocalizedName(BLOCK_TEST_NAME);
        GameRegistry.registerBlock(blockTest, BLOCK_TEST_NAME);
        // Register tile entity as ID without MODID
        GameRegistry.registerTileEntity(TileEntityTest.class, TILEENTITY_TEST_NAME);

        if (event.getSide().isClient())
        {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(blockTest), 0, new ModelResourceLocation(MODID + ":" + BLOCK_TEST_NAME, "inventory"));
        }
    }

    /* Block */

    public static class BlockTest extends BlockContainer
    {
        public BlockTest()
        {
            super(Material.rock);
            this.setCreativeTab(CreativeTabs.tabDecorations);
        }

        @Override
        public TileEntity createNewTileEntity(World worldIn, int meta)
        {
            return new TileEntityTest();
        }

        @Override
        public int getRenderType()
        {
            return 3;
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

                if (!worldObj.isRemote)
                {
                    // Spawn heart from Server to Client
                    ((WorldServer)worldObj).spawnParticle(EnumParticleTypes.HEART, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 1, 0.0D, 0.0D, 0.0D, 0.0D, new int[0]);
                }
                else
                {
                    // Spawn spark in Client
                    worldObj.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, p.getX() + 0.5, p.getY() + 1, p.getZ() + 0.5, 0.0D, 0.2D, 0.0D, new int[0]);
                }

                tick = 0;
            }
        }

        @Override
        public void writeToNBT(NBTTagCompound compound)
        {
            super.writeToNBT(compound);
            compound.setString("ver", "1.8.9");
        }
    }
}
