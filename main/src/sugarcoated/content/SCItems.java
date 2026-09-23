package sugarcoated.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.type.Item;

public class SCItems {
    public static Item
            sugar, cocoa, jelly;

    public static final Seq<Item> sugarcoatedItems = new Seq<>();

    public static void load(){
        sugar = new Item("sugar", Color.valueOf("#ecfdff")){{
           hardness = 1;
           cost = 0.5f;
        }};

        cocoa = new Item("cocoa", Color.valueOf("#c17046")){{
            hardness = 1;
            cost = 0.3f;
        }};

        jelly = new Item("jelly", Color.valueOf("#d085f9")){{
           hardness = 1;
           cost = 0.5f;
        }};

        sugarcoatedItems.addAll(
                sugar, cocoa, jelly
        );
    }
}
