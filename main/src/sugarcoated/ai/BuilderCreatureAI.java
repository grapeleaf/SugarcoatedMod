package sugarcoated.ai;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.units.*;
import sugarcoated.ai.state.*;
import sugarcoated.world.blocks.*;
import sugarcoated.world.units.*;

public class BuilderCreatureAI extends CreatureAI{
    protected @Nullable Block chosenBlock;
    protected int buildRotation;

    protected Seq<Building> territoryBuildings = new Seq<>();
    protected float localTerritoryRadius,
    defaultBuildTimer = 5f * 60f,
    buildTimer = 5f * 60f;

    protected @Nullable Vec2 buildPos;
    protected Vec2 expandPos = new Vec2();
    protected Vec2 localTerritoryCenter = new Vec2();

    /** Build cap for nests specifically*/
    protected int nestCap = 1;
    /** Build cap for any building including nests*/
    protected int buildCap = 1;

    //DEBUG
    {
        debugText.add(() -> "territoryBuildings: " + territoryBuildings.size);
        debugText.add(() -> "buildTimer: " + buildTimer);
        debugText.add(() -> "Chosen build: " + (chosenBlock != null ? chosenBlock.name : "null"));
        debugText.add(() -> "Nest cap: " + nestCap);
    }

    @Override
    public void init() {
        super.init();
        nestCap = type.nestCap;

        localTerritoryCenter.set(unit.x, unit.y);
        localTerritoryRadius = type.homeReturnRange;
    }

    @Override
    public void updateState(CreatureState state){
        super.updateState(state);
        switch(state){
            case BUILD -> updateBuild();
            case EXPAND_TERRITORY -> updateExpand();
        }
    }

    @Override
    public void enterState(CreatureState state) {
        super.enterState(state);
        switch(state){
            case BUILD -> {
                resetBuildTime();
                updateTerritory();
            }
            case EXPAND_TERRITORY -> {
                resetBuildTime();
            }
        }
    }

    @Override
    public void exitState(CreatureState state) {
        super.exitState(state);
        if(stateHandler.isState(CreatureState.BUILD)){
            resetBuildTime();
            updateTerritory();
        }
    }

    @Override
    protected CreatureState preferredState() {
        CreatureState preferred = super.preferredState();

        // return home has priority.
        if(preferred == CreatureState.RETURN_HOME){
            return preferred;
        }

        // combat takes priority
        if(combatTarget != null){
            return preferred;
        }

        //stay in build if unit has a buildplan
        if(unit.canBuild() && unit.buildPlan() != null){
            return CreatureState.BUILD;
        }

        if(buildTimer <= 0 && getNestCount() < nestCap && chosenBlock == null && buildPos == null){
            return CreatureState.BUILD;
        }

//        if(getNestCount() >= nestCap){
//            return CreatureState.EXPAND_TERRITORY;
//        }

        return preferred;
    }

    @Override
    protected void updateWander() {
        super.updateWander();
        if(buildTimer >= -1){
            buildTimer -= Time.delta;
        }
    }

    protected void updateExpand(){
    }

    protected void updateBuild(){
        if(!unit.canBuild()) {
            return;
        }

        //check if already have plan
        BuildPlan plan = unit.buildPlan();
        if(plan != null){
            Tile planTile = plan.tile();

            boolean valid =
                (planTile != null && planTile.build instanceof ConstructBlock.ConstructBuild cons
                && cons.current == plan.block) || (!plan.breaking && Build.validPlace(plan.block, unit.team(), plan.x, plan.y, plan.rotation));
            if(!valid){
                unit.plans.removeFirst();
                return;
            }

            unit.updateBuilding = true;
            if(unit.within(plan, type.buildRange)){
                targetPos = null;
                unit.lookAt(plan);
            } else {
                commandPosition(Tmp.v1.set(plan.getX(), plan.getY()), false);
            }
            chosenBlock = null;
            buildPos = null;
            return;
        }

        //choose block to place
        //make this better eventually
        if(chosenBlock == null){
            if(type.builderBlocks.isEmpty()) return;
            chosenBlock = type.builderBlocks.toSeq().random();
        }

        //get possible tile
        if(buildPos == null){
            getBuildTile();
        }

        Tile tile = Vars.world.tileWorld(buildPos.x, buildPos.y);

        //todo: dont forget to actually check for build-to-build distance,
        /* or don't actually, its only to prevent cramming which in some cases i might want*/
        //check if valid tile
        if(tile == null || !Build.validPlace(chosenBlock, unit.team(), tile.x, tile.y, buildRotation)){
            buildPos = null;
            return;
        }

        //add build plan
        //check if the chosen block is a nest
        if(chosenBlock instanceof CreatureNest nest){
            if(nest.plans.isEmpty()){
                chosenBlock = null;
                buildPos = null;
                return;
            }
            int planIndex = Mathf.random(nest.plans.size - 1);

            plan = new BuildPlan(tile.x, tile.y, buildRotation, chosenBlock, planIndex);
        } else {
            plan = new BuildPlan(tile.x, tile.y, buildRotation, chosenBlock);

        }

        unit.updateBuilding = true;
        unit.addBuild(plan);

        chosenBlock = null;
        buildPos = null;
    }

