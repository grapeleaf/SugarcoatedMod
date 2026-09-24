//package sugarcoated.entities.abilities;
//
//import arc.math.*;
//import arc.util.*;
//import mindustry.entities.*;
//import mindustry.entities.abilities.*;
//import mindustry.gen.*;
//
//public class SCRepairFieldAbility extends RepairFieldAbility {
//    /** Whether this ability can only heal units of the same family. Used for creatures.*/
//    public boolean healOnlyFamily = true;
//
//    @Override
//    public void update(Unit unit){
//        healTimer += Time.delta;
//        downTimer = smartHeal && healthChange >= healthMissing && healthMissing > 0f ? downTimer + Time.delta : 0f;
//
//        if(healTimer >= reload){
//            targets.clear();
//            hasHealed = healNow = false;
//            healthChange = healthMissing;
//            healthMissing = sumMaxHealth = sumTypeMult = 0f;
//
//            boolean limitTargets = maxTargets >= 0;
//            float healPercentMult = healPercent / 100f;
//
//
//            Units.nearby(unit.team, unit.x, unit.y, range, other -> {
//                //check for 2 more targets just in case
//                if(limitTargets && targets.size >= maxTargets + 2) return;
//
//                if(other.damaged()){
//                    targets.add(other);
//                    if(smartHeal){
//                        float maxHealth = other.maxHealth();
//                        healthMissing += maxHealth - other.health();
//                        sumMaxHealth += maxHealth;
//                        sumTypeMult += unit.type == other.type ? sameTypeHealMult : 1f;
//                        if(other.healthf() < smartHealPercent) healNow = true;
//                    }
//                }
//            });
//            int targetCount = targets.size;
//
//            //mixed approach, care both about groups and single low hp units
//            float ratio = amount + healPercentMult * sumMaxHealth * (sumTypeMult / targetCount);
//            float requiredHeals = (healthMissing * 0.7f + healthMissing / (limitTargets ? maxTargets : targetCount) * 0.3f) / smartHealStrength / ratio;
//
//            if(requiredHeals >= 1f || !smartHeal || healNow || downTimer >= smartDowntime){
//
//                //sort closest if number of targets is limited
//                if(limitTargets){
//                    boolean isSameType = sameTypeHealMult < 1f;
//                    targets.sort(u -> u.dst2(unit.x, unit.y) + (isSameType && u.type() == unit.type ? 6400f : 0f));
//                }
//
//                int len = limitTargets ? Math.min(targetCount, maxTargets) : targetCount;
//                for(int i = 0; i < len; i++){
//                    Unit other = targets.get(i);
//                    if(other.damaged()){
//                        float maxHealth = other.maxHealth();
//                        float healMult = unit.type == other.type ? sameTypeHealMult : 1f;
//                        other.heal((amount + healPercentMult * maxHealth) * healMult);
//                        healEffect.at(other, parentizeEffects);
//                        hasHealed = true;
//                    }
//                }
//                if(hasHealed){
//                    healTimer = 0f;
//                    activeEffect.at(unit, range);
//                    sound.at(unit, 1f + Mathf.range(0.1f), soundVolume);
//                }
//
//                //increase how often this checks if there are damaged units but still below the healing threshold
//            }else if(smartHeal && targetCount > 0){
//                healTimer = reload >= (2f * smartInterval) ? reload - smartInterval : smartInterval;
//            }else if(randDesync > 0){
//                healTimer = Mathf.random(randDesync) * reload;
//            }else{
//                healTimer = 0;
//            }
//        }
//    }
//}
