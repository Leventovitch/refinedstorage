package refinedstorage.tile;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import refinedstorage.RefinedStorage;
import refinedstorage.inventory.InventorySimple;
import refinedstorage.network.MessagePriorityUpdate;
import refinedstorage.storage.CellStorage;
import refinedstorage.storage.IStorage;
import refinedstorage.storage.IStorageGui;
import refinedstorage.storage.IStorageProvider;
import refinedstorage.storage.NBTStorage;
import refinedstorage.tile.settings.ICompareSetting;
import refinedstorage.tile.settings.IModeSetting;
import refinedstorage.tile.settings.IRedstoneModeSetting;
import refinedstorage.util.InventoryUtils;

public class TileDrive extends TileMachine implements IStorageProvider, IStorageGui, ICompareSetting, IModeSetting, IInventory
{
	public static final String NBT_PRIORITY = "Priority";
	public static final String NBT_COMPARE = "Compare";
	public static final String NBT_MODE = "Mode";

	private InventorySimple filterInventory = new InventorySimple("filters", 9, this);
	private InventorySimple inventory = new InventorySimple("drive", 8, this);

	private int priority = 0;
	private int compare = 0;
	private int mode = 0;

	@Override
	public int getEnergyUsage()
	{
		int base = 5;

		for (int i = 0; i < getSizeInventory(); ++i)
		{
			if (getStackInSlot(i) != null)
			{
				base += 2;
			}
		}

		return base;
	}

	@Override
	public void updateMachine()
	{
	}

	@Override
	public void addStorages(List<IStorage> storages)
	{
		for (int i = 0; i < getSizeInventory(); ++i)
		{
			if (getStackInSlot(i) != null)
			{
				storages.add(new CellStorage(getStackInSlot(i), this));
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		InventoryUtils.restoreInventory(filterInventory, 0, nbt);
		InventoryUtils.restoreInventory(inventory, 1, nbt);

		if (nbt.hasKey(NBT_PRIORITY))
		{
			priority = nbt.getInteger(NBT_PRIORITY);
		}

		if (nbt.hasKey(NBT_COMPARE))
		{
			compare = nbt.getInteger(NBT_COMPARE);
		}

		if (nbt.hasKey(NBT_MODE))
		{
			mode = nbt.getInteger(NBT_MODE);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		InventoryUtils.saveInventory(filterInventory, 0, nbt);
		InventoryUtils.saveInventory(inventory, 1, nbt);

		nbt.setInteger(NBT_PRIORITY, priority);
		nbt.setInteger(NBT_COMPARE, compare);
		nbt.setInteger(NBT_MODE, mode);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		super.toBytes(buf);

		buf.writeInt(priority);
		buf.writeInt(compare);
		buf.writeInt(mode);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		super.fromBytes(buf);

		priority = buf.readInt();
		compare = buf.readInt();
		mode = buf.readInt();
	}

	@Override
	public int getCompare()
	{
		return compare;
	}

	@Override
	public void setCompare(int compare)
	{
		markDirty();

		this.compare = compare;
	}

	@Override
	public boolean isWhitelist()
	{
		return mode == 0;
	}

	@Override
	public boolean isBlacklist()
	{
		return mode == 1;
	}

	@Override
	public void setToWhitelist()
	{
		markDirty();

		this.mode = 0;
	}

	@Override
	public void setToBlacklist()
	{
		markDirty();

		this.mode = 1;
	}

	@Override
	public String getName()
	{
		return "block.refinedstorage:drive.name";
	}

	@Override
	public IInventory getInventory()
	{
		return filterInventory;
	}

	@Override
	public IRedstoneModeSetting getRedstoneModeSetting()
	{
		return this;
	}

	@Override
	public ICompareSetting getCompareSetting()
	{
		return this;
	}

	@Override
	public IModeSetting getModeSetting()
	{
		return this;
	}

	@Override
	public void onPriorityChanged(int priority)
	{
		RefinedStorage.NETWORK.sendToServer(new MessagePriorityUpdate(pos, priority));
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		markDirty();

		this.priority = priority;
	}

	@Override
	public int getStored()
	{
		int stored = 0;

		for (int i = 0; i < getSizeInventory(); ++i)
		{
			ItemStack stack = getStackInSlot(i);

			if (stack != null)
			{
				stored += NBTStorage.getStored(stack.getTagCompound());
			}
		}

		return stored;
	}

	@Override
	public int getCapacity()
	{
		int capacity = 0;

		for (int i = 0; i < getSizeInventory(); ++i)
		{
			ItemStack stack = getStackInSlot(i);

			if (stack != null)
			{
				int cellCapacity = CellStorage.getCapacity(stack);

				if (cellCapacity == -1)
				{
					return -1;
				}

				capacity += cellCapacity;
			}
		}

		return capacity;
	}

	@Override
	public int getSizeInventory()
	{
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return inventory.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int count)
	{
		return inventory.decrStackSize(slot, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int slot)
	{
		return inventory.removeStackFromSlot(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		inventory.setInventorySlotContents(slot, stack);
	}

	@Override
	public int getInventoryStackLimit()
	{
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public void openInventory(EntityPlayer player)
	{
		inventory.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player)
	{
		inventory.closeInventory(player);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return inventory.isItemValidForSlot(slot, stack);
	}

	@Override
	public int getField(int id)
	{
		return inventory.getField(id);
	}

	@Override
	public void setField(int id, int value)
	{
		inventory.setField(id, value);
	}

	@Override
	public int getFieldCount()
	{
		return inventory.getFieldCount();
	}

	@Override
	public void clear()
	{
		inventory.clear();
	}

	@Override
	public boolean hasCustomName()
	{
		return inventory.hasCustomName();
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return inventory.getDisplayName();
	}

	@Override
	public IInventory getDroppedInventory()
	{
		return inventory;
	}
}