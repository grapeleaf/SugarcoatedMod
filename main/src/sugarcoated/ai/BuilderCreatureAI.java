package sugarcoated.ai;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.Time;
import mindustry.gen.*;
import sugarcoated.ai.state.CreatureState;
import sugarcoated.world.units.CreatureNest;

public class BuilderCreatureAI extends CreatureAI{
    protected Building chosenNest;
    protected Seq<Building> builtBuildings = new Seq<>();
    protected float localTerritoryRadius;

    protected Vec2 expandPos = new Vec2();

    protected int nestCap = 1;
    protected float nestTimer = 30f * 60f;

    //DEBUG
    {
        debugText.add(() -> "nestTimer: " + nestTimer);
        debugText.add(() -> "Chosen nest: " + chosenNest.block.name);
    }

    @Override
    public void init() {
        super.init();
        nestCap += type.nestCap;
        nestTimer *= type.buildTimerMult;
        localTerritoryRadius = getTerritoryRadius();
    }

    @Override
    public void updateState(CreatureState state){
        super.updateState(state);
        switch(state){
            case BUILD -> updateBuildNest();
            case EXPAND_TERRITORY -> updateExpand();
        }
    }

    @Override
    public void enterState(CreatureState state) {
        super.enterState(state);
        switch(state){
            case BUILD -> {}
            case EXPAND_TERRITORY -> {
                builtBuildings.clear();
            }
        }
    }

    @Override
    protected CreatureState preferredState() {
        CreatureState preferred = super.preferredState();

        // return home has priority.
        if(preferred == CreatureState.RETURN_HOME){
            return preferred;
        }

        if(nestTimer <= 0 && builtBuildings.size < nestCap){
            return CreatureState.BUILD;
        }

        if(builtBuildings.size >= nestCap && combatTarget == null){
            return CreatureState.EXPAND_TERRITORY;
        }
        return preferred;
    }

    @Override
    protected void updateWander() {
        super.updateWander();
        if(nestTimer >= -1){
            nestTimer -= Time.delta;
        }
    }

    protected void updateBuildNest(){
    }

    protected void updateExpand(){
    }

    protected int getNestCount(){
        return builtBuildings.count(b -> b.isValid() && b.block instanceof CreatureNest);
    }

    protected float getTerritoryRadius(){
        return builtBuildings.sumf(b -> b.block instanceof CreatureNest nest ? nest.nestRangeLimit : 0f) * 2f;
    }
}
