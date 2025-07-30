// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.api.wires;

import com.miniverse.modularinfrastructure.api.Lib;
import com.miniverse.modularinfrastructure.api.utils.GatedLogger;
import org.apache.logging.log4j.LogManager;

public class WireLogger
{
	public static GatedLogger logger = new GatedLogger(LogManager.getLogger(Lib.MODID+"-wires"), false);
}