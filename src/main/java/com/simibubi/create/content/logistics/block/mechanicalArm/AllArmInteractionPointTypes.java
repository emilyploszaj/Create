package com.simibubi.create.content.logistics.block.mechanicalArm;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.fabricators_of_create.porting_lib.extensions.LevelExtensions;
import io.github.fabricators_of_create.porting_lib.transfer.callbacks.TransactionCallback;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import net.minecraft.core.Vec3i;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterBlock;
import com.simibubi.create.content.contraptions.components.crafter.MechanicalCrafterTileEntity;
import com.simibubi.create.content.contraptions.components.deployer.DeployerBlock;
import com.simibubi.create.content.contraptions.components.saw.SawBlock;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.logistics.block.belts.tunnel.BeltTunnelBlock;
import com.simibubi.create.content.logistics.block.chute.AbstractChuteBlock;
import com.simibubi.create.content.logistics.block.funnel.AbstractFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.BeltFunnelBlock.Shape;
import com.simibubi.create.content.logistics.block.funnel.FunnelBlock;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class AllArmInteractionPointTypes {

	public static final BasinType BASIN = register("basin", BasinType::new);
	public static final BeltType BELT = register("belt", BeltType::new);
	public static final BlazeBurnerType BLAZE_BURNER = register("blaze_burner", BlazeBurnerType::new);
	public static final ChuteType CHUTE = register("chute", ChuteType::new);
	public static final CrafterType CRAFTER = register("crafter", CrafterType::new);
	public static final CrushingWheelsType CRUSHING_WHEELS = register("crushing_wheels", CrushingWheelsType::new);
	public static final DeployerType DEPLOYER = register("deployer", DeployerType::new);
	public static final DepotType DEPOT = register("depot", DepotType::new);
	public static final FunnelType FUNNEL = register("funnel", FunnelType::new);
	public static final MillstoneType MILLSTONE = register("millstone", MillstoneType::new);
	public static final SawType SAW = register("saw", SawType::new);

	public static final CampfireType CAMPFIRE = register("campfire", CampfireType::new);
	public static final ComposterType COMPOSTER = register("composter", ComposterType::new);
	public static final JukeboxType JUKEBOX = register("jukebox", JukeboxType::new);
	public static final RespawnAnchorType RESPAWN_ANCHOR = register("respawn_anchor", RespawnAnchorType::new);

	private static <T extends ArmInteractionPointType> T register(String id, Function<ResourceLocation, T> factory) {
		T type = factory.apply(Create.asResource(id));
		ArmInteractionPointType.register(type);
		return type;
	}

	public static void register() {
	}

	//

	public static class BasinType extends ArmInteractionPointType {
		public BasinType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.BASIN.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class BeltType extends ArmInteractionPointType {
		public BeltType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.BELT.has(state) && !(level.getBlockState(pos.above())
				.getBlock() instanceof BeltTunnelBlock);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new BeltPoint(this, level, pos, state);
		}
	}

	public static class BlazeBurnerType extends ArmInteractionPointType {
		public BlazeBurnerType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.BLAZE_BURNER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new BlazeBurnerPoint(this, level, pos, state);
		}
	}

	public static class ChuteType extends ArmInteractionPointType {
		public ChuteType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AbstractChuteBlock.isChute(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new TopFaceArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class CrafterType extends ArmInteractionPointType {
		public CrafterType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_CRAFTER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CrafterPoint(this, level, pos, state);
		}
	}

	public static class CrushingWheelsType extends ArmInteractionPointType {
		public CrushingWheelsType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.CRUSHING_WHEEL_CONTROLLER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new TopFaceArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class DeployerType extends ArmInteractionPointType {
		public DeployerType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.DEPLOYER.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new DeployerPoint(this, level, pos, state);
		}
	}

	public static class DepotType extends ArmInteractionPointType {
		public DepotType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.DEPOT.has(state) || AllBlocks.WEIGHTED_EJECTOR.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new DepotPoint(this, level, pos, state);
		}
	}

	public static class FunnelType extends ArmInteractionPointType {
		public FunnelType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof AbstractFunnelBlock
				&& !(state.hasProperty(FunnelBlock.EXTRACTING) && state.getValue(FunnelBlock.EXTRACTING))
				&& !(state.hasProperty(BeltFunnelBlock.SHAPE) && state.getValue(BeltFunnelBlock.SHAPE) == Shape.PUSHING);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new FunnelPoint(this, level, pos, state);
		}
	}

	public static class MillstoneType extends ArmInteractionPointType {
		public MillstoneType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.MILLSTONE.has(state);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ArmInteractionPoint(this, level, pos, state);
		}
	}

	public static class SawType extends ArmInteractionPointType {
		public SawType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return AllBlocks.MECHANICAL_SAW.has(state) && state.getValue(SawBlock.FACING) == Direction.UP
				&& ((KineticTileEntity) level.getBlockEntity(pos)).getSpeed() != 0;
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new DepotPoint(this, level, pos, state);
		}
	}

	public static class CampfireType extends ArmInteractionPointType {
		public CampfireType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.getBlock() instanceof CampfireBlock;
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new CampfirePoint(this, level, pos, state);
		}
	}

	public static class ComposterType extends ArmInteractionPointType {
		public ComposterType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(Blocks.COMPOSTER);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new ComposterPoint(this, level, pos, state);
		}
	}

	public static class JukeboxType extends ArmInteractionPointType {
		public JukeboxType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(Blocks.JUKEBOX);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new JukeboxPoint(this, level, pos, state);
		}
	}

	public static class RespawnAnchorType extends ArmInteractionPointType {
		public RespawnAnchorType(ResourceLocation id) {
			super(id);
		}

		@Override
		public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
			return state.is(Blocks.RESPAWN_ANCHOR);
		}

		@Override
		public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
			return new RespawnAnchorPoint(this, level, pos, state);
		}
	}

	//

	public static class DepositOnlyArmInteractionPoint extends ArmInteractionPoint {
		public DepositOnlyArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public void cycleMode() {
		}

		@Override
		public ItemStack extract(int amount, TransactionContext ctx) {
			return ItemStack.EMPTY;
		}
	}

	public static class TopFaceArmInteractionPoint extends ArmInteractionPoint {
		public TopFaceArmInteractionPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 1, .5f);
		}
	}

	public static class BeltPoint extends DepotPoint {
		public BeltPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public void keepAlive() {
			super.keepAlive();
			BeltTileEntity beltTE = BeltHelper.getSegmentTE(level, pos);
			if (beltTE == null)
				return;
			TransportedItemStackHandlerBehaviour transport =
				beltTE.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
			if (transport == null)
				return;
			MutableBoolean found = new MutableBoolean(false);
			transport.handleProcessingOnAllItems(tis -> {
				if (found.isTrue())
					return TransportedResult.doNothing();
				tis.lockedExternally = true;
				found.setTrue();
				return TransportedResult.doNothing();
			});
		}
	}

	public static class BlazeBurnerPoint extends DepositOnlyArmInteractionPoint {
		public BlazeBurnerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ItemStack stack, TransactionContext ctx) {
			ItemStack input = stack.copy();
			InteractionResultHolder<ItemStack> res = BlazeBurnerBlock.tryInsert(cachedState, level, pos, input, false, false, ctx);
			ItemStack remainder = res.getObject();
			if (input.isEmpty()) {
				return remainder;
			} else {
				TransactionCallback.onSuccess(ctx, () ->
						Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder));
				return input;
			}
		}
	}

	public static class CrafterPoint extends ArmInteractionPoint {
		public CrafterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return cachedState.getValue(MechanicalCrafterBlock.HORIZONTAL_FACING)
				.getOpposite();
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal()).scale(.5f));
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (oldState != cachedState)
				cachedAngles = null;
		}

		@Override
		public ItemStack extract(int amount, TransactionContext ctx) {
			BlockEntity te = level.getBlockEntity(pos);
			if (!(te instanceof MechanicalCrafterTileEntity))
				return ItemStack.EMPTY;
			MechanicalCrafterTileEntity crafter = (MechanicalCrafterTileEntity) te;
			SmartInventory inventory = crafter.getInventory();
			inventory.allowExtraction();
			ItemStack extract = super.extract(amount, ctx);
			inventory.forbidExtraction();
			return extract;
		}
	}

	public static class DeployerPoint extends ArmInteractionPoint {
		public DeployerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Direction getInteractionDirection() {
			return cachedState.getValue(DeployerBlock.FACING)
				.getOpposite();
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return super.getInteractionPositionVector()
				.add(Vec3.atLowerCornerOf(getInteractionDirection().getNormal()).scale(.65f));
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (oldState != cachedState)
				cachedAngles = null;
		}
	}

	public static class DepotPoint extends ArmInteractionPoint {
		public DepotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 14 / 16f, .5f);
		}
	}

	public static class FunnelPoint extends DepositOnlyArmInteractionPoint {
		public FunnelPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			Direction facing = FunnelBlock.getFunnelFacing(cachedState);
			return VecHelper.getCenterOf(pos)  // FIXME SHOULD NEVER BE NULL
				.add(Vec3.atLowerCornerOf(facing == null ? Vec3i.ZERO : facing
					.getNormal()).scale(-.15f));
		}

		@Override
		protected Direction getInteractionDirection() {
			Direction facing = FunnelBlock.getFunnelFacing(cachedState);
			return facing != null ? facing : Direction.UP; // FIXME SHOULD NEVER BE NULL
		}

		@Override
		public void updateCachedState() {
			BlockState oldState = cachedState;
			super.updateCachedState();
			if (oldState != cachedState)
				cachedAngles = null;
		}

		@Override
		public ItemStack insert(ItemStack stack, TransactionContext ctx) {
			FilteringBehaviour filtering = TileEntityBehaviour.get(level, pos, FilteringBehaviour.TYPE);
			InvManipulationBehaviour inserter = TileEntityBehaviour.get(level, pos, InvManipulationBehaviour.TYPE);
			if (cachedState.getOptionalValue(BlockStateProperties.POWERED).orElse(false))
				return stack;
			if (inserter == null)
				return stack;
			if (filtering != null && !filtering.test(stack))
				return stack;

			ItemStack insert = inserter.insert(stack);
			if (insert.getCount() != stack.getCount()) {
				BlockEntity tileEntity = level.getBlockEntity(pos);
				if (tileEntity instanceof FunnelTileEntity) {
					FunnelTileEntity funnelTileEntity = (FunnelTileEntity) tileEntity;
					funnelTileEntity.onTransfer(stack);
					if (funnelTileEntity.hasFlap())
						funnelTileEntity.flap(true);
				}
			}
			return insert;
		}
	}

	public static class CampfirePoint extends DepositOnlyArmInteractionPoint {
		public CampfirePoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ItemStack stack, TransactionContext ctx) {
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof CampfireBlockEntity campfireBE))
				return stack;
			Optional<CampfireCookingRecipe> recipe = campfireBE.getCookableRecipe(stack);
			if (recipe.isEmpty())
				return stack;
			boolean hasSpace = false;
			for (ItemStack campfireStack : campfireBE.getItems()) {
				if (campfireStack.isEmpty()) {
					hasSpace = true;
					break;
				}
			}
			if (!hasSpace)
				return stack;
			ItemStack remainder = stack.copy();
			TransactionCallback.onSuccess(ctx, () ->
					campfireBE.placeFood(remainder.copy(), recipe.get().getCookingTime()));
			remainder.shrink(1);
			return remainder;
		}
	}

	public static class ComposterPoint extends ArmInteractionPoint {
		public ComposterPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 13 / 16f, .5f);
		}

		// fabric: should not be needed since FAPI handles wrapping Containers
