package sugarcoated.content.type.unit;

import mindustry.type.*;

public class SCCreatureUnitType extends SCUnitType {
    /** What family this creature belongs to */
    public String creatureFamily = "none";
    /** Radius for alerting other units of the same family*/
    public float alertRadius = 64f;

    //chasing
    /** Whether this creature should chase targets */
    public boolean shouldChase = true;
    /** This creatures max change range, can be overridden by setting a value of >0*/
    public float chaseRange = -1f,
    /** How long this creature will chase for until it loses interest*/
    chaseTimer = 300f,
    /** How long until this creature is able to chase targets again after chaseTimer reaches zero*/
    chaseCooldown = 300f;

    //strafing
    /** Whether this unit should strafe around targets when attacking*/
    public boolean strafeTarget = true;
    /** Min strafe time */
    public float strafeTimeMin = 30f,
    /** Max strafe time */
    strafeTimeMax = 90f,
    /** The angle around the target of which this creature is allowed to strafe across*/
    strafeAngle = 45f,
    /** Max strafing distance for this creature, can be overridden by setting a value of >0*/
    strafeDistMax = -1f,
    /** Strafe offset for this creature (e.g. if this is 24, creature can randomly move 4 tiles closer to the target)*/
    strafeOffs = 24f;

    //fleeing
    /** Determines whether this creature flees on low health or not*/
    public boolean flee = true;
    /** If the creatures health is below this number, it flees. Can be overridden by setting a value of >0*/
    public float fleeHealthThresh = -1,

    //wandering
    /** Min wander time */
    wanderTimeMin = 120f,
    /** Max wander time */
    wanderTimeMax = 300f,
    /** Max possible wandering range */
    wanderRange = 100f,
    /** Max distance from home until this creature wants to return */
    homeReturnRange = 200f;

    public SCCreatureUnitType(String name) {
        super(name);
    }

    @Override
    public void init(){
        super.init();
        chaseCooldown = -chaseCooldown;

        float margin = 4f;
        if(strafeDistMax < 0){
            strafeDistMax = Float.MAX_VALUE;
            for(Weapon weapon : weapons){
                if(!weapon.useAttackRange) continue;

                strafeDistMax = Math.min(range, weapon.range() - margin);
            }
        }

        if(chaseRange < 0){
            chaseRange = Float.MAX_VALUE;
            for(Weapon weapon : weapons){
                if(!weapon.useAttackRange) continue;

                chaseRange = Math.max(range, weapon.range() - margin) * 1.5f;
            }
        }

        if(fleeHealthThresh < 0){
            fleeHealthThresh = health / 3;
        }
    }
}
