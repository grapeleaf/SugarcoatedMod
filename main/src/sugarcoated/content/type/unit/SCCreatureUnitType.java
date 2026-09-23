package sugarcoated.content.type.unit;

public class SCCreatureUnitType extends SCUnitType {
    /** What family this creature belongs to */
    public String creatureFamily = "none";
    /** Radius for alerting other units of the same family*/
    public float alertRadius = 64f;

    /** Whether this unit should strafe around targets when attacking*/
    public boolean strafeTarget = true;
    /** Min strafe time */
    public float strafeTimeMin = 30f,
    /** Max strafe time */
    strafeTimeMax = 90f;

    /** Min wander time */
    public float wanderTimeMin = 120f,
    /** Max wander time */
    wanderTimeMax = 300f,
    /** Max possible wandering range */
    wanderRange = 100f,
    /** Max distance from home until this creature wants to return */
    homeReturnRange = 300f;

    public SCCreatureUnitType(String name) {
        super(name);
    }
}
