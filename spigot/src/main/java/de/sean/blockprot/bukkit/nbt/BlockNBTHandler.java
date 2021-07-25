/*
 * Copyright (C) 2021 spnda
 * This file is part of BlockProt <https://github.com/spnda/BlockProt>.
 *
 * BlockProt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BlockProt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with BlockProt.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.sean.blockprot.bukkit.nbt;

import de.sean.blockprot.nbt.FriendModifyAction;
import de.sean.blockprot.nbt.LockReturnValue;
import de.sean.blockprot.util.BlockProtUtil;
import de.sean.blockprot.bukkit.BlockProt;
import de.sean.blockprot.bukkit.util.BlockUtil;
import de.sean.blockprot.nbt.FriendModifyAction;
import de.sean.blockprot.nbt.IBlockNBTHandler;
import de.sean.blockprot.nbt.IFriendHandler;
import de.sean.blockprot.nbt.LockReturnValue;
import de.sean.blockprot.util.BlockProtUtil;
import de.tr7zw.changeme.nbtapi.NBTBlock;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * A block handler to get values and settings from a single lockable
 * block.
 *
 * @since 0.2.3
 */
public final class BlockNBTHandler extends IBlockNBTHandler<NBTCompound, Player, FriendHandler> {
    public static final String PERMISSION_LOCK = "blockprot.lock";

    public static final String PERMISSION_INFO = "blockprot.info";

    public static final String PERMISSION_ADMIN = "blockprot.admin";

    public static final String PERMISSION_BYPASS = "blockprot.bypass";

    private static final boolean DEFAULT_REDSTONE = true;

    /**
     * The backing block this handler handles.
     *
     * @since 0.2.3
     */
    public final Block block;

    /**
     * Create a new handler for given {@code block}.
     *
     * @param block The block we want to use and get the
     *              NBT container for.
     * @throws RuntimeException if {@code block} is not a lockable block
     *                          or lockable tile entity.
     * @since 0.2.3
     */
    public BlockNBTHandler(@NotNull final Block block) throws RuntimeException {
        super();
        this.block = block;

        if (BlockProt.getDefaultConfig().isLockableBlock(this.block.getType())) {
            container = new NBTBlock(block).getData();
        } else if (BlockProt.getDefaultConfig().isLockableTileEntity(this.block.getType())) {
            container = new NBTTileEntity(block.getState()).getPersistentDataContainer();
        } else {
            throw new RuntimeException("Given block " + block.getType() + " is not a lockable block/tile entity");
        }
    }

