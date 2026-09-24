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
import sugarcoated.ai.state.CreatureState;
import sugarcoated.ai.state.CreatureStateHandler;
import sugarcoated.content.type.unit.*;

public class SCCreatureAI extends CommandAI {
    protected SCCreatureUnitType type;
    protected @Nullable Teamc combatTarget;

    protected Vec2 wanderTarget = new Vec2();
    protected Vec2 strafeTarget = new Vec2();
    protected Vec2 home;

    protected float wanderTimer;
    protected float strafeTimer;
    protected float chaseTimer;

    protected CreatureState state;
    protected CreatureStateHandler stateHandler;

    //DEBUG
    public static boolean debugView = true;

    @Override
    public void unit(Unit unit){
        super.unit(unit);
        type = (SCCreatureUnitType)unit.type;
        home = null;

        wanderTimer = 0f;
        strafeTimer = 0f;
        chaseTimer = type.chaseTimer;

        state = CreatureState.WANDER;
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

        chooseState();
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
                    && other.controller() instanceof SCCreatureAI ai){

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
        }
    }

    public void enterState(CreatureState state){
        switch(state){
            case RETURN_HOME -> clearCombat();

            case CHASE -> {
                if(withinHome() || chaseTimer <= type.chaseCooldown){
                    chaseTimer = type.chaseTimer;
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

    protected void chooseState(){
        //prioritize returning to home on low health
        if(type.flee && unit.health() <= type.fleeHealthThresh && !withinHome()){
            stateHandler.transition(CreatureState.RETURN_HOME);
            return;
        }

        //stay on return home state until home
        if(stateHandler.isState(CreatureState.RETURN_HOME)){
            if(withinHome()){
                stateHandler.transition(CreatureState.WANDER);
            }
            return;
        }

        if(combatTarget != null){
            if(type.strafeTarget && inStrafeRange()){
                stateHandler.transition(CreatureState.STRAFE);
                return;
            }
            if(type.shouldChase && inChaseRange()){
                stateHandler.transition(CreatureState.CHASE);
                return;
            }
        }

        if(!withinHome() && combatTarget == null){
            stateHandler.transition(CreatureState.RETURN_HOME);
            return;
        }

        if(withinHome()){
            stateHandler.transition(CreatureState.WANDER);
        }
    }

    protected void updateWander(){
        wander(home);
    }
    protected void updateChase(){
        attackTarget = combatTarget;
        chaseTimer -= Time.delta;

        if(chaseTimer <= 0f){
            combatTarget = null;
            attackTarget = null;
            targetPos = null;
        }
    }
    protected void updateStrafe(){
        if(combatTarget == null){
            stateHandler.transition(CreatureState.WANDER);
            return;
        }

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
        return home != null && unit.within(home, type.homeReturnRange);
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
        }

        //strafeTarget
        if(stateHandler.isState(CreatureState.STRAFE)){
            Drawf.line(Color.cyan, unit.x, unit.y, strafeTarget.x, strafeTarget.y);
            Drawf.circles(strafeTarget.x, strafeTarget.y, 8f, Color.cyan);
            Drawf.text("STRAFE POS", strafeTarget.x, strafeTarget.y + textMargin, Color.cyan);
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

        //text
        Drawf.text("CurrentState: " + stateHandler.currentState, unit.x + (unit.hitSize / 2), unit.y + unit.hitSize + textMargin, Color.white);
        Drawf.text("ChaseTimer: " + Mathf.round(chaseTimer * 100f) / 100f, unit.x + (unit.hitSize / 2), unit.y + unit.hitSize + textMargin * 2, Color.white);
    }
}
