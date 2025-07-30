// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.api.wires.localhandlers;

import com.miniverse.modularinfrastructure.api.wires.GlobalWireNetwork;
import com.miniverse.modularinfrastructure.api.wires.LocalWireNetwork;

public interface ILocalHandlerConstructor
{
	LocalNetworkHandler create(LocalWireNetwork local, GlobalWireNetwork global) throws Exception;
}