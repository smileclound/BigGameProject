package mygame.states;

import com.jme3.animation.LoopMode;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.cinematic.Cinematic;
import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.events.AbstractCinematicEvent;
import com.jme3.cinematic.events.CinematicEvent;
import com.jme3.cinematic.events.CinematicEventListener;
import com.jme3.cinematic.events.MotionTrack;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import mygame.Game;

/**
 * AppState that managers the intro when starting a new game
 * The player flies in his ship and gets hit by a rock and crashes on the island
 *
 * @author wasd
 */
public class IntroCinematicAppState extends AbstractAppState{
    
    private Game app;
    private Node stateNode = new Node("IntroCinematic Root Node");
    private InGameAppState gameState;
    
    private Spatial player;
    private Spatial ship;
    private Spatial rock;
    private Node shipNode;
    private CameraNode camNode;
    
    private ParticleEmitter fire;

    public IntroCinematicAppState(Game app, InGameAppState gameState) {
        this.app = app;
        this.gameState = gameState;
        this.player = app.getResourceLoader().getPlayerModel();
        this.rock = app.getResourceLoader().getRockModel();
        this.ship = app.getResourceLoader().getShipModel();
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app=(Game) app;
        
        this.app.enableSpaceBox(true);
        
        rock.setLocalScale(2f);
        ship.setLocalTranslation(-2, 0, 0);
        
        shipNode = new Node("Ship and Player");
        shipNode.attachChild(player);
        shipNode.attachChild(ship);
        
        stateNode.attachChild(shipNode);
        stateNode.attachChild(rock);
        
        Cinematic cinematic = new Cinematic(stateNode, 14f, LoopMode.DontLoop);
        CinematicEventListener cel = new CinematicEventListener() {

            public void onPlay(CinematicEvent cinematic) {}

            public void onPause(CinematicEvent cinematic) {}

            public void onStop(CinematicEvent cinematic) {
                cinematicEnded();
            }
        };
        cinematic.addListener(cel);
        
        cinematic.addCinematicEvent(0f, getShipTrack());
        cinematic.addCinematicEvent(0f, getCameraTrack());
        cinematic.addCinematicEvent(0f, getRockTrack());
        cinematic.addCinematicEvent(7.6f, new AbstractCinematicEvent() {
            @Override
            protected void onPlay() {
                shipNode.attachChild(fire);
            }

            @Override
            protected void onUpdate(float tpf) {
                shipNode.rotate(tpf, tpf*2, tpf);
            }
            @Override
            protected void onStop() {}
            @Override
            public void onPause() {}
        });
        
        this.app.getRootNode().attachChild(stateNode);
        this.app.getStateManager().attach(cinematic);
        initLight();
        initFire();
        cinematic.play();
    }
    
    /**
     * @return The MotionTrack of the ship with the player
     */
    private MotionTrack getShipTrack(){
        MotionPath path = new MotionPath();
        path.addWayPoint(new Vector3f(200, 0, -30));
        path.addWayPoint(new Vector3f(10, 0, -30));
        path.addWayPoint(new Vector3f(-10, -300, -110));
        path.setCurveTension(.1f);
        path.enableDebugShape(app.getAssetManager(), stateNode);
        
        //TODO use a node that contains player and the ship
        MotionTrack track = new MotionTrack(shipNode, path);
        track.setSpeed(.9f);
        
        return track;
    }
    
    /**
     * @return The MotionTrack of the camera
     */
    private MotionTrack getCameraTrack(){
        //TODO MotionTrack for the camera and make it always lookAt player
        MotionPath path = new MotionPath();
        path.addWayPoint(new Vector3f(190, 0, 10));
        path.addWayPoint(new Vector3f(100, 5, 10));
        path.addWayPoint(new Vector3f(20, 0, 10));
        path.setCurveTension(1);
        
        camNode = new CameraNode("Motion Camera", app.getCamera());
        camNode.setControlDir(ControlDirection.SpatialToCamera);
        stateNode.attachChild(camNode);
        
        MotionTrack track = new MotionTrack(camNode, path);
        track.setSpeed(1.4f);
        return track;
    }
    
    /**
     * @return The path of the rock that crashes with the ship.
     */
    private MotionTrack getRockTrack(){
        MotionPath path = new MotionPath();
        path.addWayPoint(new Vector3f(110, 100, -30));
        path.addWayPoint(new Vector3f(-90, -100, -30));
        path.enableDebugShape(app.getAssetManager(), stateNode);

        MotionTrack track = new MotionTrack(rock, path);
        return track;
    }
    
    private void initLight(){
        // sunset light
        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.1f,-0.7f,1).normalizeLocal());
        dl.setColor(new ColorRGBA(0.44f, 0.30f, 0.20f, 1.0f));
        stateNode.addLight(dl);

        // skylight
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-0.6f,-1,-0.6f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.10f, 0.22f, 0.44f, 1.0f));
        stateNode.addLight(dl);

        // white ambient light
        dl = new DirectionalLight();
        dl.setDirection(new Vector3f(1, -0.5f,-0.1f).normalizeLocal());
        dl.setColor(new ColorRGBA(0.80f, 0.70f, 0.80f, 1.0f));
        stateNode.addLight(dl);
    }
    
    private void initFire(){
        fire = new ParticleEmitter("Fire emitter", ParticleMesh.Type.Triangle, 50);
        Material mat_red = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
        mat_red.setTexture("Texture", app.getAssetManager().loadTexture("Textures/flame.png"));
        fire.setMaterial(mat_red);
        fire.setImagesX(2); 
        fire.setImagesY(2);
        fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fire.setStartSize(3.5f);
        fire.setEndSize(0.5f);
        fire.setLowLife(1f);
        fire.setHighLife(3f);
        fire.getParticleInfluencer().setVelocityVariation(1f);
        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.5f));
    }
    
    @Override
    public void update(float tpf) {
        camNode.lookAt(shipNode.getLocalTranslation(), Vector3f.UNIT_Y);
        if(rock!=null)
            rock.rotate(tpf, tpf, tpf);
    }
    
    /**
     * Called when the cinematic has finished
     */
    private void cinematicEnded() {
//        player.setLocalScale(1f);
//        rock.setLocalScale(1f);
        stateNode.detachAllChildren();
        gameState.finishedIntroCinema();
        app.enableSpaceBox(false);
        gameState.show();
        app.getStateManager().detach(this);
    }

}
