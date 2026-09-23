package sugarcoated.content;

import sugarcoated.content.blocks.SCEnvironmentBlocks;
import sugarcoated.content.blocks.SCStorageBlocks;

public class SCBlocks {
    public static void load(){
        SCEnvironmentBlocks.load();
        SCStorageBlocks.load();
    }
}
