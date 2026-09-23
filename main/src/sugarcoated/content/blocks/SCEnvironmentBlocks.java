package sugarcoated.content.blocks;

import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import sugarcoated.content.SCItems;

public class SCEnvironmentBlocks {

    public static Block
    //Floors
    blueSugarFloor, pinkSugarFloor, cocoaFloor, crystalFloor,
    //Walls
    crystallineJelly, crystalWall;

    public static void load(){

        //Floors
        blueSugarFloor = new Floor("blue-sugar-floor"){{
            variants = 3;
            itemDrop = SCItems.sugar;
        }};

        pinkSugarFloor = new Floor("pink-sugar-floor"){{
            variants = 3;
            itemDrop = SCItems.sugar;
        }};

        cocoaFloor = new Floor("cocoa-floor"){{
            variants = 3;
            itemDrop = SCItems.cocoa;
        }};

        crystalFloor = new Floor("crystal-floor"){{
            variants = 3;
        }};

        //Walls
        crystallineJelly = new StaticWall("crystalline-jelly"){{
            variants = 2;
            itemDrop = SCItems.jelly;
            crystalFloor.asFloor().wall = this;
        }};

        crystalWall = new StaticWall("crystal-wall"){{
            variants = 3;
            crystalFloor.asFloor().wall = this;
        }};
    }
}
