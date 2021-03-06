package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.RSUtils;
import com.raoulvdberge.refinedstorage.api.network.IWirelessTransmitter;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerBasic;
import com.raoulvdberge.refinedstorage.inventory.ItemHandlerUpgrade;
import com.raoulvdberge.refinedstorage.item.ItemUpgrade;
import com.raoulvdberge.refinedstorage.tile.data.ITileDataProducer;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

public class TileWirelessTransmitter extends TileNode implements IWirelessTransmitter {
    public static final TileDataParameter<Integer> RANGE = new TileDataParameter<>(DataSerializers.VARINT, 0, new ITileDataProducer<Integer, TileWirelessTransmitter>() {
        @Override
        public Integer getValue(TileWirelessTransmitter tile) {
            return tile.getRange();
        }
    });

    private ItemHandlerUpgrade upgrades = new ItemHandlerUpgrade(4, this, ItemUpgrade.TYPE_RANGE);

    public TileWirelessTransmitter() {
        dataManager.addWatchedParameter(RANGE);
    }

    @Override
    public int getEnergyUsage() {
        return RS.INSTANCE.config.wirelessTransmitterUsage + upgrades.getEnergyUsage();
    }

    @Override
    public void updateNode() {
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        RSUtils.readItems(upgrades, 0, tag);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        RSUtils.writeItems(upgrades, 0, tag);

        return tag;
    }

    @Override
    public int getRange() {
        return RS.INSTANCE.config.wirelessTransmitterBaseRange + (upgrades.getUpgradeCount(ItemUpgrade.TYPE_RANGE) * RS.INSTANCE.config.wirelessTransmitterRangePerUpgrade);
    }

    @Override
    public BlockPos getOrigin() {
        return pos;
    }

    @Override
    public int getDimension() {
        return getWorld().provider.getDimension();
    }

    public ItemHandlerBasic getUpgrades() {
        return upgrades;
    }

    @Override
    public IItemHandler getDrops() {
        return upgrades;
    }

    @Override
    public boolean canConduct(@Nullable EnumFacing direction) {
        return direction != null && EnumFacing.DOWN.equals(direction);
    }

    @Override
    public boolean hasConnectivityState() {
        return true;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(upgrades);
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public void walkNeighborhood(Operator operator) {
        operator.apply(getWorld(), pos.offset(EnumFacing.DOWN), EnumFacing.UP);
    }
}
