package sugarcoated;

import arc.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import sugarcoated.ai.SCCreatureAI;
import sugarcoated.annotations.Annotations.*;
import sugarcoated.content.SCBlocks;
import sugarcoated.content.SCItems;
import sugarcoated.content.SCStatusEffects;
import sugarcoated.content.SCUnitTypes;
import sugarcoated.gen.*;

import static mindustry.Vars.*;

/**
 * The mod's main mod class. Contains static references to other modules.
 * @author Avant Team
 */
@LoadRegs("error")// Need this temporarily, so the class gets generated.
@EnsureLoad
public class SugarcoatedMod extends Mod{
    public static boolean tools = false;

    /** Default constructor for Mindustry mod loader to instantiate. */
    public SugarcoatedMod(){
        this(false);
    }

    /**
     * Constructs the mod, and binds some functionality to the game under certain circumstances.
     * @param tools Whether the mod is in an asset-processing context.
     */
    public SugarcoatedMod(boolean tools){
        SugarcoatedMod.tools = tools;

        if(!headless){
            Events.on(FileTreeInitEvent.class, e -> Core.app.post(SCSounds::load));
        }

        Events.run(EventType.Trigger.draw, () -> {
            if(!SCCreatureAI.debugView || headless) return;

            Groups.unit.each(unit -> {
                if(unit.controller() instanceof SCCreatureAI ai){
                    ai.drawDebug();
                }
            });
        });

        Events.on(ContentInitEvent.class, e -> {
            if(!headless){
                Regions.load();
                content.each(content -> {
                    if(isSugarcoated(content) && content instanceof MappableContent mContent){
                        SCContentRegionRegistry.load(mContent);
                    }
                });
            }
        });
    }

    @Override
    public void loadContent(){
        SCSounds.load();

        SCStatusEffects.load();

        SCItems.load();
        SCUnitTypes.load();

        SCBlocks.load();

        //below has to be done after all things are loaded.
        SCEntityMapping.init();
    }

    public static boolean isSugarcoated(Content content){
        return content.minfo.mod != null && content.minfo.mod.name.equals("sugarcoated");
    }
}