package com.github.talrey.createdeco.items;

import com.github.talrey.createdeco.blocks.CatwalkBlock;
import com.simibubi.create.foundation.utility.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import com.simibubi.create.foundation.utility.placement.PlacementOffset;

import io.github.fabricators_of_create.porting_lib.item.UseFirstBehaviorItem;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Predicate;

public class CatwalkBlockItem extends BlockItem implements UseFirstBehaviorItem {
  private final int placementHelperID;

  public CatwalkBlockItem (CatwalkBlock block, Properties props) {
    super(block, props);
    placementHelperID = PlacementHelpers.register(new CatwalkHelper());
  }

  @Override
  public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
    BlockPos pos   = ctx.getClickedPos();
    Direction face = ctx.getClickedFace();
    Level world    = ctx.getLevel();

    Player player     = ctx.getPlayer();
    BlockState state        = world.getBlockState(pos);
    IPlacementHelper helper = PlacementHelpers.get(placementHelperID);
    BlockHitResult ray = new BlockHitResult(ctx.getClickLocation(), face, pos, true);
    if (helper.matchesState(state) && player != null) {
      return helper.getOffset(player, world, state, pos, ray).placeInWorld(world, this, player, ctx.getHand(), ray);
    }
    return InteractionResult.PASS;
  }

  @MethodsReturnNonnullByDefault
  public static class CatwalkHelper implements IPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate () {
      return (Predicate<ItemStack>) CatwalkBlock::isCatwalk;
    }

    @Override
    public Predicate<BlockState> getStatePredicate () {
      return state -> CatwalkBlock.isCatwalk(state.getBlock());
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
      Direction face = ray.getDirection();
      if (face.getAxis() != Direction.Axis.Y) {
        return PlacementOffset.success(pos.offset(face.getNormal()), offsetState -> offsetState
          .setValue(CatwalkBlock.LIFTED, state.getValue(CatwalkBlock.LIFTED))
          .setValue(CatwalkBlock.getPropertyFromDirection(face),
            !CatwalkBlock.isCatwalk(world.getBlockState(pos.offset(face.getNormal()).offset(face.getNormal())).getBlock()))
          .setValue(CatwalkBlock.getPropertyFromDirection(face.getOpposite()), false)
        );
      }
      List<Direction> dirs = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), Direction.Axis.Y);
      for (Direction dir : dirs) {
        BlockPos newPos = pos.offset(dir.getNormal());
        if (!CatwalkBlock.canPlaceCatwalk(world, newPos)) continue;
        return PlacementOffset.success(newPos, offsetState -> offsetState
          .setValue(CatwalkBlock.LIFTED, state.getValue(CatwalkBlock.LIFTED))
          .setValue(CatwalkBlock.getPropertyFromDirection(dir), !CatwalkBlock.isCatwalk(world.getBlockState(newPos.offset(dir.getNormal())).getBlock()))
          .setValue(CatwalkBlock.getPropertyFromDirection(dir.getOpposite()), false)
        );
      }
      return PlacementOffset.fail();
    }
  }
}
