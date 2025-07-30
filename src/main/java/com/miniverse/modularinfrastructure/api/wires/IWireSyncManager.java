// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.api.wires;

public interface IWireSyncManager
{
	void onConnectionAdded(Connection c);

	void onConnectionRemoved(Connection c);

	void onConnectionEndpointsChanged(Connection c);
}