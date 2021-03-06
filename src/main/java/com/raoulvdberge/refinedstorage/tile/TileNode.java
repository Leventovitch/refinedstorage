package com.raoulvdberge.refinedstorage.tile;

import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNeighborhoodAware;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import com.raoulvdberge.refinedstorage.api.util.IWrenchable;
import com.raoulvdberge.refinedstorage.proxy.CapabilityNetworkNode;
import com.raoulvdberge.refinedstorage.tile.config.IRedstoneConfigurable;
import com.raoulvdberge.refinedstorage.tile.config.RedstoneMode;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileNode extends TileBase implements INetworkNode, IRedstoneConfigurable, IWrenchable, INetworkNeighborhoodAware {
    public static final TileDataParameter<Integer> REDSTONE_MODE = RedstoneMode.createParameter();

    private static final String NBT_ACTIVE = "Active";

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    private boolean active;
    protected boolean rebuildOnUpdateChange;

    protected INetworkMaster network;

    public TileNode() {
        dataManager.addWatchedParameter(REDSTONE_MODE);
    }

    @Override
    public boolean canUpdate() {
        return redstoneMode.isEnabled(getWorld(), pos);
    }

    public boolean isActive() {
        return active;
    }

    public abstract void updateNode();

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            boolean wasActive = active;

            active = hasNetwork() && canUpdate();

            if (active != wasActive) {
                if (hasConnectivityState()) {
                    updateBlock();
                }

                if (network != null) {
                    onConnectionChange(network, active);

                    if (rebuildOnUpdateChange) {
                        network.getNodeGraph().rebuild();
                    }
                }
            }

            if (active) {
                updateNode();
            }
        }

        super.update();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (getWorld() != null && !getWorld().isRemote && hasNetwork()) {
            network.getNodeGraph().rebuild();
        }
    }

    @Override
    public void onConnected(INetworkMaster network) {
        this.network = network;

        onConnectionChange(network, true);

        markDirty();
    }

    @Override
    public void onDisconnected(INetworkMaster network) {
        onConnectionChange(network, false);

        this.network = null;

        markDirty();
    }

    public void onConnectionChange(INetworkMaster network, boolean state) {
        // NO OP
    }

    public boolean canConduct(@Nullable EnumFacing direction) {
        return true;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityNetworkNode.NETWORK_NODE_CAPABILITY) {
            return true;
        }

        return super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityNetworkNode.NETWORK_NODE_CAPABILITY) {
            return CapabilityNetworkNode.NETWORK_NODE_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }

    @Override
    @Nullable
    public INetworkMaster getNetwork() {
        return network;
    }

    public boolean hasNetwork() {
        return network != null;
    }

    @Nonnull
    @Override
    public ItemStack getItemStack() {
        IBlockState state = getWorld().getBlockState(pos);
        Item item = Item.getItemFromBlock(state.getBlock());
        return new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    @Override
    public void setRedstoneMode(RedstoneMode mode) {
        this.redstoneMode = mode;

        markDirty();
    }

    @Override
    public void read(NBTTagCompound tag) {
        super.read(tag);

        readConfiguration(tag);
    }

    @Override
    public NBTTagCompound write(NBTTagCompound tag) {
        super.write(tag);

        writeConfiguration(tag);

        return tag;
    }

    @Override
    public NBTTagCompound writeConfiguration(NBTTagCompound tag) {
        redstoneMode.write(tag);

        return tag;
    }

    @Override
    public void readConfiguration(NBTTagCompound tag) {
        redstoneMode = RedstoneMode.read(tag);
    }

    public NBTTagCompound writeUpdate(NBTTagCompound tag) {
        super.writeUpdate(tag);

        if (hasConnectivityState()) {
            tag.setBoolean(NBT_ACTIVE, active);
        }

        return tag;
    }

    public void readUpdate(NBTTagCompound tag) {
        if (hasConnectivityState()) {
            active = tag.getBoolean(NBT_ACTIVE);
        }

        super.readUpdate(tag);
    }

    public boolean hasConnectivityState() {
        return false;
    }

    @Override
    public void walkNeighborhood(Operator operator) {
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (canConduct(facing)) {
                operator.apply(getWorld(), pos.offset(facing), facing.getOpposite());
            }
        }
    }
}
