package flaxbeard.thaumicexploration.commands;

import flaxbeard.thaumicexploration.tile.TileEntitySoulBrazier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

/**
 * Created by Katrina on 03/11/2014.
 */
public class CommandAlterRate implements ICommand {
    private List<String> aliases;

    public CommandAlterRate() {
        aliases = new ArrayList<String>();
        aliases.add("AlterRate");
        aliases.add("ar");
    }

    @Override
    public String getCommandName() {
        return "AlterRate";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/AlterRate <essentia rate> <vis rate>";
    }

    @Override
    public List getCommandAliases() {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        int essentia = Integer.parseInt(p_71515_2_[0]);
        int vis = Integer.parseInt(p_71515_2_[1]);
        TileEntitySoulBrazier.EssentiaRate = essentia;
        TileEntitySoulBrazier.VisRate = vis;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_) {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
