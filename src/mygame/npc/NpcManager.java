package mygame.npc;

import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.ArrayList;
import java.util.List;
import mygame.ResourceLoader;

/**
 * This class should have methods to return NPCs.
 * They will then be loaded from InGameAppState and get added to the world.
 * 
 * The methods will return spatials with CharacterControls attached.
 * Use spatial.getControl(CharacterControl.class) to get the control and add it to the physics.
 * 
 * @author wasd
 */
public class NpcManager {
    
    private AssetManager assetManager;
    private ResourceLoader loader;
    private List<Npc> npcList = new ArrayList<Npc>();

    public NpcManager(AssetManager assetManager, ResourceLoader loader) {
        this.assetManager = assetManager;
        this.loader = loader;
    }
    
    public Node createSandGuy(float x, float y, float z){
        NpcPhysics control = new NpcPhysics(1, 2);
        npcList.add(control);
        Spatial model = loader.getSandGuyModel();
        model.setLocalRotation(new Quaternion().fromAngles(0, FastMath.PI/2, 0)); //fix the rotation
        model.setLocalTranslation(0, -1.95f, -1.6f); //centers the model
        Node node = new Node("SandGuyNode");
        node.attachChild(model);
        node.addControl(control);
        control.setPhysicsLocation(new Vector3f(x, y, z));
        return node;
    }
    
    /**
     * Gets an NPC close to position
     * @param position The position
     * @return The closest Npc
     */
    public Npc getCloseNpc(Vector3f position){
        Npc closest = null;
        float bestDistance = -1f;
        for(Npc npc : npcList){
            
            float distance = npc.getPosition().distance(position);
            if(bestDistance==-1f){
                closest = npc;
                bestDistance = distance;
                continue;
            }
            if(distance<bestDistance){
                closest=npc;
                bestDistance=distance;
            }
        }
        return closest;
    }
    
}
