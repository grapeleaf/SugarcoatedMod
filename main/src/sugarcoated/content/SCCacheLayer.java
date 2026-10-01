package sugarcoated.content;

import mindustry.graphics.*;

public class SCCacheLayer {
    public static CacheLayer hotChocolate;

    public static void load(){
        CacheLayer.add(
                hotChocolate = new CacheLayer.ShaderLayer(SCShaders.hotChocolate)
        );
    }
}
