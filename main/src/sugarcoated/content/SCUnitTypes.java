package sugarcoated.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.ai.types.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.effect.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.unit.MissileUnitType;
import sugarcoated.CandyPal;
import sugarcoated.ai.*;
import sugarcoated.content.type.unit.*;
import sugarcoated.entities.abilities.*;
import sugarcoated.gen.*;

import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Mathf.rand;
import static mindustry.content.Fx.v;

public class SCUnitTypes {

    //Peppermint Family
    public static UnitType babyPepper, babyMint, sweetMother;

    public static void load(){
        babyPepper = new SCCreatureUnitType("baby-pepper"){{
            controller = unit -> {
                if(unit.team != Vars.state.rules.defaultTeam){
                    return new SCCreatureAI();
                }
                return new CommandAI();
            };
            constructor = LegsUnit::create;

            //Stat
            health = 550;
            armor = 6;

            speed = 1.1f;
            drag = 0.08f;

            hitSize = 12;
            rotateSpeed = 5;

            //Behavior
            creatureFamily = "pepper";
            alertRadius = 16f * 8f;

            terrainWalk = false;

            wanderTimeMin = 90f;
            wanderTimeMax = 300f;
            wanderRange = 170f;

            chaseTimer = 7f * 60f;

            //Visual
            drawCell = false;
            outlineColor = CandyPal.redMintOutline;

            legCount = 6;
            legGroupSize = 2;
            rippleScale = 0.5f;

            legLength = 10;
            legExtension = -1.5f;
            legBaseOffset = 3f;
            legLengthScl = 0.9f;
            legForwardScl = 2f;
            legMoveSpace = 1.1f;
            legStraightLength = 1f;
            legMaxLength = 1f;
            legMinLength = 0.85f;
            legSplashDamage = 0f;
            legSplashRange = 0;
            legStraightness = 0.25f;

            lockLegBase = true;
            legContinuousMove = true;

            stepShake = 0;
            stepSound = Sounds.walkerStepTiny;
            stepSoundVolume = 0.4f;

            shadowElevation = 0.15f;
            hovering = true;

            groundLayer = Layer.legUnit - 1f;

            weapons.addAll(
                new Weapon("baby-pepper-weapon"){{
                    mirror = true;
                    alternate = false;
                    showStatSprite = false;

                    x = 0.2f;
                    y = 0f;
                    baseRotation = -90f;
                    shootCone = 360f;

                    reload = 24;
                    cooldownTime = 40f;
                    heatColor = CandyPal.redMint;

                    shootSound = SCSounds.shootPepper;
                    shootSoundVolume = 0.4f;

                    shoot = new ShootSpread(2,45);

                    bullet = new BasicBulletType(){{
                        shootEffect = Fx.none;
                        smokeEffect = new Effect(10, e -> {
                            color(Color.white, e.color, e.fin());
                            stroke(0.7f + e.fout());
                            Lines.square(e.x, e.y, e.fin() * 5f, 45f);

                            Drawf.light(e.x, e.y, 23f, e.color, e.fout() * 0.7f);
                        });
                        hitEffect = despawnEffect = new ExplosionEffect(){{
                           lifetime = 20f;
                           waveStroke = 0f;
                           waveRad = 0f;
                           smokeSize = 0f;

                           sparks = 6;
                           sparkRad = 20f;
                           sparkLen = 8f;
                           sparkStroke = 2f;

                           sparkColor = CandyPal.redMint;
                        }};
                        hitSound = SCSounds.explosionPepper;
                        despawnSound = SCSounds.explosionPepper;
                        hitSoundVolume = 0.4f;

                        speed = 5f;
                        damage = 10f;
                        lifetime = 25;

                        homingPower = 0.3f;
                        homingDelay = 4f;

                        width = 4f;
                        height = 8f;

                        lightColor = hitColor = CandyPal.redMint;
                        frontColor = Color.white;

                        trailWidth = 1f;
                        trailLength = 7;
                        trailColor = lightColor;

                        shrinkY = 0f;
                        shrinkX = 0f;
                    }};
                }});
        }};

        babyMint = new SCCreatureUnitType("baby-mint"){{
            controller = unit -> {
                if(unit.team != Vars.state.rules.defaultTeam){
                    return new SCHealerCreatureAI();
                }
                return new CommandAI();
            };
            constructor = LegsUnit::create;

            //Stat
            health = 450;
            armor = 4;

            speed = 1f;
            drag = 0.08f;

            hitSize = 12;
            rotateSpeed = 5;

            //Behavior
            creatureFamily = "pepper";
            alertRadius = 20f * 8f;

            terrainWalk = false;

            wanderTimeMin = 120f;
            wanderTimeMax = 350f;
            wanderRange = 120f;

            chaseTimer = 4f * 60f;

            homeReturnRange = 150f;

            //Visual
            drawCell = false;
            outlineColor = CandyPal.greenMintOutline;

            legCount = 6;
            legGroupSize = 2;
            rippleScale = 0.5f;

            legLength = 10;
            legExtension = -1.5f;
            legBaseOffset = 3f;
            legLengthScl = 0.9f;
            legForwardScl = 2f;
            legMoveSpace = 1.1f;
            legStraightLength = 1f;
            legMaxLength = 1f;
            legMinLength = 0.85f;
            legSplashDamage = 0f;
            legSplashRange = 0;
            legStraightness = 0.25f;

            lockLegBase = true;
            legContinuousMove = true;

            stepShake = 0;
            stepSound = Sounds.walkerStepTiny;
            stepSoundVolume = 0.4f;

            shadowElevation = 0.15f;
            hovering = true;

            groundLayer = Layer.legUnit - 1f;

            abilities.add(
                    new RepairFieldAbility(35f, 60f, 40){{
                        sameTypeHealMult = 0.3f;
                        maxTargets = 4;

                        smartHeal = true;
                        smartDowntime = 60 * 4.5f;
                    }}
            );

            weapons.addAll(
                    new Weapon("baby-mint-weapon"){{
                        mirror = true;
                        alternate = false;
                        showStatSprite = false;

                        x = 0.2f;
                        y = 0f;
                        baseRotation = -90f;
                        shootCone = 360f;

                        reload = 35;
                        cooldownTime = 40f;
                        heatColor = CandyPal.greenMint;

                        shootSound = SCSounds.shootPepper;
                        shootSoundVolume = 0.4f;
                        bullet = new BasicBulletType(){{
                            shootEffect = Fx.none;
                            smokeEffect = new Effect(10, e -> {
                                color(Color.white, e.color, e.fin());
                                stroke(0.7f + e.fout());
                                Lines.square(e.x, e.y, e.fin() * 5f, 45f);

                                Drawf.light(e.x, e.y, 23f, e.color, e.fout() * 0.7f);
                            });
                            hitEffect = despawnEffect = new ExplosionEffect(){{
                                lifetime = 20f;
                                waveStroke = 0f;
                                waveRad = 0f;
                                smokeSize = 0f;

                                sparks = 6;
                                sparkRad = 20f;
                                sparkLen = 8f;
                                sparkStroke = 2f;

                                sparkColor = CandyPal.greenMint;
                            }};
                            hitSound = SCSounds.explosionPepper;
                            despawnSound = SCSounds.explosionPepper;
                            hitSoundVolume = 0.4f;

                            speed = 5f;
                            damage = 12f;
                            lifetime = 27f;

                            homingPower = 0.3f;
                            homingDelay = 4f;

                            width = 4f;
                            height = 8f;

                            lightColor = hitColor = CandyPal.greenMint;
                            frontColor = Color.white;

                            trailWidth = 1f;
                            trailLength = 7;
                            trailColor = lightColor;

                            shrinkY = 0f;
                            shrinkX = 0f;
                        }};
                    }}
            );
        }};

        sweetMother = new SCCreatureUnitType("sweet-mother"){{
            controller = unit -> {
                if(unit.team != Vars.state.rules.defaultTeam){
                    return new SCCreatureAI();
                }
                return new CommandAI();
            };
            constructor = LegsUnit::create;

            //Stat
            health = 5000;
            armor = 5;

            speed = 2.3f;
            drag = 0.14f;

            hitSize = 32f;
            rotateSpeed = 8;

            //Behavior
            creatureFamily = "pepper";
            alertRadius = 35f * 8f;

            terrainWalk = true;

            strafeAngle = 90f;
            strafeDistMax = 15f * 8f;
            strafeOffs = 7f * 8f;
            strafeTimeMin = 60f;
            strafeTimeMax = 90f;

            wanderTimeMin = 90f;
            wanderTimeMax = 480;
            wanderRange = 200f;

            chaseTimer = 15f * 60f;

            homeReturnRange = 400f;

            //Visual
            drawCell = false;
            outlineColor = CandyPal.redMintOutline;

            legCount = 6;
            legGroupSize = 2;

            legLength = 75f;
            legExtension = -15f;
            legBaseOffset = 7.5f;
            legLengthScl = 0.95f;
            legForwardScl = 2f;
            legMoveSpace = 0.5f;
            legStraightLength = 0.9f;
            legMaxLength = 1.2f;
            legMinLength = 0.85f;
            legSplashDamage = 70f;
            legSplashRange = 50f;
            legStraightness = 0f;

            lockLegBase = true;
            legContinuousMove = true;

            stepShake = 1.20f;
            stepSound = Sounds.walkerStep;
            stepSoundVolume = 1.3f;

            shadowElevation = 1f;
            hovering = true;

            groundLayer = Layer.legUnit;
            abilities.add(
                new SCSpawnDeathAbility(babyPepper, 8, 80f){{
                    appliedEffect = SCStatusEffects.speedy;
                    effectDuration = 360f;

                    faceOutwards = true;
                    randAmount = 5;
                }},
                new SCSpawnDeathAbility(babyMint, 5, 80f){{
                    appliedEffect = SCStatusEffects.speedy;
                    effectDuration = 360f;

                    faceOutwards = true;
                    randAmount = 3;
                }}
            );

            parts.add(
                new RegionPart("-fang"){{
                    layerOffset = -0.001f;
                    mirror = true;

                    y = 2f;

                    moveRot = 9f;
                    progress = p -> Mathf.absin(Time.time + 12f, 20f, 1f);

                    moves.add(new PartMove(p -> Mathf.absin(Time.time + 14f, 15f, 1f), -1f, -1f, 0f));
                }}
            );

            weapons.addAll(
                new Weapon(){{
                    mirror = true;
                    alternate = false;

                    x = 12f;
                    y = -1f;
                    baseRotation = -90f;
                    shootCone = 190f;

                    reload = 45f;

                    shootSound = SCSounds.shootMagic;
                    shootSoundVolume = 0.7f;
                    shoot = new ShootSpread(4, 7f){{
                        shotDelay = 4f;
                    }};
                    bullet = new BasicBulletType(){{
                        shootEffect = Fx.none;
                        smokeEffect = Fx.none;
                        hitEffect = despawnEffect = new ExplosionEffect(){{
                            lifetime = 22f;
                            waveStroke = 0f;
                            waveRad = 0f;
                            smokeSize = 0f;

                            sparks = 6;
                            sparkRad = 24f;
                            sparkLen = 8f;
                            sparkStroke = 3.5f;

                            sparkColor = CandyPal.redMint;
                        }};
                        hitSound = despawnSound = SCSounds.explosionMagic;

                        speed = 9f;
                        damage = 35f;
                        lifetime = 20f;
                        drag = -0.015f;

                        homingPower = 0.26f;
                        homingDelay = 4f;

                        width = 8f;
                        height = 12f;

                        lightColor = hitColor = CandyPal.redMint;
                        frontColor = Color.white;

                        trailWidth = 2f;
                        trailLength = 12;
                        trailColor = lightColor;

                        shrinkY = 0f;
                        shrinkX = 0f;
                    }};
                }},

                new Weapon(){{
                    useAttackRange = false;

                    x = 0f;
                    y = 0f;

                    reload = 250f;
                    rotate = false;

                    shootY = 17f;
                    shootCone = 45f;

                    shootSound = SCSounds.shootMagicLarge;
                    shootSoundVolume = 0.7f;
                    bullet = new BulletType(){{
                       shootEffect = new Effect(20, e -> {
                           color(Color.white, e.color, e.fin());
                           stroke(0.8f + e.fout());
                           Lines.square(e.x, e.y, e.fin() * 60f, 45f);
                           Drawf.light(e.x, e.y, 23f, e.color, e.fout() * 0.7f);
                       });
                       smokeEffect = new Effect(45f, e -> {
                           rand.setSeed(e.id);
                           for(int i = 0; i < 15; i++){
                             v.trns(e.rotation + rand.range(30f), rand.random(e.finpow() * 40f));
                             e.scaled(e.lifetime * rand.random(0.3f, 1f), b -> {
                                 color(e.color, Pal.lightishGray, b.fin());
                                 Fill.square(e.x +v.x, e.y + v.y, b.fout() * 3.4f + 0.8f, 45f);
                             });
                           }
                       });

                       hitColor = CandyPal.redMint;
                       shake = 4f;
                       speed = 0f;
                       keepVelocity = false;

                       spawnUnit = new MissileUnitType("sweet-mother-missile"){{
                           health = 200;
                           speed = 4f;
                           rotateSpeed = 0f;

                           lifetime = 60f * 1.3f;
                           maxRange = 6f;

                           useUnitCap = false;

                           trailColor = engineColor = CandyPal.redMint;
                           outlineColor = CandyPal.redMintOutline;

                           engineSize = 6f;
                           drawCell = false;

                           deathExplosionEffect = new ExplosionEffect(){{
                               lifetime = 25f;

                               waveLife = 15f;
                               waveStroke = 4f;
                               waveRad = 40f;

                               smokes = 4;
                               smokeSize = 8f;
                               smokeRad = 30f;
                               smokeColor = Color.white;

                               sparks = 7;
                               sparkRad = 40f;
                               sparkLen = 9f;
                               sparkStroke = 4f;

                               waveColor = sparkColor = CandyPal.redMint;
                           }};

                           weapons.add(
                               new Weapon(){{
                                   shootSound = SCSounds.explosionMagicLarge;
                                   shootSoundVolume = 1.5f;

                                   shootCone = 350f;
                                   mirror = false;
                                   reload = 1f;

                                   shootOnDeath = true;
                                   shootOnDeathEffect = Fx.massiveExplosion;


                                   bullet = new ExplosionBulletType(300f, 60f){{
                                       despawnEffect = shootEffect = new WaveEffect(){{
                                           colorFrom = colorTo = CandyPal.redMint;
                                           sizeTo = 40f;
                                           lifetime = 15f;
                                           strokeFrom = 4f;
                                       }};
                                       despawnShake = 7f;

                                       fragBullets = 5;
                                       fragVelocityMin = 0.4f;
                                       fragRandomSpread = 175f;
                                       fragLifeMin = 0.5f;
                                       fragBullet = new BasicBulletType(){{
                                           shootEffect = Fx.none;
                                           smokeEffect = Fx.none;

                                           hitEffect = despawnEffect = new ExplosionEffect(){{
                                               lifetime = 20f;
                                               waveStroke = 0f;
                                               waveRad = 0f;

                                               smokes = 4;
                                               smokeSize = 8f;
                                               smokeRad = 30f;
                                               smokeColor = Color.white;

                                               sparks = 7;
                                               sparkRad = 40f;
                                               sparkLen = 9f;
                                               sparkStroke = 4f;

                                               sparkColor = CandyPal.redMint;
                                           }};

                                           hitSound = despawnSound = SCSounds.explosionMagic;

                                           speed = 4f;
                                           damage = 40f;
                                           lifetime = 40f;

                                           pierceCap = 2;
                                           pierce = true;
                                           pierceBuilding = true;

                                           collidesAir = false;

                                           width = 20f;
                                           height = 20f;

                                           lightColor = hitColor = CandyPal.redMint;
                                           frontColor = Color.white;

                                           trailWidth = 4.5f;
                                           trailLength = 8;
                                           trailColor = lightColor;

                                           shrinkY = 0f;
                                           shrinkX = 0f;
                                       }};
                                   }};
                               }}
                           );
                       }};
                    }};
                }}
            );
        }};
    }
}
