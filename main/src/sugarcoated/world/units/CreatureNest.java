package sugarcoated.world.units;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.entities.units.*;
import mindustry.graphics.*;
import mindustry.world.blocks.units.*;
import sugarcoated.CandyPal;

public class CreatureNest extends UnitFactory {
    public TextureRegion underRegion;
    /** Minimum distance between other nests. This is only used for builder creatures*/
    public float nestRangeLimit = -1f;

    public CreatureNest(String name) {
        super(name);
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        super.drawPlanRegion(plan, list);
    }

    @Override
    public void init(){
        super.init();
        if(nestRangeLimit <= 0){
            nestRangeLimit = (size * 2) * 8;
        }
    }

    @Override
    public void load(){
        super.load();
        underRegion = findFactoryRegion("-under");
    }

    public class CreatureNestBuild extends UnitFactoryBuild {

        @Override
        public void draw(){
            Draw.rect(underRegion, x, y);
            Draw.rect(region, x, y);

            boolean playerTeam = Vars.player != null && team == Vars.player.team();
            if(playerTeam){
                Draw.rect(outRegion, x, y, rotdeg());
            }

            if(currentPlan != -1){
                UnitPlan plan = plans.get(currentPlan);
                if(!playerTeam){
                    Draw.draw(Layer.blockOver, () -> Drawf.construct(this.x, this.y, plan.unit.fullIcon, CandyPal.redMint, rotdeg() - 90f, progress / plan.time, speedScl, time));
                } else {
                    Draw.draw(Layer.blockOver, () -> Drawf.construct(this.x, this.y, plan.unit.fullIcon, Pal.accent, rotdeg() - 90f, progress / plan.time, speedScl, time));
                }
            }

            Draw.z(Layer.blockOver);

            payRotation = rotdeg();
            drawPayload();

            Draw.z(Layer.blockOver + 0.1f);

            Draw.rect(topRegion, x, y);
        }
    }
}
