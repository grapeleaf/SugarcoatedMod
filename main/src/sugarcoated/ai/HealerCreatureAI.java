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
    public RepairFieldAbility repairAbility;

    protected @Nullable Unit healTarget;
    protected final Interval healTargetTimer = new Interval();

    {
        debugText.add(() -> "HealTarget: " + healTarget);
    }

    @Override
    public void unit(Unit unit) {
        super.unit(unit);
        repairAbility = (RepairFieldAbility)unit.type.abilities.find(a -> a instanceof RepairFieldAbility);

        healTarget = null;
        healTargetTimer.reset(0,20f);
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
    protected CreatureState preferredState(){
        CreatureState preferred = super.preferredState();

        // return home has priority.
        if(preferred == CreatureState.RETURN_HOME){
            return preferred;
        }

        // only look for heal targets when at home or wandering
        if((withinHome() || stateHandler.isState(CreatureState.WANDER))
            && (combatTarget == null || !isAttacking())
            && healTarget == null && healTargetTimer.get(0,20f)){
            findHealTarget();
        }

        if(healTarget != null){
            return CreatureState.HEAL_ALLY;
        }

        return preferred;
    }

    protected void updateHealAlly(){
        if(healTarget == null || !healTarget.isValid() || !healTarget.damaged()){
            healTarget = null;
            targetPos = null;
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
        //heal target
        if(healTarget != null){
            Drawf.line(Color.lime, unit.x, unit.y, healTarget.x(), healTarget.y());
            Drawf.circles(healTarget.x(), healTarget.y(), 5f, Color.lime);
        }
    }
}