    protected void updateTerritory(){
        territoryBuildings.clear();

        float radius = localTerritoryRadius;

        if(radius <= 0f || unit.team() == null){
            return;
        }

        QuadTree<Building> builds = unit.team().data().buildingTree;

        if(builds == null){
            return;
        }

        Seq<Building> nearby = new Seq<>();
        builds.intersect(localTerritoryCenter.x - radius, localTerritoryCenter.y - radius, radius * 2f, radius * 2f, nearby);

        //get valid creature buildings
        for(Building b : nearby){
            if(!b.isValid()) continue;
            if(!b.within(localTerritoryCenter.x, localTerritoryCenter.y, radius + b.hitSize() / 2)) continue;
            if(!(b instanceof CreatureBuilding creatureBuild && creatureBuild.getFamily().equals(type.creatureFamily))) continue;

            territoryBuildings.add(b);
        }

        //calculate new territory center & radius.
        float sumX = 0f;
        float sumY = 0f;
        float sumRange = 0f;
        int nestCount = 0;

        for(Building building : territoryBuildings){
            //only count nests
            if(!(building.block instanceof CreatureNest nest)) continue;

            sumX += building.x;
            sumY += building.y;
            sumRange += nestCount == 0 ? nest.nestRangeLimit * 1.5f : nest.nestRangeLimit;

            nestCount++;
        }

        //use home as temp territory if no nests
        if(nestCount == 0){
            localTerritoryCenter.set(home.x, home.y);
            localTerritoryRadius = type.homeReturnRange;
            return;
        }

        localTerritoryCenter.set(sumX / nestCount, sumY / nestCount);
        localTerritoryRadius = sumRange;
    }

    protected void resetBuildTime(){
        buildTimer = defaultBuildTimer * type.buildTimerMult;
    }

    protected int getNestCount(){
        return territoryBuildings.count(b -> b.isValid() && b.block instanceof CreatureNest);
    }

    protected void getBuildTile(){
        if(buildPos == null){
            buildPos = new Vec2();
        }

        float angle = Mathf.random(360f);
        float dist = Mathf.sqrt(Mathf.random()) * localTerritoryRadius;

        buildPos.set(
                localTerritoryCenter.x + Mathf.cosDeg(angle) * dist,
                localTerritoryCenter.y + Mathf.sinDeg(angle) * dist
        );
        buildRotation = chosenBlock.rotate ? Mathf.random(3) : 0;
    }

    //todo: fix this when bridged home and territory
    @Override
    protected void wander(Vec2 pos){
        wanderTimer -= Time.delta;

        if(wanderTimer <= 0f){
            float angle = Mathf.random(360f);
            float distance = Mathf.random(type.wanderRange + localTerritoryRadius + 30f);

            wanderTimer = Mathf.random(type.wanderTimeMin, type.wanderTimeMax);
            wanderTarget.set(
                    pos.x + Mathf.cosDeg(angle) * distance,
                    pos.y + Mathf.sinDeg(angle) * distance
            );
            commandPosition(wanderTarget);
        }
    }
    //todo: this too
    @Override
    protected boolean withinHome(){
        return home != null && unit.within(home, localTerritoryRadius + 30f);
    }

    @Override
    public void drawDebug() {
        super.drawDebug();
        //buildPos
        if(buildPos != null){
            Drawf.dashCircle(buildPos.x, buildPos.y, 8f, Pal.accent);
            Drawf.text("BUILD POS", buildPos.x, buildPos.y, Pal.accent);
        }

        //territory
        Drawf.dashCircle(localTerritoryCenter.x, localTerritoryCenter.y, localTerritoryRadius, Color.purple);

        //territory buildings
        for(Building b : territoryBuildings){
            Drawf.dashRect(Color.purple, b.x, b.y, b.hitSize()/2, b.hitSize()/2);
        }
    }
}
