package refinedstorage.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import refinedstorage.RefinedStorage;
import refinedstorage.RefinedStorageGui;
import refinedstorage.tile.TileDrive;

public class BlockDrive extends BlockMachine
{
	public BlockDrive()
	{
		super("drive");
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileDrive();
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			player.openGui(RefinedStorage.INSTANCE, RefinedStorageGui.DRIVE, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}
}