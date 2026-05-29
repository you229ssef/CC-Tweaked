package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.ITurtleAccess;
import dan200.computercraft.api.turtle.TurtleCommand;
import dan200.computercraft.api.turtle.TurtleCommandResult;
import dan200.computercraft.shared.turtle.blocks.TurtleBlockEntity;

public class TurtleChangeIDCommand implements TurtleCommand {
    private final int newID;

    public TurtleChangeIDCommand(int newID) {
        this.newID = newID;
    }

    @Override
    public TurtleCommandResult execute(ITurtleAccess turtle) {
        if (turtle instanceof TurtleBrain brain) {
            TurtleBlockEntity entity = brain.getOwner();
            entity.setComputerID(newID);
            return TurtleCommandResult.success();
        }
        return TurtleCommandResult.failure("Internal error");
    }
}
