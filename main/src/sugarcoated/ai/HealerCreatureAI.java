package sugarcoated.ai;

import arc.graphics.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import sugarcoated.ai.state.*;
import sugarcoated.content.type.unit.*;

public class HealerCreatureAI extends CreatureAI {
    protected @Nullable Unit healTarget;
    public RepairFieldAbility repairAbility;

    @Override
    public void unit(Unit unit) {
        super.unit(unit);
        repairAbility = (RepairFieldAbility)unit.type.abilities.find(a -> a instanceof RepairFieldAbility);
    }

    @Override
    public void updateUnit() {
        super.updateUnit();
        Log.info(healTarget);
    }

    @Override
    public void updateState(CreatureState state){
        if(stateHandler.isState(CreatureState.HEAL_ALLY)){
            updateHealAlly();
        }
        super.updateState(state);
    }

    @Override
    public void enterState(CreatureState state){
        super.enterState(state);
        switch(state){
            case CHASE, STRAFE, RETURN_HOME -> healTarget = null;
        }
    }

    @Override
    protected void chooseState(){
        super.chooseState();
        //only look for heal targets if not attacking
        if((combatTarget == null || !isAttacking()) && healTarget == null){
            Log.info("choosing");
            findHealTarget();
        }

        if(healTarget != null){
            stateHandler.transition(CreatureState.HEAL_ALLY);
        }
    }

    protected void updateHealAlly(){
        if(healTarget == null || !healTarget.isValid() || !healTarget.damaged()){
            findHealTarget();
        }

        if(healTarget == null){
            return;
        }

        if(unit.within(healTarget, repairAbility.range)){
            targetPos = null;
            return;
        }

        if(targetPos == null) {
            targetPos = new Vec2();
        }
        attackTarget = null;
        targetPos.set(healTarget);
    }

    protected void findHealTarget(){
        Log.info("finding heal target");
        //clear old target
        healTarget = null;

        Units.nearby(unit.team, unit.x, unit.y, type.range / 1.5f, other -> {
            if(other == unit || !other.damaged()) return;

            if(other.type instanceof SCCreatureUnitType otherType && otherType.creatureFamily != null && otherType.creatureFamily.equals(type.creatureFamily)){
                if(healTarget == null || other.healthf() < healTarget.healthf()){
                    healTarget = other;
                }
            }
        });
    }

    @Override
    public void drawDebug() {
        super.drawDebug();
        //persistent combat target
        if(healTarget != null){
            Drawf.line(Color.lime, unit.x, unit.y, healTarget.x(), healTarget.y());
            Drawf.circles(healTarget.x(), healTarget.y(), 5f, Color.lime);
        }
    }
}
