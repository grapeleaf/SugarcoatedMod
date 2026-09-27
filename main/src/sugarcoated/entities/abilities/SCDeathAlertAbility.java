package sugarcoated.entities.abilities;

import arc.audio.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.StatusEffect;
import mindustry.type.UnitType;
import sugarcoated.ai.CreatureAI;
import sugarcoated.content.type.unit.SCCreatureUnitType;

public class SCDeathAlertAbility extends Ability {
    public Sound alertSound = Sounds.none;
    /** Visual alert effect*/
    public Effect alertEffect = Fx.none;
    /** Optional status effect applies to alerted units*/
    public StatusEffect alertStatus = StatusEffects.none;
    /** If true, will only alert if this creature is alone*/
    public boolean triggerIfAlone = true;

    public float alertSoundVolume = 1f,
    /** Alert range*/
    range = -1f,
    /** Screen shake for alerting*/
    alertShake = 0f,
    alertStatusDuration = 60f;

    /** This ability doesn't work on regular units, only used for creatures*/
    public SCDeathAlertAbility(float range, Sound alertSound, float volume){
        this.range = range;
        this.alertSound = alertSound;
        this.alertSoundVolume = volume;
    }

    public SCDeathAlertAbility(float range){
        this.range = range;
    }

    @Override
    public void init(UnitType type) {
        if(range <= 0){
            range = type.range * 2;
        }
    }

    @Override
    public void death(Unit unit) {
        if(Vars.net.client()){
            return;
        }
        if(!(unit.type instanceof SCCreatureUnitType uType) || !(unit.controller() instanceof CreatureAI unitAI)){
            return;
        }

        if(triggerIfAlone){
            boolean[] nearby = {false};
            Units.nearby(unit.team, unit.x, unit.y, unit.range(), other -> {
                if(other != unit && other.type instanceof SCCreatureUnitType otherType && otherType.creatureFamily.equals(uType.creatureFamily)){
                    nearby[0] = true;
                }
            });
            if(nearby[0]){
                return;
            }
        }

        boolean applyStatus = alertStatus != StatusEffects.none;
        boolean[] alerted = {false};

        if(unitAI.combatTarget != null){
            Units.nearby(unit.team, unit.x, unit.y, range, other -> {
                if(other != unit && other.type instanceof SCCreatureUnitType otherType && otherType.creatureFamily.equals(uType.creatureFamily)
                    && other.controller() instanceof CreatureAI otherAI){
                    otherAI.attackTarget = unitAI.combatTarget;
                    if(applyStatus){
                        other.apply(alertStatus, alertStatusDuration);
                    }
                    alerted[0] = true;
                }
            });
        }
        if(alerted[0]){
            alertSound.at(unit.x, unit.y, 1f + Mathf.range(0.1f), alertSoundVolume);
            alertEffect.at(unit.x, unit.y);
            Effect.shake(alertShake, alertShake, unit);
        }
    }
}
