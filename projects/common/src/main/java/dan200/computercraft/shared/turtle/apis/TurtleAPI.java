// Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
//
// SPDX-License-Identifier: LicenseRef-CCPL

package dan200.computercraft.shared.turtle.apis;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.*;
import dan200.computercraft.api.turtle.TurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.api.turtle.TurtleSide;
import dan200.computercraft.core.metrics.Metrics;
import dan200.computercraft.core.metrics.MetricsObserver;
import dan200.computercraft.shared.turtle.core.*;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Turtles are a robotic device, which can break and place blocks, attack mobs, and move about the world. They have
 * an internal inventory of 16 slots, allowing them to store blocks they have broken or would like to place.
 */
public class TurtleAPI implements ILuaAPI {
    private final MetricsObserver metrics;
    private final TurtleAccessInternal turtle;

    public TurtleAPI(MetricsObserver metrics, TurtleAccessInternal turtle) {
        this.metrics = metrics;
        this.turtle = turtle;
    }

    @Override
    public String[] getNames() {
        return new String[]{ "turtle" };
    }

    private MethodResult trackCommand(TurtleCommand command) {
        metrics.observe(Metrics.TURTLE_OPS);
        return turtle.executeCommand(command);
    }

    @LuaFunction
    public final MethodResult forward() {
        return trackCommand(new TurtleMoveCommand(MoveDirection.FORWARD));
    }

    @LuaFunction
    public final MethodResult back() {
        return trackCommand(new TurtleMoveCommand(MoveDirection.BACK));
    }

    @LuaFunction
    public final MethodResult up() {
        return trackCommand(new TurtleMoveCommand(MoveDirection.UP));
    }

    @LuaFunction
    public final MethodResult down() {
        return trackCommand(new TurtleMoveCommand(MoveDirection.DOWN));
    }

    @LuaFunction
    public final MethodResult turnLeft() {
        return trackCommand(new TurtleTurnCommand(TurnDirection.LEFT));
    }

    @LuaFunction
    public final MethodResult turnRight() {
        return trackCommand(new TurtleTurnCommand(TurnDirection.RIGHT));
    }

