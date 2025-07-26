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
        postConfiguratorWithModes();
        simpleItem(ModItems.WIRE_CONNECTOR);
        simpleItem(ModItems.WIRE_CUTTERS);
    }
    
    private void postConfiguratorWithModes() {
        // Create the individual mode models first
        withExistingParent("post_configurator_copy", ResourceLocation.parse("item/generated"))
            .texture("layer0", modLoc("item/post_configurator_copy"));
        withExistingParent("post_configurator_paste", ResourceLocation.parse("item/generated"))
            .texture("layer0", modLoc("item/post_configurator_paste"));
        withExistingParent("post_configurator_batch", ResourceLocation.parse("item/generated"))
            .texture("layer0", modLoc("item/post_configurator_batch"));
        
        // Create the main model with overrides
        withExistingParent(ModItems.POST_CONFIGURATOR.getId().getPath(), 
            ResourceLocation.parse("item/generated"))
            .texture("layer0", modLoc("item/post_configurator_copy"))
            .override()
                .predicate(modLoc("mode"), 0.0f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/post_configurator_copy")))
                .end()
            .override()
                .predicate(modLoc("mode"), 1.0f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/post_configurator_paste")))
                .end()
            .override()
                .predicate(modLoc("mode"), 2.0f)
                .model(new ModelFile.UncheckedModelFile(modLoc("item/post_configurator_batch")))
                .end();
    }
    
    private void simpleItem(DeferredItem<? extends Item> item) {
        withExistingParent(item.getId().getPath(), 
            ResourceLocation.parse("item/generated"))
            .texture("layer0", modLoc("item/" + item.getId().getPath()));
    }
}