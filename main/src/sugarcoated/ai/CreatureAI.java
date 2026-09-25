package sugarcoated.ai;

import arc.func.*;
import arc.graphics.Color;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ai.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import sugarcoated.ai.state.CreatureState;
import sugarcoated.ai.state.CreatureStateHandler;
import sugarcoated.content.type.unit.*;

public class CreatureAI extends CommandAI {
    protected SCCreatureUnitType type;
    protected @Nullable Teamc combatTarget;

    protected Vec2 wanderTarget = new Vec2();
    protected Vec2 strafeTarget = new Vec2();
    protected Vec2 home;

    protected float wanderTimer;
    protected float strafeTimer;
    protected float chaseTimer;
    protected float investigateTimer;

    protected CreatureStateHandler stateHandler;

    //DEBUG
    public static boolean debugView = true;
    @SuppressWarnings("unchecked")
    protected final Seq<Prov<String>> debugText = Seq.with(
            () -> "CurrentState: " + stateHandler.currentState,
            () -> "ChaseTimer: " + Mathf.round(chaseTimer * 100f) / 100f,
            () -> "TargetPos: " + targetPos,
            () -> "CombatTarget: " + combatTarget,
            () -> "AttackTarget: " + attackTarget
    );

    @Override
    public void unit(Unit unit){
        super.unit(unit);
        type = (SCCreatureUnitType)unit.type;
        home = null;

        wanderTimer = 0f;
        strafeTimer = 0f;
        chaseTimer = type.chaseTimer;
        investigateTimer = 0f;

        stateHandler = new CreatureStateHandler(this);
    }

    @Override
    public void updateUnit(){
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

        transitionState();
        stateHandler.update();

        //cooldown for chasing so it doesn't immediately chase after losing patience
        if(chaseTimer <= 0f && chaseTimer > type.chaseCooldown){
            chaseTimer -= Time.delta;
        }

        //pursue the target for patrol, keeping the currentState position
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
    }

    @Override
    public void hit(Bullet bullet){
        super.hit(bullet);
        if(type.creatureFamily != null){
            Units.nearby(unit.team, unit.x, unit.y, type.alertRadius, other -> {
                if(other != unit && other.type instanceof SCCreatureUnitType otherType && otherType.creatureFamily.equals(type.creatureFamily)
                    && other.controller() instanceof CreatureAI ai){

                    if(ai.combatTarget == null && attackTarget == null && !isAttacking()){
                        ai.combatTarget = combatTarget;
                    }
                }
            });
        }
    }

    public void updateState(CreatureState state){
        switch(state){
            case WANDER -> updateWander();
            case CHASE -> updateChase();
            case STRAFE -> updateStrafe();
            case RETURN_HOME -> updateReturnHome();
            case INVESTIGATE -> updateInvestigate();
        }
    }

    public void enterState(CreatureState state){
        switch(state){
            case RETURN_HOME -> clearCombat();

            case CHASE -> {
                lastTargetPos = null;
                if(withinHome() || chaseTimer <= type.chaseCooldown){
                    chaseTimer = type.chaseTimer;
                }
            }

            case INVESTIGATE -> {
                if(investigateTimer <= 0){
                    investigateTimer = 5f * 60f;
                }
            }

            case STRAFE -> strafeTimer = 0f;

            case WANDER -> {
                clearCombat();
                targetPos = null;
            }
        }
    }

    public void exitState(CreatureState state) {
        switch(state){
            case CHASE -> targetPos = null;

            case STRAFE -> {
                targetPos = null;
                strafeTarget = null;
            }

            case RETURN_HOME -> clearCombat();
        }
    }

    protected CreatureState preferredState(){
        //prioritize returning to home on low health
        if(type.flee && unit.health() <= type.fleeHealthThresh && !withinHome()){
            return CreatureState.RETURN_HOME;
        }

        //stay on return home state until home
        if(stateHandler.isState(CreatureState.RETURN_HOME)){
            return withinHome() ? CreatureState.WANDER : CreatureState.RETURN_HOME;
        }

        //combat states
        if(combatTarget != null){
            if(type.strafeTarget && inStrafeRange()){
                return CreatureState.STRAFE;
            }
            if(type.shouldChase && inChaseRange()){
                return CreatureState.CHASE;
            }
        }

        if(!withinHome()){
            return CreatureState.RETURN_HOME;
        }

        return CreatureState.WANDER;
    }

    protected void transitionState(){
        stateHandler.transition(preferredState());
    }

    protected void updateWander(){
        wander(home);
    }

    protected void updateInvestigate(){
        if(lastTargetPos == null) return;
        investigateTimer -= Time.delta;

        wander(lastTargetPos);
    }

