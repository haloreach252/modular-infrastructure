package com.miniverse.modularinfrastructure.datagen;

import com.miniverse.modularinfrastructure.ModularInfrastructure;
import com.miniverse.modularinfrastructure.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModularInfrastructure.MODID, existingFileHelper);
    }
    
    @Override
    protected void registerModels() {
        // Tools
        simpleItem(ModItems.POST_CONFIGURATOR);
        simpleItem(ModItems.WIRE_CONNECTOR);
        simpleItem(ModItems.WIRE_CUTTERS);
    }
    
    private void simpleItem(DeferredItem<? extends Item> item) {
        withExistingParent(item.getId().getPath(), 
            ResourceLocation.parse("item/generated"))
            .texture("layer0", modLoc("item/" + item.getId().getPath()));
    }
}