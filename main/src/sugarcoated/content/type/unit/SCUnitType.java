package sugarcoated.content.type.unit;

import mindustry.ai.ControlPathfinder;
import mindustry.ai.Pathfinder;
import mindustry.type.UnitType;

public class SCUnitType extends UnitType {
    public boolean terrainWalk = false;

    public SCUnitType(String name) {
        super(name);
    }

    public void initPathType(){
        if(flowfieldPathType == -1){
            flowfieldPathType =
                    naval ? Pathfinder.costNaval :
                            allowLegStep ? Pathfinder.costLegs :
                                    flying ? Pathfinder.costNone :
                                            hovering ? Pathfinder.costHover :
                                                    Pathfinder.costGround;
        }
        if(pathCost == null){
            pathCost =
                    naval ? ControlPathfinder.costNaval :
                            allowLegStep ? ControlPathfinder.costLegs :
                                    hovering ? ControlPathfinder.costHover :
                                            ControlPathfinder.costGround;
        }

        pathCostId = ControlPathfinder.costTypes.indexOf(pathCost);
        if(pathCostId == -1) pathCostId = 0;
    }

    @Override
    public void init(){
        super.init();

        if(!terrainWalk){
            allowLegStep = false;
            flowfieldPathType = Pathfinder.costGround;
            pathCost = ControlPathfinder.costGround;
            pathCostId = ControlPathfinder.costTypes.indexOf(ControlPathfinder.costGround);
        } else {
            initPathType();
        }
    }
}