    /**
     * Creates a new handler without a block. This should only be used
     * for temporary internal operations.
     *
     * @param compound The compound value we should use.
     * @since 0.3.0
     */
    private BlockNBTHandler(@NotNull final NBTCompound compound) {
        super();
        this.container = compound;
        this.block = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getOwner() {
        if (!container.hasKey(OWNER_ATTRIBUTE)) return "";
        else return container.getString(OWNER_ATTRIBUTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOwner(@NotNull final String owner) {
        container.setString(OWNER_ATTRIBUTE, owner);
    }

    /**
     * As of 0.3.0 we expect a list of compounds, in which we can
     * store the access flags and other future settings.
     * Therefore we will remap the values here. This will possibly
     * be removed in a future version.
     *
     * @since 0.3.0
     */
    private void remapAccess() {
        final List<String> stringList = BlockProtUtil.parseStringList(container.getString(OLD_LOCK_ATTRIBUTE));
        if (stringList.isEmpty()) return;
        container.removeKey(OLD_LOCK_ATTRIBUTE); // Remove the original list.
        container.addCompound(LOCK_ATTRIBUTE); // Create the new compound.
        stringList.forEach(this::addFriend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Stream<FriendHandler> getFriendsStream() {
        remapAccess();
        if (!container.hasKey(LOCK_ATTRIBUTE)) return Stream.empty();

        final NBTCompound compound = container.getOrCreateCompound(LOCK_ATTRIBUTE);
        return compound
            .getKeys()
            .stream()
            .map((k) -> new FriendHandler(compound.getCompound(k)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFriends(@NotNull final List<FriendHandler> access) {
        container.removeKey(LOCK_ATTRIBUTE);
        if (!access.isEmpty()) {
            NBTCompound compound = container.addCompound(LOCK_ATTRIBUTE);
            for (IFriendHandler<?, ?> handler : access) {
                NBTCompound newCompound = compound.addCompound(handler.getName());
                newCompound.mergeCompound((NBTCompound) handler.getContainer());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFriend(@NotNull final String friend) {
        NBTCompound compound = container.getOrCreateCompound(LOCK_ATTRIBUTE);
        compound.addCompound(friend).setString("id", friend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFriend(@NotNull final String friend) {
        NBTCompound compound = container.getOrCreateCompound(LOCK_ATTRIBUTE);
        compound.removeKey(friend);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRedstone() {
        // We will default to 'true'. The default value for a boolean is 'false',
        // which would also be the default value for NBTCompound#getBoolean
        if (!container.hasKey(REDSTONE_ATTRIBUTE)) {
            container.setBoolean(REDSTONE_ATTRIBUTE, DEFAULT_REDSTONE);
            return DEFAULT_REDSTONE;
        }
        return container.getBoolean(REDSTONE_ATTRIBUTE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRedstone(final boolean redstone) {
        container.setBoolean(REDSTONE_ATTRIBUTE, redstone);
    }

    /**
     * Locks this block for given {@code player} as the owner.
     *
     * @param player      The player to set as an owner.
     * @param doubleChest A double chest we want to also lock. This parameter is optional
     *                    and can be null.
     * @return A {@link LockReturnValue} whether or not the block was successfully locked,
     * else there might have been issues with permissions.
     * @since 0.2.3
     * @deprecated Use {@link #lockBlock(Player)} instead.
     */
    @Deprecated
    @NotNull
    public LockReturnValue lockBlock(@NotNull final Player player, @Nullable final NBTTileEntity doubleChest) {
        return lockBlock(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public LockReturnValue lockBlock(@NotNull final Player player) {
        String owner = getOwner();
        final String playerUuid = player.getUniqueId().toString();

        if (owner.isEmpty()) {
            // This block is not owned by anyone, this user can claim this block
            owner = playerUuid;
            setOwner(owner);
            this.applyToOtherContainer();
            return new LockReturnValue(true);
        } else if (owner.equals(playerUuid) || player.isOp() || player.hasPermission(PERMISSION_ADMIN)) {
            this.clear();
            this.applyToOtherContainer();
            return new LockReturnValue(true);
        }
        return new LockReturnValue(false);
    }

    /**
     * Locks redstone for this block.
     *
     * @param player      The player requesting this command, should be the owner.
     * @param doubleChest A double chest we also want to apply this to. This
     *                    parameter is optional and can be null.
     * @param value       The value we want to set it to. If null, we just flip
     *                    the current value.
     * @return A {@link LockReturnValue} whether or not the redstone was switched
     * successfully.
     * @since 0.2.3
     * @deprecated Use {@link #lockRedstoneForBlock(String, Boolean)} instead.
     */
    @Deprecated
    @NotNull
    public LockReturnValue lockRedstoneForBlock(@NotNull final String player, @Nullable final NBTTileEntity doubleChest, @Nullable final Boolean value) {
        return lockRedstoneForBlock(player, value);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    public @NotNull LockReturnValue lockRedstoneForBlock(@NotNull final String player, @Nullable final Boolean value) {
        if (isOwner(player)) {
            boolean redstone = value == null ? !getRedstone() : value;
            setRedstone(redstone);
            this.applyToOtherContainer();
            return new LockReturnValue(true);
        }
        return new LockReturnValue(false);
    }

    /**
     * @param player      The player requesting this command, should be the owner.
     * @param friend      The friend do to {@code action} with.
     * @param action      The action we should perform with {@code friend} on this block.
     * @param doubleChest A double chest we also want to apply this to. This
     *                    parameter is optional and can be null. This parameter will be ignored
     *                    due to deprecation.
     * @return A {@link LockReturnValue} whether or not the friends were modified
     * successfully.
     * @since 0.2.3
     * @deprecated Use {@link #modifyFriends(String, String, FriendModifyAction)} instead.
     */
    @Deprecated
    @NotNull
    public LockReturnValue modifyFriends(@NotNull final String player, @NotNull final String friend, @NotNull final FriendModifyAction action, @Nullable final NBTTileEntity doubleChest) {
        return modifyFriends(player, friend, action);
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public LockReturnValue modifyFriends(@NotNull final String player, @NotNull final String friend, @NotNull final FriendModifyAction action) {
        // This theoretically shouldn't happen, though we will still check for it just to be sure
        if (!isOwner(player)) return new LockReturnValue(
            false
        );

        final List<FriendHandler> friends = getFriends();
        switch (action) {
            case ADD_FRIEND: {
                if (containsFriend(friends, friend)) {
                    return new LockReturnValue(false);
                } else {
                    addFriend(friend);
                    this.applyToOtherContainer();
                    return new LockReturnValue(true);
                }
            }
            case REMOVE_FRIEND: {
                if (containsFriend(friends, friend)) {
                    removeFriend(friend);
                    this.applyToOtherContainer();
                    return new LockReturnValue(true);
                } else {
                    return new LockReturnValue(false);
                }
            }
            default: {
                Bukkit.getLogger().warning("modifyFriends: Unknown error occured.");
                return new LockReturnValue(false);
            }
        }
    }

    /**
     * Merges this handler with the handler of the other half of given
     * {@code block}, if that is a door. Will fail silently if the given
     * block is not a door.
     *
     * @param block The original door block, can be the bottom or top half.
     * @since 0.2.3
     * @deprecated Use {@link #applyToOtherContainer()} instead.
     */
    @Deprecated
    public void applyToDoor(@NotNull final Block block) {
        if (BlockProt.getDefaultConfig().isLockableDoor(block.getType())) {
            final Block otherDoor = BlockUtil.getOtherDoorHalf(block.getState());
            if (otherDoor == null) return;
            final BlockNBTHandler otherDoorHandler = new BlockNBTHandler(otherDoor);
            otherDoorHandler.mergeHandler(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyToOtherContainer() {
        if (BlockProt.getDefaultConfig().isLockableDoor(block.getType())) {
            final Block otherDoor = BlockUtil.getOtherDoorHalf(block.getState());
            if (otherDoor == null) return;
            final BlockNBTHandler otherDoorHandler = new BlockNBTHandler(otherDoor);
            otherDoorHandler.mergeHandler(this);
        } else {
            final BlockState doubleChestState = BlockUtil.getDoubleChest(this.block);
            if (doubleChestState != null) {
                final BlockNBTHandler doubleChestHandler = new BlockNBTHandler(doubleChestState.getBlock());
                doubleChestHandler.mergeHandler(this);
            }
        }
    }

    @Override
    public @NotNull String getName() {
        String name = container.getName();
        return name == null ? "" : name;
    }
}
