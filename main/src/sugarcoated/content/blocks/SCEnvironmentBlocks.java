package sugarcoated.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import sugarcoated.content.*;

public class SCEnvironmentBlocks {

    public static Block
    //Floors
    blueSugarFloor, pinkSugarFloor, sparseBlueSugar, sparsePinkSugar, cocoaFloor, denseCocoaFloor, crystalFloor,
    vanillaFrosting, strawberryFrosting,
    whiteCandy, redCandy, greenCandy,
    //Liquid Floors
    hotChocolate,
    //Walls
    blueSugarWall, pinkSugarWall, chocolateWall, crystallineJelly, crystalWall, sugarCrystalChunk,
    vanillaFrostingWall, strawberryFrostingWall,
    //Props
    sugarCrystal, lollipopTree;

    public static void load(){

        //Floors
        whiteCandy = new Floor("white-candy"){{
            variants = 0;
        }};

        redCandy = new Floor("red-candy"){{
            variants = 0;
        }};

        greenCandy = new Floor("green-candy"){{
            variants = 0;
        }};

        sparseBlueSugar = new Floor("sparse-blue-sugar-floor"){{
            variants = 3;
        }};

        sparsePinkSugar = new Floor("sparse-pink-sugar-floor"){{
            variants = 3;
        }};

        blueSugarFloor = new Floor("blue-sugar-floor"){{
            variants = 3;
        }};

        pinkSugarFloor = new Floor("pink-sugar-floor"){{
            variants = 3;
        }};

        cocoaFloor = new Floor("cocoa-floor"){{
            variants = 3;
        }};

        denseCocoaFloor = new Floor("dense-cocoa-floor"){{
           variants = 2;
           blendGroup = cocoaFloor;
        }};

        crystalFloor = new Floor("crystal-floor"){{
            variants = 3;
        }};

        vanillaFrosting = new Floor("vanilla-frosting"){{
            variants = 0;
        }};

        strawberryFrosting = new Floor("strawberry-frosting"){{
            variants = 0;
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
        blueSugarWall = new StaticWall("blue-sugar-wall"){{
            blueSugarFloor.asFloor().wall = this;
        }};

        pinkSugarWall = new StaticWall("pink-sugar-wall"){{
            pinkSugarFloor.asFloor().wall = this;
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

        sugarCrystalChunk = new StaticWall("sugar-crystal-chunk"){{
            hasShadow = false;
            playerUnmineable = true;
            variants = 2;
            itemDrop = SCItems.sugar;
        }};

        vanillaFrostingWall = new TallBlock("vanilla-frosting-wall"){{
            variants = 3;
            clipSize = 48f;
            shadowOffset = 0;
            vanillaFrosting.asFloor().wall = this;
        }};

        strawberryFrostingWall = new TallBlock("strawberry-frosting-wall"){{
            variants = 3;
            clipSize = 48f;
            shadowOffset = 0;
            strawberryFrosting.asFloor().wall = this;
        }};

        //Prop
        sugarCrystal = new TallBlock("sugar-crystal"){{
            clipSize = 156f;
            variants = 0;
            emitLight = true;
            itemDrop = SCItems.sugar;
        }};

        lollipopTree = new TreeBlock("lollipop-tree");
    }
}
