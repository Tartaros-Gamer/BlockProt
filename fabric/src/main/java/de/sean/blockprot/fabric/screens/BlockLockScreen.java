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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.TranslatableText;

public class BlockLockScreen extends BlockProtFabricScreen {
    private static final int ROWS = 1;
    private static final TranslationIdentifier SCREEN_NAME = TranslationIdentifier.SCREEN_LOCK;

    public BlockLockScreen(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X1, syncId, playerInventory, ROWS);

        this.setItemStack(0, new ItemStack(Items.CHEST));
        this.setItemStack(1, new ItemStack(Items.REDSTONE));
        this.setItemStack(8, new ItemStack(Items.BLACK_STAINED_GLASS_PANE));
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.getInventory().canPlayerUse(player);
    }

    @Override
    public int getRows() {
        return ROWS;
    }

    @Override
    public String getDefaultScreenName() {
        return null;
    }

    public static TranslatableText getScreenName() {
        return SCREEN_NAME.asTranslatableText();
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.PICKUP_ALL) {

        } else {
            // ????????????
        }
    }
}
