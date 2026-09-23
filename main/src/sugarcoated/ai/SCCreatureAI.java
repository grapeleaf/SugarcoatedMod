package sugarcoated.ai;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import sugarcoated.content.type.unit.*;

public class SCCreatureAI extends CommandAI {
    float wanderTimer;
    float strafeTimer;
    Vec2 wanderTarget = new Vec2();
    Vec2 home;

    SCCreatureUnitType type;

    @Override
    public void unit(Unit unit){
        super.unit(unit);
        type = (SCCreatureUnitType)unit.type;
        home = null;
        wanderTimer = 0f;
        strafeTimer = 0f;
    }

    @Override
    public void updateUnit(){
        super.updateUnit();
        if(home == null){
            home = new Vec2(unit.x, unit.y);
        }

        //Alert other units when attacking
        //Disabled temporarily
//        if(target != null){
//            if(type.creatureFamily != null){
//                Units.nearby(unit.team, unit.x, unit.y, type.alertRadius, other -> {
//                    if(other != unit && other.controller() instanceof CommandAI ai){
//                        if(other.type instanceof SCCreatureUnitType otherType && otherType.creatureFamily.equals(type.creatureFamily)){
//                            if(!ai.isAttacking()){
//                                ai.attackTarget = target;
//                            }
//                        }
//                    }
//                });
//            }
//            return;
//        }

        //Strafe around target if attacking
        if(target != null && type.strafeTarget){
            if(target instanceof Unit || target instanceof Building b && b.block instanceof Turret){
                attackTarget = null;
                strafeTarget();
            }
        }

        if(commandController != null) return;
        wander(home);
    }

    public void strafeTarget(){
        strafeTimer -= Time.delta;

        if(strafeTimer <= 0f){
            float angle = target.angleTo(unit) + Mathf.range(45f);
            float distance = type.range - (Mathf.random(6f));

            strafeTimer = Mathf.random(type.strafeTimeMin, type.strafeTimeMax);
            wanderTarget.set(
                target.x() + Mathf.cosDeg(angle) * distance,
                target.y() + Mathf.sinDeg(angle) * distance
            );

            if(targetPos == null){
                targetPos = new Vec2();
            }

            targetPos.set(wanderTarget);
        }
    }

    public void wander(Vec2 pos){
        wanderTimer -= Time.delta;

        if(wanderTimer <= 0f){
            float angle = Mathf.random(360f);
            float distance = Mathf.random(type.wanderRange);

            wanderTimer = Mathf.random(type.wanderTimeMin, type.wanderTimeMax);
            wanderTarget.set(
                pos.x + Mathf.cosDeg(angle) * distance,
                pos.y + Mathf.sinDeg(angle) * distance
            );
            commandPosition(wanderTarget);
        }
    }
}
