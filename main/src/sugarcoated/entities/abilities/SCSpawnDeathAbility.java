package sugarcoated.entities.abilities;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;

public class SCSpawnDeathAbility extends SpawnDeathAbility {
    /** Status effect applied to spawned units */
    public StatusEffect appliedEffect;
    public float effectDuration = 60;

    public SCSpawnDeathAbility(UnitType unit, int amount, float spread){
        this.unit = unit;
        this.amount = amount;
        this.spread = spread;
    }

    @Override
    public void death(Unit unit){
        if(!Vars.net.client()){
            int spawned = amount + Mathf.random(randAmount);
            for(int i = 0; i < spawned; i++){
                Tmp.v1.rnd(Mathf.random(spread));
                var un = this.unit.spawn(unit.team, unit.x + Tmp.v1.x, unit.y + Tmp.v1.y);

                un.rotation = faceOutwards ? Tmp.v1.angle() : unit.rotation + Mathf.range(5f);

                if(appliedEffect != null){
                    un.apply(appliedEffect, effectDuration);
                }
            }
        }
    }
}