//		@Override
//		public void updateCachedState() {
//			BlockState oldState = cachedState;
//			super.updateCachedState();
//			if (oldState != cachedState)
//				cachedHandler.invalidate();
//		}
//
//		@Nullable
//		@Override
//		protected IItemHandler getHandler() {
//			if (!cachedHandler.isPresent()) {
//				cachedHandler = LazyOptional.of(() -> {
//					ComposterBlock composterBlock = (ComposterBlock) Blocks.COMPOSTER;
//					WorldlyContainer container = composterBlock.getContainer(cachedState, level, pos);
//					SidedInvWrapper insertionHandler = new SidedInvWrapper(container, Direction.UP);
//					SidedInvWrapper extractionHandler = new SidedInvWrapper(container, Direction.DOWN);
//					return new CombinedInvWrapper(insertionHandler, extractionHandler);
//				});
//			}
//			return cachedHandler.orElse(null);
//		}
	}

	public static class JukeboxPoint extends TopFaceArmInteractionPoint {
		public JukeboxPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		public ItemStack insert(ItemStack stack, TransactionContext ctx) {
			Item item = stack.getItem();
			if (!(item instanceof RecordItem))
				return stack;
			if (cachedState.getValue(JukeboxBlock.HAS_RECORD))
				return stack;
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof JukeboxBlockEntity jukeboxBE))
				return stack;
			if (!jukeboxBE.getRecord()
				.isEmpty())
				return stack;
			ItemStack remainder = stack.copy();
			ItemStack toInsert = remainder.split(1);
			level.updateSnapshots(ctx);
			level.setBlock(pos, cachedState.setValue(JukeboxBlock.HAS_RECORD, true), 2);
			TransactionCallback.onSuccess(ctx, () -> {
				jukeboxBE.setRecord(toInsert);
				level.levelEvent(null, 1010, pos, Item.getId(item));
				AllTriggers.triggerForNearbyPlayers(AllTriggers.MUSICAL_ARM, level, pos, 10);
			});
			return remainder;
		}

		@Override
		public ItemStack extract(int amount, TransactionContext ctx) {
			if (!cachedState.getValue(JukeboxBlock.HAS_RECORD))
				return ItemStack.EMPTY;
			BlockEntity blockEntity = level.getBlockEntity(pos);
			if (!(blockEntity instanceof JukeboxBlockEntity jukeboxBE))
				return ItemStack.EMPTY;
			ItemStack record = jukeboxBE.getRecord();
			if (record.isEmpty())
				return ItemStack.EMPTY;
			level.updateSnapshots(ctx);
			level.setBlock(pos, cachedState.setValue(JukeboxBlock.HAS_RECORD, false), 2);
			TransactionCallback.onSuccess(ctx, () -> {
				level.levelEvent(1010, pos, 0);
				jukeboxBE.clearContent();
			});
			return record;
		}
	}

	public static class RespawnAnchorPoint extends DepositOnlyArmInteractionPoint {
		public RespawnAnchorPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
			super(type, level, pos, state);
		}

		@Override
		protected Vec3 getInteractionPositionVector() {
			return Vec3.atLowerCornerOf(pos).add(.5f, 1, .5f);
		}

		@Override
		public ItemStack insert(ItemStack stack, TransactionContext ctx) {
			if (!stack.is(Items.GLOWSTONE))
				return stack;
			if (cachedState.getValue(RespawnAnchorBlock.CHARGE) == 4)
				return stack;
			TransactionCallback.onSuccess(ctx, () -> RespawnAnchorBlock.charge(level, pos, cachedState));
			ItemStack remainder = stack.copy();
			remainder.shrink(1);
			return remainder;
		}
	}

}
