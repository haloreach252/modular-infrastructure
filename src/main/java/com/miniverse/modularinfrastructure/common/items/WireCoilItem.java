// Originally from Immersive Engineering, adapted under its license
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.miniverse.modularinfrastructure.common.items;

import com.miniverse.modularinfrastructure.api.IEApiDataComponents;
import com.miniverse.modularinfrastructure.api.Lib;
import com.miniverse.modularinfrastructure.api.TargetingInfo;
import com.miniverse.modularinfrastructure.api.wires.*;
import com.miniverse.modularinfrastructure.api.wires.utils.WireLink;
import com.miniverse.modularinfrastructure.api.wires.utils.WirecoilUtils;
import com.miniverse.modularinfrastructure.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.*;

import static com.miniverse.modularinfrastructure.api.wires.utils.WireUtils.findObstructingBlocks;

public class WireCoilItem extends Item implements IWireCoil
{
	@Nonnull
	private final WireType type;

	public WireCoilItem(@Nonnull WireType type)
	{
		super(new Properties());
		this.type = type;
	}

	@Override
	public WireType getWireType(ItemStack stack)
	{
		return type;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		if(WireType.REDSTONE_CATEGORY.equals(type.getCategory()))
		{
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.redstone").withStyle(ChatFormatting.GRAY));
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.construction1").withStyle(ChatFormatting.GRAY));
		}
		else if(WireType.STRUCTURE_CATEGORY.equals(type.getCategory()))
		{
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.construction0").withStyle(ChatFormatting.GRAY));
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"coil.construction1").withStyle(ChatFormatting.GRAY));
		}
		WireLink link = stack.get(IEApiDataComponents.WIRE_LINK.get());
		if(link!=null)
		{
			String dimensionName = "";
			if(link.dimension()!=null)
			{
				String s2 = link.dimension().location().getPath();
				if(s2.toLowerCase(Locale.ENGLISH).startsWith("the_"))
					s2 = s2.substring(4);
				dimensionName = Utils.toCamelCase(s2);
			}
			list.add(Component.translatable(Lib.DESC_INFO+"attachedToDim", link.cp().getX(),
					link.cp().getY(), link.cp().getZ(), dimensionName));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		return WirecoilUtils.doCoilUse(this, ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getHand(), ctx.getClickedFace(),
				(float)ctx.getClickLocation().x, (float)ctx.getClickLocation().y, (float)ctx.getClickLocation().z);
	}
}