    protected void updateChase(){
        chaseTimer -= Time.delta;
        if(chaseTimer <= 0f){
            combatTarget = null;
            attackTarget = null;
            targetPos = null;
            return;
        }
        //if path to combatTarget not obstructed, prefer this path
        if(!ControlPathfinder.isNearObstacle(unit, unit.tileX(), unit.tileY(), combatTarget.tileX(), combatTarget.tileY())){
            attackTarget = combatTarget;
            setupLastPos();
        } else {
            //must mean there is a path but its just obstructed
            if(lastTargetPos != null && targetPos == null){
                moveTo(lastTargetPos, 30f);
                if(unit.within(lastTargetPos.x, lastTargetPos.y, 32f)){
                    chaseTimer -= Time.delta * 2;
                }
            }
            if(targetPos == null){
                targetPos = new Vec2();
                targetPos.set(combatTarget);
            }
        }
    }

    protected void updateStrafe(){
        attackTarget = null;
        strafeTarget();
    }

    protected void updateReturnHome(){
        commandPosition(home);
    }

    protected void clearCombat(){
        attackTarget = null;
        combatTarget = null;
    }

    protected boolean withinHome(){
        return home != null && unit.within(home, type.wanderRange);
    }

    protected boolean inStrafeRange(){
        return combatTarget != null && unit.within(combatTarget, unit.range());
    }

    protected boolean inChaseRange(){
        return combatTarget != null && unit.within(combatTarget, unit.range() + type.chaseRange);
    }

    protected void strafeTarget(){
        strafeTimer -= Time.delta;

        if(strafeTimer <= 0f){
            float angle = combatTarget.angleTo(unit) + Mathf.range(type.strafeAngle);
            float distance = (type.strafeDistMax - 10f) - Mathf.random(type.strafeOffs);
            float maxDistance = type.range - 5f;

            strafeTimer = Mathf.random(type.strafeTimeMin, type.strafeTimeMax);

            if(strafeTarget == null){
                strafeTarget = new Vec2();
            }

            if(unit.health() <= type.health / 2f){
                strafeTarget.set(combatTarget.x() + Mathf.cosDeg(angle) * maxDistance, combatTarget.y() + Mathf.sinDeg(angle) * maxDistance);
            } else {
                strafeTarget.set(combatTarget.x() + Mathf.cosDeg(angle) * distance, combatTarget.y() + Mathf.sinDeg(angle) * distance);
            }

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

        //chase range
        Drawf.circles(unit.x, unit.y, unit.range() + type.chaseRange, Color.orange);

        //strafe range
        Drawf.circles(unit.x, unit.y, unit.range() + 10f, Color.red);

        //persistent combat target
        if(combatTarget != null){
            Drawf.line(Color.red, unit.x, unit.y, combatTarget.x(), combatTarget.y());
            Drawf.circles(combatTarget.x(), combatTarget.y(), 5f, Color.red);
            Drawf.text("COMBAT TARGET", combatTarget.x(), combatTarget.y() + textMargin, Color.red);
        }

        //strafeTarget
        if(stateHandler.isState(CreatureState.STRAFE)){
            Drawf.line(Color.cyan, unit.x, unit.y, strafeTarget.x, strafeTarget.y);
            Drawf.circles(strafeTarget.x, strafeTarget.y, 8f, Color.cyan);
            Drawf.text("STRAFE POS", strafeTarget.x, strafeTarget.y + textMargin, Color.cyan);
        }

        //targetPos
        if(lastTargetPos != null){
            Drawf.dashLine(Color.orange, unit.x, unit.y, lastTargetPos.getX(), lastTargetPos.getY());
            Drawf.circles(lastTargetPos.getX(), lastTargetPos.getY(), 3f, Color.orange);
            Drawf.text("LAST TARGET POS", lastTargetPos.getX(), lastTargetPos.getY() - textMargin * 2, Color.orange);
        }

        //targetPos
        if(targetPos != null){
            Drawf.dashLine(Color.gold, unit.x, unit.y, targetPos.getX(), targetPos.getY());
            Drawf.circles(targetPos.getX(), targetPos.getY(), 5f, Color.gold);
            Drawf.text("TARGET POS", targetPos.getX(), targetPos.getY() - textMargin, Color.gold);
        }


        //homePos
        if(home != null){
            Drawf.line(Color.green, unit.x, unit.y, home.getX(), home.getY());
            Drawf.circles(home.getX(), home.getY(), 5f, Color.green);
            Drawf.dashCircle(home.getX(), home.getY(), type.homeReturnRange, Color.green);
            Drawf.text("HOME", home.getX(), home.getY() + textMargin, Color.green);

        }

        //make this better tbh
        //text
        drawDebugText(debugText);
    }

    protected void drawDebugText(Seq<Prov<String>> values){
        float textX = unit.x + unit.hitSize / 2f;
        float textY = unit.y + unit.hitSize + 8f;
        float spacing = 8f;

        for(Prov<String> value : values){
            Drawf.text(value.get(), textX, textY, Color.white);
            textY += spacing;
        }
    }
}