    @LuaFunction
    public final MethodResult dig(Optional<TurtleSide> side) {
        metrics.observe(Metrics.TURTLE_OPS);
        return trackCommand(TurtleToolCommand.dig(InteractDirection.FORWARD, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult digUp(Optional<TurtleSide> side) {
        metrics.observe(Metrics.TURTLE_OPS);
        return trackCommand(TurtleToolCommand.dig(InteractDirection.UP, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult digDown(Optional<TurtleSide> side) {
        metrics.observe(Metrics.TURTLE_OPS);
        return trackCommand(TurtleToolCommand.dig(InteractDirection.DOWN, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult interact(Optional<TurtleSide> side) {
        return trackCommand(TurtleToolCommand.interact(InteractDirection.FORWARD, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult interactUp(Optional<TurtleSide> side) {
        return trackCommand(TurtleToolCommand.interact(InteractDirection.UP, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult interactDown(Optional<TurtleSide> side) {
        return trackCommand(TurtleToolCommand.interact(InteractDirection.DOWN, side.orElse(null)));
    }

    @LuaFunction("*changeID")
    public final MethodResult changeID(int id) {
        return trackCommand(new TurtleChangeIDCommand(id));
    }

    @LuaFunction
    public final MethodResult place(IArguments args) throws LuaException {
        return trackCommand(new TurtlePlaceCommand(InteractDirection.FORWARD, args.getAll()));
    }

    @LuaFunction
    public final MethodResult placeUp(IArguments args) throws LuaException {
        return trackCommand(new TurtlePlaceCommand(InteractDirection.UP, args.getAll()));
    }

    @LuaFunction
    public final MethodResult placeDown(IArguments args) throws LuaException {
        return trackCommand(new TurtlePlaceCommand(InteractDirection.DOWN, args.getAll()));
    }

    @LuaFunction
    public final MethodResult drop(Optional<Integer> count) throws LuaException {
        return trackCommand(new TurtleDropCommand(InteractDirection.FORWARD, checkCount(count)));
    }

    @LuaFunction
    public final MethodResult dropUp(Optional<Integer> count) throws LuaException {
        return trackCommand(new TurtleDropCommand(InteractDirection.UP, checkCount(count)));
    }

    @LuaFunction
    public final MethodResult dropDown(Optional<Integer> count) throws LuaException {
        return trackCommand(new TurtleDropCommand(InteractDirection.DOWN, checkCount(count)));
    }

    @LuaFunction
    public final MethodResult select(int slot) throws LuaException {
        var actualSlot = checkSlot(slot);
        return turtle.executeCommand(turtle -> {
            turtle.setSelectedSlot(actualSlot);
            return TurtleCommandResult.success();
        });
    }

    @LuaFunction
    public final int getItemCount(Optional<Integer> slot) throws LuaException {
        int actualSlot = checkSlot(slot).orElse(turtle.getSelectedSlot());
        return turtle.getInventory().getItem(actualSlot).getCount();
    }

    @LuaFunction
    public final int getItemSpace(Optional<Integer> slot) throws LuaException {
        int actualSlot = checkSlot(slot).orElse(turtle.getSelectedSlot());
        var stack = turtle.getInventory().getItem(actualSlot);
        return stack.isEmpty() ? 64 : Math.min(stack.getMaxStackSize(), 64) - stack.getCount();
    }

    @LuaFunction
    public final MethodResult detect() {
        return trackCommand(new TurtleDetectCommand(InteractDirection.FORWARD));
    }

    @LuaFunction
    public final MethodResult detectUp() {
        return trackCommand(new TurtleDetectCommand(InteractDirection.UP));
    }

    @LuaFunction
    public final MethodResult detectDown() {
        return trackCommand(new TurtleDetectCommand(InteractDirection.DOWN));
    }

    @LuaFunction
    public final MethodResult compare() {
        return trackCommand(new TurtleCompareCommand(InteractDirection.FORWARD));
    }

    @LuaFunction
    public final MethodResult compareUp() {
        return trackCommand(new TurtleCompareCommand(InteractDirection.UP));
    }

    @LuaFunction
    public final MethodResult compareDown() {
        return trackCommand(new TurtleCompareCommand(InteractDirection.DOWN));
    }

    @LuaFunction
    public final MethodResult attack(Optional<TurtleSide> side) {
        return trackCommand(TurtleToolCommand.attack(InteractDirection.FORWARD, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult attackUp(Optional<TurtleSide> side) {
        return trackCommand(TurtleToolCommand.attack(InteractDirection.UP, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult attackDown(Optional<TurtleSide> side) {
        return trackCommand(TurtleToolCommand.attack(InteractDirection.DOWN, side.orElse(null)));
    }

    @LuaFunction
    public final MethodResult suck(Optional<Integer> count) throws LuaException {
        return trackCommand(new TurtleSuckCommand(InteractDirection.FORWARD, checkCount(count)));
    }

    @LuaFunction
    public final MethodResult suckUp(Optional<Integer> count) throws LuaException {
        return trackCommand(new TurtleSuckCommand(InteractDirection.UP, checkCount(count)));
    }

    @LuaFunction
    public final MethodResult suckDown(Optional<Integer> count) throws LuaException {
        return trackCommand(new TurtleSuckCommand(InteractDirection.DOWN, checkCount(count)));
    }

    @LuaFunction
    public final Object getFuelLevel() {
        return turtle.isFuelNeeded() ? turtle.getFuelLevel() : "unlimited";
    }

    @LuaFunction
    public final MethodResult refuel(Optional<Integer> countA) throws LuaException {
        int count = countA.orElse(Integer.MAX_VALUE);
        if (count < 0) throw new LuaException("Refuel count " + count + " out of range");
        return trackCommand(new TurtleRefuelCommand(count));
    }

    @LuaFunction
    public final MethodResult compareTo(int slot) throws LuaException {
        return trackCommand(new TurtleCompareToCommand(checkSlot(slot)));
    }

    @LuaFunction
    public final MethodResult transferTo(int slotArg, Optional<Integer> countArg) throws LuaException {
        var slot = checkSlot(slotArg);
        var count = checkCount(countArg);
        return trackCommand(new TurtleTransferToCommand(slot, count));
    }

    @LuaFunction
    public final int getSelectedSlot() {
        return turtle.getSelectedSlot() + 1;
    }

    @LuaFunction
    public final Object getFuelLimit() {
        return turtle.isFuelNeeded() ? turtle.getFuelLimit() : "unlimited";
    }

    @LuaFunction
    public final MethodResult equipLeft() {
        return trackCommand(new TurtleEquipCommand(TurtleSide.LEFT));
    }

    @LuaFunction
    public final MethodResult equipRight() {
        return trackCommand(new TurtleEquipCommand(TurtleSide.RIGHT));
    }

    @LuaFunction(mainThread = true)
    public final @Nullable Map<?, ?> getEquippedLeft() {
        var upgrade = turtle.getUpgradeWithData(TurtleSide.LEFT);
        return upgrade == null ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(turtle.getLevel().registryAccess(), upgrade.getUpgradeItem());
    }

    @LuaFunction(mainThread = true)
    public final @Nullable Map<?, ?> getEquippedRight() {
        var upgrade = turtle.getUpgradeWithData(TurtleSide.RIGHT);
        return upgrade == null ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(turtle.getLevel().registryAccess(), upgrade.getUpgradeItem());
    }

    @LuaFunction
    public final MethodResult inspect() {
        return trackCommand(new TurtleInspectCommand(InteractDirection.FORWARD));
    }

    @LuaFunction
    public final MethodResult inspectUp() {
        return trackCommand(new TurtleInspectCommand(InteractDirection.UP));
    }

    @LuaFunction
    public final MethodResult inspectDown() {
        return trackCommand(new TurtleInspectCommand(InteractDirection.DOWN));
    }

    @LuaFunction
    public final MethodResult getItemDetail(ILuaContext context, Optional<Integer> slot, Optional<Boolean> detailed) throws LuaException {
        int actualSlot = checkSlot(slot).orElse(turtle.getSelectedSlot());
        if (detailed.orElse(false)) {
            return context.executeMainThreadTask(() -> {
                var stack = turtle.getInventory().getItem(actualSlot);
                return new Object[]{ stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getDetails(turtle.getLevel().registryAccess(), stack) };
            });
        } else {
            var stack = turtle.getItemSnapshot(actualSlot);
            return MethodResult.of(stack.isEmpty() ? null : VanillaDetailRegistries.ITEM_STACK.getBasicDetails(turtle.getLevel().registryAccess(), stack));
        }
    }


    private static int checkSlot(int slot) throws LuaException {
        if (slot < 1 || slot > 16) throw new LuaException("Slot number " + slot + " out of range");
        return slot - 1;
    }

    private static Optional<Integer> checkSlot(Optional<Integer> slot) throws LuaException {
        return slot.isPresent() ? Optional.of(checkSlot(slot.get())) : Optional.empty();
    }

    private static int checkCount(Optional<Integer> countArg) throws LuaException {
        int count = countArg.orElse(64);
        if (count < 1 || count > 64) throw new LuaException("Item count " + count + " out of range");
        return count;
    }
}
