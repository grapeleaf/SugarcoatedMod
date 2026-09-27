package sugarcoated.content.blocks;

import mindustry.content.Items;
import mindustry.type.*;
import mindustry.world.*;
import sugarcoated.content.SCUnitTypes;
import sugarcoated.world.units.*;

import static mindustry.type.ItemStack.with;

public class SCPayloadBlocks {
    public static Block
    //Nests
    smallPeppermintNest;
    public static void load(){
        smallPeppermintNest = new CreatureNest("peppermint-nest-small"){{
            requirements(Category.units, with(Items.copper, 20, Items.lead, 20));
            size = 3;
            plans.add(
                new UnitPlan(SCUnitTypes.babyPepper, 20f * 60f, with(Items.copper, 20, Items.lead, 20)),
                new UnitPlan(SCUnitTypes.babyMint, 25f * 60f, with(Items.copper, 20, Items.lead, 20))
            );
        }};
    }
}
