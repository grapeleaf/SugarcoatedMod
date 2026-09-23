package sugarcoated.ai;

import arc.graphics.Color;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;
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

    public static boolean debugView = false;
    boolean isStrafing = false;
    boolean isChasing = false;

    @Override
    public void unit(Unit unit){
        super.unit(unit);
        type = (SCCreatureUnitType)unit.type;
        home = null;
        wanderTimer = 0f;
        strafeTimer = 0f;
        chaseTimer = type.chaseTimer;
    }
    @Override
    public void updateUnit(){
//        Log.info(" ");
//        Log.info("CombatTarget: "+combatTarget);
//        Log.info("Target: "+target);
//        Log.info("AttackTarget: "+attackTarget);
//        Log.info("TargetPos: "+targetPos);
//        Log.info(" ");
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
            //alert nearby friendly creatures
            if(type.creatureFamily != null){
                Units.nearby(unit.team, unit.x, unit.y, type.alertRadius, other -> {
                    if(other != unit && other.type instanceof SCCreatureUnitType otherType && otherType.creatureFamily.equals(type.creatureFamily) && other.controller() instanceof SCCreatureAI ai){
                        if(ai.combatTarget == null && attackTarget == null && !isAttacking()){
                            ai.attackTarget = combatTarget;
                        }
                    }
                });
            }
            //give up chase if too far
            if(!inChaseRange()){
                combatTarget = null;
                attackTarget = null;
                targetPos = null;
                //reset chase timer if target escape its radius
                chaseTimer = type.chaseTimer;

                //debug
                isChasing = false;
                isStrafing = false;
            }
            //strafe if within range
            else if(type.strafeTarget && inStrafeRange()){
                attackTarget = null;
                strafeTarget();

                //debug
                isChasing = false;
                isStrafing = true;
            }
            //chase until lost patience
            else if(type.shouldChase){
                attackTarget = combatTarget;
                chaseTimer -= Time.delta;

                //debug
                isChasing = true;
                isStrafing = false;

                if(chaseTimer <= 0f){
                    combatTarget = null;
                    attackTarget = null;
                    targetPos = null;

                    //debug
                    isChasing = false;
                }
            }
        }

        //cooldown for chasing so it doesn't immediately chase after losing patience
        if(chaseTimer <= 0f){
            chaseTimer -= Time.delta;
            if(chaseTimer <= -60f * 5f){
                chaseTimer = type.chaseTimer;
            }
        }

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

        if(commandController == null || combatTarget == null){
            wander(home);
        }
    }

    protected boolean inStrafeRange(){
        return combatTarget != null && unit.within(combatTarget, unit.range() + 10f);
    }

    protected boolean inChaseRange(){
        return combatTarget != null && unit.within(combatTarget, unit.range() + type.chaseRange);
    }

    protected void strafeTarget(){
        strafeTimer -= Time.delta;

        if(strafeTimer <= 0f){
            float angle = combatTarget.angleTo(unit) + Mathf.range(type.strafeAngle);
            float distance = (type.strafeDistMax - 10f) - (Mathf.random(type.strafeOffs));

            strafeTimer = Mathf.random(type.strafeTimeMin, type.strafeTimeMax);
            strafeTarget.set(
                combatTarget.x() + Mathf.cosDeg(angle) * distance,
                    combatTarget.y() + Mathf.sinDeg(angle) * distance
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

    public void drawDebug(){
        if(unit == null || !unit.isAdded()) return;
        float textMargin = 8f;

        //visualize chase range
        Drawf.circles(unit.x, unit.y, unit.range() + type.chaseRange, Color.orange);

        //strafe range
        Drawf.circles(unit.x, unit.y, unit.range() + 10f, Color.red);

        //persistent combat target
        if(combatTarget != null){
            Drawf.line(Pal.remove, unit.x, unit.y, combatTarget.x(), combatTarget.y());
            Drawf.circles(combatTarget.x(), combatTarget.y(), 5f, Pal.remove);
        }

        //current strafe position
        if(isStrafing){
            Drawf.line(Color.cyan, unit.x, unit.y, strafeTarget.x, strafeTarget.y);
            Drawf.circles(strafeTarget.x, strafeTarget.y, 4f, Color.cyan);
        }

        //text
        Drawf.text("ChaseTimer: " + Mathf.round(chaseTimer * 100f) / 100f, unit.x, unit.y + unit.hitSize + textMargin, Color.white);
        Drawf.text("CHASING :" + isChasing, unit.x, unit.y + unit.hitSize + textMargin * 2, Color.white);
        Drawf.text("STRAFING :" + isStrafing, unit.x, unit.y + unit.hitSize + textMargin * 3, Color.white);
    }
}
