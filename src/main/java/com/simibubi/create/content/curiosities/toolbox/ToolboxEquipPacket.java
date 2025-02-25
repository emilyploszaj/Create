package com.simibubi.create.content.curiosities.toolbox;

import java.util.function.Supplier;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ToolboxEquipPacket extends SimplePacketBase {

	private BlockPos toolboxPos;
	private int slot;
	private int hotbarSlot;

	public ToolboxEquipPacket(BlockPos toolboxPos, int slot, int hotbarSlot) {
		this.toolboxPos = toolboxPos;
		this.slot = slot;
		this.hotbarSlot = hotbarSlot;
	}

	public ToolboxEquipPacket(FriendlyByteBuf buffer) {
		if (buffer.readBoolean())
			toolboxPos = buffer.readBlockPos();
		slot = buffer.readVarInt();
		hotbarSlot = buffer.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(toolboxPos != null);
		if (toolboxPos != null)
			buffer.writeBlockPos(toolboxPos);
		buffer.writeVarInt(slot);
		buffer.writeVarInt(hotbarSlot);
	}

	@Override
	public void handle(Supplier<Context> context) {
		Context ctx = context.get();
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.getSender();
			Level world = player.level;

			if (toolboxPos == null) {
				ToolboxHandler.unequip(player, hotbarSlot, false);
				ToolboxHandler.syncData(player);
				return;
			}

			BlockEntity blockEntity = world.getBlockEntity(toolboxPos);

			double maxRange = ToolboxHandler.getMaxRange(player);
			if (player.distanceToSqr(toolboxPos.getX() + 0.5, toolboxPos.getY(), toolboxPos.getZ() + 0.5) > maxRange
				* maxRange)
				return;
			if (!(blockEntity instanceof ToolboxTileEntity))
				return;

			ToolboxHandler.unequip(player, hotbarSlot, false);

			if (slot < 0 || slot >= 8) {
				ToolboxHandler.syncData(player);
				return;
			}

			ToolboxTileEntity toolboxTileEntity = (ToolboxTileEntity) blockEntity;

			ItemStack playerStack = player.getInventory().getItem(hotbarSlot);
			if (!playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack,
				toolboxTileEntity.inventory.filters.get(slot))) {
				toolboxTileEntity.inventory.inLimitedMode(inventory -> {
					try (Transaction t = TransferUtil.getTransaction()) {
						ItemVariant stack = ItemVariant.of(playerStack);
						long count = playerStack.getCount();
						long inserted = inventory.insert(stack, count, t);
						if (inserted != count)
							inserted += TransferUtil.insertToNotHotbar(player, stack, count - inserted);
						long remainder = count - inserted;
						if (remainder != count) {
							t.commit();
							ItemStack newStack = player.getSlot(hotbarSlot).get().copy();
							newStack.setCount((int) remainder);
							player.getInventory().setItem(hotbarSlot, newStack);
						}
					}
				});
			}

			CompoundTag compound = player.getExtraCustomData()
				.getCompound("CreateToolboxData");
			String key = String.valueOf(hotbarSlot);

			CompoundTag data = new CompoundTag();
			data.putInt("Slot", slot);
			data.put("Pos", NbtUtils.writeBlockPos(toolboxPos));
			compound.put(key, data);

			player.getExtraCustomData()
				.put("CreateToolboxData", compound);

			toolboxTileEntity.connectPlayer(slot, player, hotbarSlot);
			ToolboxHandler.syncData(player);
		});
		ctx.setPacketHandled(true);
	}

}
