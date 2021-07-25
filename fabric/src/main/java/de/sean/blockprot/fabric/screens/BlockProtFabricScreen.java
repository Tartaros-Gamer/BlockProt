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

package de.sean.blockprot.fabric.screens;

import de.sean.blockprot.fabric.translation.TranslationIdentifier;
import de.sean.blockprot.screens.BlockProtScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public abstract class BlockProtFabricScreen extends GenericContainerScreenHandler implements BlockProtScreen<ItemStack, TranslationIdentifier> {
    protected static final int DEFAULT_ROW_SIZE = 9;

    public BlockProtFabricScreen(ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, int rows) {
        super(type, syncId, playerInventory, new SimpleInventory(DEFAULT_ROW_SIZE * rows), rows);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackButton() {
        setBackButton(this.getRows() - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackButton(int index) {
        setItemStack(index, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setItemStack(int index, ItemStack item) {
        this.inventory.setStack(index, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setItemStack(int index, ItemStack item, TranslationIdentifier key) {
        item.setCustomName(key.asTranslatableText());
        this.inventory.setStack(index, item);
    }
}
