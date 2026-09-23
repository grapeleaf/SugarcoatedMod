package sugarcoated.content.blocks;

import mindustry.content.*;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.blocks.storage.CoreBlock;

import static mindustry.type.ItemStack.with;

public class SCStorageBlocks {
    public static Block
            coreBonbon;

    public static void load() {
        coreBonbon = new CoreBlock("core-bonbon"){{
           requirements(Category.effect, with(Items.copper, 1000, Items.lead, 1000));

           alwaysUnlocked = true;
           isFirstTier = true;

           size = 4;
           scaledHealth = 250;
           itemCapacity = 4500;

           unitCapModifier = 15;
           unitType = UnitTypes.gamma;
           requiresCoreZone = true;

           squareSprite = true;
           thrusterLength = 32f/4f;
        }};
    }
}
