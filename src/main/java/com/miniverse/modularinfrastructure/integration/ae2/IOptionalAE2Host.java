package com.miniverse.modularinfrastructure.integration.ae2;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.util.AECableType;
import net.minecraft.core.Direction;

import javax.annotation.Nullable;

/**
 * Interface for block entities that can optionally host AE2 grid nodes
 * This allows block entities to implement IInWorldGridNodeHost functionality
 * only when AE2 is present
 */
public interface IOptionalAE2Host extends IInWorldGridNodeHost {
    
    /**
     * Get the AE2 component if available
     */
    @Nullable
    IAE2DataConnector getAE2Component();
    
    @Override
    @Nullable
    default IGridNode getGridNode(Direction dir) {
        IAE2DataConnector ae2Component = getAE2Component();
        if (ae2Component instanceof AE2DataConnectorComponent component) {
            return component.getGridNode();
        }
        return null;
    }
    
    @Override
    @Nullable
    default AECableType getCableConnectionType(Direction dir) {
        IAE2DataConnector ae2Component = getAE2Component();
        if (ae2Component instanceof AE2DataConnectorComponent component) {
            return component.getCableConnectionType(dir);
        }
        return AECableType.NONE;
    }
}