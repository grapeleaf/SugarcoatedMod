package sugarcoated.content.blocks;

import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import sugarcoated.content.*;

public class SCEnvironmentBlocks {

    public static Block
    //Floors
    blueSugarFloor, pinkSugarFloor, sparseBlueSugar, sparsePinkSugar, cocoaFloor, denseCocoaFloor, crystalFloor,
    //Liquid Floors
    hotChocolate,
    //Walls
    sugarWall, chocolateWall, crystallineJelly, crystalWall;

    public static void load(){

        //Floors
        sparseBlueSugar = new Floor("sparse-blue-sugar-floor"){{
            variants = 3;
        }};

        sparsePinkSugar = new Floor("sparse-pink-sugar-floor"){{
            variants = 3;
        }};

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

        denseCocoaFloor = new Floor("dense-cocoa-floor"){{
           variants = 2;
           blendGroup = cocoaFloor;
           itemDrop = SCItems.cocoa;
        }};

        crystalFloor = new Floor("crystal-floor"){{
            variants = 3;
        }};

        //Liquid Floors
        hotChocolate = new Floor("hot-chocolate-floor"){{
            variants = 3;
            speedMultiplier = 0.2f;
            damageTaken = 5f;
            drownTime = 10f * 60f;
            isLiquid = true;

            status = StatusEffects.melting;
            statusDuration = 5f * 60f;

            cacheLayer = SCCacheLayer.hotChocolate;
            albedo = 0.15f;
            obstructsLight = true;
        }};

        //Walls
        sugarWall = new StaticWall("sugar-wall"){{
           blueSugarFloor.asFloor().wall = this;
        }};

        chocolateWall = new StaticWall("chocolate-wall"){{
           variants = 0;
           cocoaFloor.asFloor().wall = this;
        }};

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
