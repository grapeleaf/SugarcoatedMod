package sugarcoated.content;

import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.type.*;
import sugarcoated.CandyPal;

public class SCStatusEffects {
    public static StatusEffect
        enraged, speedy, blitzing;

    public static void load(){
        enraged = new StatusEffect("enraged"){{
            color = Pal.remove;
            speedMultiplier = 1.8f;
            damageMultiplier = 1.3f;
        }};

        speedy = new StatusEffect("speedy"){{
            color = CandyPal.greenMint;
            speedMultiplier = 2.3f;
        }};

        blitzing = new StatusEffect("blitzing"){{
            color = CandyPal.redMint;
            speedMultiplier = 3f;

            init(() -> opposite(StatusEffects.unmoving));
        }};
    }
}
