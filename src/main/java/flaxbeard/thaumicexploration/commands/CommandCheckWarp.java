package flaxbeard.thaumicexploration.commands;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import thaumcraft.common.Thaumcraft;

/**
 * Created by Katrina on 04/11/2014.
 */
public class CommandCheckWarp implements ICommand {
    @Override
    public String getCommandName() {
        return "checkwarp";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "/checkwarp";
    }

    @Override
    public List getCommandAliases() {
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("checkwarp");
        return arr;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        p_71515_1_.addChatMessage(new ChatComponentText(
                "Permanent Warp: " + Thaumcraft.proxy.playerKnowledge.getWarpPerm(p_71515_1_.getCommandSenderName())));
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
