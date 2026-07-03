import me.client.Dark;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
@Mod(modid = "Pelusa Dark Client", version = "b3.0")
public class Main {
    @EventHandler
    public void init(FMLInitializationEvent event) {
    	Dark.instance = new Dark();
    	Dark.instance.init();
    }
}
