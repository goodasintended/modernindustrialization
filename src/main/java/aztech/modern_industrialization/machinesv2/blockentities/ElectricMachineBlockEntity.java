/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.components.EnergyComponent;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.RecipeEfficiencyBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;

public class ElectricMachineBlockEntity extends MachineBlockEntity implements CrafterComponent.Behavior, Tickable {
    public ElectricMachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, EnergyBar.Parameters energyBarParams, ProgressBar.Parameters progressBarParams,
            RecipeEfficiencyBar.Parameters efficiencyBarParams, MachineTier tier, long euCapacity) {
        super(type, guiParams);
        this.inventory = inventory;
        this.crafter = new CrafterComponent(inventory, this);
        this.energy = new EnergyComponent(euCapacity);
        this.type = recipeType;
        this.tier = tier;
        this.insertable = energy.buildInsertable(cableTier -> cableTier == CableTier.LV);
        registerClientComponent(new ProgressBar.Server(progressBarParams, crafter::getProgress));
        registerClientComponent(new EnergyBar.Server(energyBarParams, energy::getEu, energy::getCapacity));
        registerClientComponent(new RecipeEfficiencyBar.Server(efficiencyBarParams, crafter));
    }

    private final MachineInventoryComponent inventory;
    private final CrafterComponent crafter;
    private final EnergyComponent energy;

    private final MachineRecipeType type;
    private final MachineTier tier;

    private final EnergyInsertable insertable;

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return energy.consumeEu(max, simulation);
    }

    @Override
    public MachineRecipeType recipeType() {
        return type;
    }

    @Override
    public long getBaseRecipeEu() {
        return tier.getBaseEu();
    }

    @Override
    public long getMaxRecipeEu() {
        return tier.getMaxEu();
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            crafter.tickRecipe();
            markDirty();
            // FIXME auto-extract
        }
    }

    @Override
    public MIInventory getInventory() {
        return inventory.inventory;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Override
    protected MachineModelClientData getModelData() {
        return new MachineModelClientData(MachineCasings.LV, Direction.NORTH);
    }

    public static void registerEnergyApi(BlockEntityType<?> bet) {
        EnergyApi.MOVEABLE.registerForBlockEntities((be, direction) -> ((ElectricMachineBlockEntity) be).insertable, bet);
    }
}