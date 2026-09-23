package sugarcoated.content;

import mindustry.content.StatusEffects;
import mindustry.graphics.*;
import mindustry.type.*;

public class SCStatusEffects {
    public static StatusEffect
    speedy;

    public static void load(){
        speedy = new StatusEffect("speedy"){{
            color = Pal.boostTo;
            speedMultiplier = 3f;

            init(() -> opposite(StatusEffects.unmoving));
        }};
    }
}
