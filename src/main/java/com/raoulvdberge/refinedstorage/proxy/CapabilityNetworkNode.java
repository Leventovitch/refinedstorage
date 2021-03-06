package com.raoulvdberge.refinedstorage.proxy;

import com.google.common.base.Preconditions;
import com.raoulvdberge.refinedstorage.api.network.INetworkMaster;
import com.raoulvdberge.refinedstorage.api.network.INetworkNode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

public class CapabilityNetworkNode {
    @CapabilityInject(INetworkNode.class)
    public static Capability<INetworkNode> NETWORK_NODE_CAPABILITY = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(INetworkNode.class, new Capability.IStorage<INetworkNode>() {
            public NBTBase writeNBT(Capability<INetworkNode> capability, INetworkNode instance, EnumFacing side) {
                return new NBTTagCompound();
            }

            public void readNBT(Capability<INetworkNode> capability, INetworkNode instance, EnumFacing side, NBTBase base) {
                // NO OP
            }
        }, () -> new INetworkNode() {
            private INetworkMaster network;

            @Override
            public int getEnergyUsage() {
                return 0;
            }

            @Nonnull
            @Override
            public ItemStack getItemStack() {
                return ItemStack.EMPTY;
            }

            @Override
            public void onConnected(INetworkMaster network) {
                this.network = network;
            }

            @Override
            public void onDisconnected(INetworkMaster network) {
            }

            @Override
            public boolean canUpdate() {
                return false;
            }

            @Override
            public INetworkMaster getNetwork() {
                return network;
            }
        });

        Preconditions.checkNotNull(NETWORK_NODE_CAPABILITY, "Capability not registered");
    }
}
