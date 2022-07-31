package flaxbeard.thaumicexploration.block;

import flaxbeard.thaumicexploration.tile.TileEntityAutoCrafter;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockAutoCrafter extends BlockContainer {

    public BlockAutoCrafter() {
        super(Material.iron);
        // TODO Auto-generated constructor stub
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        // TODO Auto-generated method stub
        return new TileEntityAutoCrafter();
    }
}
