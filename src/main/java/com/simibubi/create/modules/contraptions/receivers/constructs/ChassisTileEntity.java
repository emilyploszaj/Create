package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllPackets;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.foundation.block.SyncedTileEntity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.MathHelper;

public class ChassisTileEntity extends SyncedTileEntity implements ITickableTileEntity {

	public static final int MAX_RANGE = 16;
	public static final int DEFAULT_RANGE = 8;
	private int range;

	public int newRange;
	public int lastModified;

	public ChassisTileEntity() {
		super(AllTileEntities.CHASSIS.type);
		newRange = range = DEFAULT_RANGE;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("Range", range);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		newRange = range = compound.getInt("Range");
		super.read(compound);
	}

	public int getRange() {
		if (world.isRemote)
			return newRange;
		return range;
	}

	public void setRange(int range) {
		this.range = range;
		sendData();
	}

	public void setRangeLazily(int range) {
		this.newRange = MathHelper.clamp(range, 1, MAX_RANGE);
		if (newRange == this.range)
			return;
		this.lastModified = 0;
	}

	@Override
	public void tick() {
		if (!world.isRemote)
			return;
		if (lastModified == -1)
			return;
		if (lastModified++ > 10) {
			lastModified = -1;
			AllPackets.channel.sendToServer(new ConfigureChassisPacket(pos, newRange));
		}
	}

}
