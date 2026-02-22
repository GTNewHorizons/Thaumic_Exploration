package flaxbeard.thaumicexploration.integration.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

public class NEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        ReplicatorHandler.init();
        API.registerRecipeHandler(new ReplicatorHandler());
        API.registerUsageHandler(new ReplicatorHandler());
    }

    @Override
    public String getName() {
        return "Thaumic Exploration NEI";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
