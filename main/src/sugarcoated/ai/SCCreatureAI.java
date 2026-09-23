package sugarcoated.ai;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.blocks.defense.turrets.*;
import sugarcoated.content.type.unit.*;

public class SCCreatureAI extends CommandAI {
    protected SCCreatureUnitType type;
    protected @Nullable Teamc combatTarget;

    protected Vec2 home;

    protected float wanderTimer;
    protected float strafeTimer;
    protected float chaseTimer;

    protected Vec2 wanderTarget = new Vec2();
    protected Vec2 strafeTarget = new Vec2();

    @Override
    public void unit(Unit unit){
        super.unit(unit);
        type = (SCCreatureUnitType)unit.type;
        home = null;
        wanderTimer = 0f;
        strafeTimer = 0f;
        chaseTimer = 0f;
    }
    @Override
    public void updateUnit(){
        Log.info(combatTarget);
        if(home == null){
            home = new Vec2(unit.x, unit.y);
        }

        //remove invalid combat target
        if(combatTarget != null && combatTarget instanceof Healthc h && !h.isValid()){
            combatTarget = null;
        }

        //register persistent combatTarget
        if(combatTarget == null){
            combatTarget = target != null ? target : attackTarget;
        }

        if(combatTarget != null){
            //give up chase if too far
            if(!inChaseRange()){
                combatTarget = null;
                return;
            }
            //strafe if within range
            if(type.strafeTarget && inStrafeRange()){
                attackTarget = null;
                strafeTarget();
                return;
            }
            //chase until lost patience
            if(type.shouldChase && inChaseRange()){
                attackTarget = combatTarget;
                chaseTimer -= Time.delta;
                return;
            }
        }

        //cooldown for chasing so it doesnt immediately chase after losing patience
        if(combatTarget == null && chaseTimer <= 0f){
            chaseTimer -= Time.delta;
            if(chaseTimer <= -60f * 5f){
                chaseTimer = type.chaseTimer;
            }
        }

        //strafe around target if within range and is attacking
//        if(type.strafeTarget && target != null && inStrafeRange()){
//            if(target instanceof Unit || target instanceof Building b && b.block instanceof Turret){
//                attackTarget = null;
//                strafeTarget();
//            }
//        }

        //pursue the target for patrol, keeping the current position
        if(hasStance(UnitStance.patrol) && hasStance(UnitStance.pursueTarget) && target != null && attackTarget == null){
            //commanding a target overwrites targetPos, so add it to the queue
            if(targetPos != null){
                commandQueue.add(targetPos.cpy());
            }
            commandTarget(target, false);
        }

        //remove invalid targets
        if(commandQueue.any()){
            commandQueue.removeAll(e -> e instanceof Healthc h && !h.isValid());
        }

        //assign defaults
        if(command == null && unit.type.commands.size > 0){
            command = unit.type.defaultCommand == null ? unit.type.commands.first() : unit.type.defaultCommand;
        }

        //update command controller based on index.
        var curCommand = command;
        if(lastCommand != curCommand){
            lastCommand = curCommand;
            commandController = (curCommand == null ? null : curCommand.controller.get(unit));
        }

        //use the command controller if it is provided, and bail out.
        if(commandController != null){
            if(commandController.unit() != unit) commandController.unit(unit);
            commandController.updateUnit();
        }else{
            defaultBehavior();
            if(shouldBoost() && unit.type.canBoost){
                //auto land when near target
                if((attackTarget != null && unit.within(attackTarget, unit.range())) || (hasStance(UnitStance.patrol) && target != null && unit.within(target, unit.range()))){
                    unit.updateBoosting(false);
                }else{
                    unit.updateBoosting(true, true);
                }
            }else{
                //boosting control is not supported, so just don't.
                unit.updateBoosting(false);
            }
        }

        if(commandController == null){
            wander(home);
        }
    }

    protected boolean inStrafeRange(){
        return combatTarget != null && unit.within(combatTarget, unit.range() + 10f);
    }

    protected boolean inChaseRange(){
        return attackTarget != null && unit.within(combatTarget, unit.range() + type.chaseRange);
    }

    protected void strafeTarget(){
        strafeTimer -= Time.delta;

        if(strafeTimer <= 0f){
            float angle = target.angleTo(unit) + Mathf.range(45f);
            float distance = (type.strafeDistMax - 10f) - (Mathf.random(type.strafeOffs));

            strafeTimer = Mathf.random(type.strafeTimeMin, type.strafeTimeMax);
            strafeTarget.set(
                target.x() + Mathf.cosDeg(angle) * distance,
                target.y() + Mathf.sinDeg(angle) * distance
            );
            if(targetPos == null){
                targetPos = new Vec2();
            }
            targetPos.set(strafeTarget);
        }
    }

    protected void wander(Vec2 pos){
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
