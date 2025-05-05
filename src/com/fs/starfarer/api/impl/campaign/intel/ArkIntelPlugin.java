package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ArkEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateHaulerIntel;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateHaulerCMD;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArkIntelPlugin extends BaseIntelPlugin{
    public static float TRANSIT_DAYS_BASE = 100;
    public static float TRANSIT_SPEED_LY_PER_CYCLE = 50;


    public static Object UPDATE_WITNESSED_ARRIVAL = new Object();

    public static enum ArkAction {
        OUTBOUND,
        DEEP_SPACE_TRANSIT,
        INBOUND,
        DEPLOYING,
    }

    public static ArkIntelPlugin get(SectorEntityToken gateHauler) {
        for (IntelInfoPlugin p : Global.getSector().getIntelManager().getIntel(ArkIntelPlugin.class)) {
            if (p instanceof ArkIntelPlugin) {
                ArkIntelPlugin intel = (ArkIntelPlugin) p;
                if (intel.getArk() == gateHauler) {
                    return intel;
                }
            }
        }
        return null;
    }

    protected SectorEntityToken ark;
    protected StarSystemAPI destination;
    protected float departureAngle;
    protected int transitDays;
    protected float elapsedDaysInAction;
    protected SectorEntityToken parkingOrbit;
    protected SectorEntityToken stableLocation;
    protected ArkIntelPlugin.ArkAction action = null;

    //protected CampaignEntityMovementUtil movement;

    public ArkIntelPlugin(SectorEntityToken ark) {
        this.ark = ark;
        Global.getSector().addScript(this);

        //movement = new CampaignEntityMovementUtil(ark, 0.5f, 3f, 5f, 2000f);
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        Global.getSector().removeScript(this);
    }

    public void updateMemoryFlags() {
//		MemoryAPI mem = gateHauler.getMemoryWithoutUpdate();
//		mem.set("$state", state.name());
//		if (state == GateHaulerState.OUTBOUND || state == GateHaulerState.INBOUND) {
//			mem.set("$inTransit", true);
//		} else {
//			mem.unset("$inTransit");
//		}
    }

    public void activate() {
        // don't actually need to do anything; all handled in the entity plugin and rules
    }

    public int computeTransitDays(StarSystemAPI destination) {
        //if (Global.getSettings().isDevMode()) return 1;

        if (destination == null) return 0;

        float dist = Misc.getDistanceLY(ark, destination.getHyperspaceAnchor());
        float transitDays = TRANSIT_DAYS_BASE + dist / TRANSIT_SPEED_LY_PER_CYCLE * 365f;
        return Math.round(transitDays);
    }

    public void initiateDeployment(SectorEntityToken stableLocation) {
        if (stableLocation == null) return;

        setAction(ArkAction.DEPLOYING);
        this.stableLocation = stableLocation;

        getPlugin().getMovement().moveToLocation(stableLocation.getLocation());
        getPlugin().getMovement().setFaceInOppositeDirection(false);
        getPlugin().getMovement().setTurnThenAccelerate(true);
        getPlugin().setLongBurn(false);

        ark.getMemoryWithoutUpdate().set("$deploying", true);


        ark.fadeOutIndicator();
        ark.addTag(Tags.NON_CLICKABLE);
        //gateHauler.addTag(Tags.NO_ENTITY_TOOLTIP);

        stableLocation.fadeOutIndicator();
        stableLocation.addTag(Tags.NON_CLICKABLE);
        stableLocation.addTag(Tags.NO_ENTITY_TOOLTIP);
    }

    public void initiateDeparture(StarSystemAPI destination) {
        if (destination == null || destination == ark.getContainingLocation()) return;

        setAction(ArkIntelPlugin.ArkAction.OUTBOUND);
        if (parkingOrbit != null) {
            ark.getContainingLocation().removeEntity(parkingOrbit);
            parkingOrbit = null;
        }

        transitDays = computeTransitDays(destination);
        this.destination = destination;

        departureAngle = Misc.getAngleInDegrees(ark.getLocationInHyperspace(), destination.getLocation());

        ark.fadeOutIndicator();
        getPlugin().getMovement().moveInDirection(departureAngle);
        getPlugin().getMovement().setFaceInOppositeDirection(false);
        getPlugin().getMovement().setTurnThenAccelerate(true);
        getPlugin().setLongBurn(true);

        ark.getMemoryWithoutUpdate().set("$inTransit", true);
    }

    public void initiateArrival() {
        if (destination == null) return; // something's badly wrong and the gate hauler is probably gone for good

        setAction(ArkAction.INBOUND);
        findParkingOrbit();

        float brakeTime = ArkEntityPlugin.MAX_SPEED / ArkEntityPlugin.ACCELERATION;
        float brakeDist = ArkEntityPlugin.MAX_SPEED * 0.5f * brakeTime;

        Vector2f spawnLoc = Misc.getUnitVectorAtDegreeAngle(departureAngle + 180f);
        Vector2f spawnVel = new Vector2f(spawnLoc);

        spawnVel.scale(ArkEntityPlugin.MAX_SPEED);
        spawnVel.negate();
        spawnLoc.scale(brakeDist * 1f + 4000f);
        Vector2f.add(spawnLoc, parkingOrbit.getLocation(), spawnLoc);

        ark.setExpired(false);
        ark.removeTag(Tags.NON_CLICKABLE);
        ark.removeTag(Tags.FADING_OUT_AND_EXPIRING);
        ark.setAlwaysUseSensorFaderBrightness(null);

        if (!destination.getAllEntities().contains(ark)) {
            destination.addEntity(ark);
        }

        ark.fadeOutIndicator();

        getPlugin().getMovement().setLocation(spawnLoc);
        getPlugin().getMovement().setVelocity(spawnVel);
        getPlugin().getMovement().setFacing(departureAngle + 180f);

        getPlugin().getMovement().moveToLocation(parkingOrbit.getLocation());
        getPlugin().getMovement().setTurnThenAccelerate(true);
        getPlugin().getMovement().setFaceInOppositeDirection(true);
        getPlugin().setLongBurn(true);
    }

    protected void findParkingOrbit() {
        float minDist = 4000f;
        float maxDist = 8000f;
        parkingOrbit = null;
        SectorEntityToken found = null;
        for (SectorEntityToken curr : destination.getEntitiesWithTag(Tags.STABLE_LOCATION)) {
            float dist = curr.getLocation().length();
            if (dist >= minDist && dist <= 8000f) {
                found = curr;
                break;
            }
        }
        if (found == null) {
            for (PlanetAPI curr : destination.getPlanets()) {
                if (curr.isMoon()) continue;
                float dist = curr.getLocation().length();
                if (dist >= minDist && dist <= 8000f) {
                    found = curr;
                    break;
                }
            }
        }

        if (found != null) {
            Vector2f loc = Misc.getPointAtRadius(found.getLocation(), found.getRadius() + 400f);
            parkingOrbit = destination.createToken(loc);
            float orbitRadius = found.getRadius() + 250f;
            float orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f);
            parkingOrbit.setCircularOrbit(found, Misc.random.nextFloat() * 360f, orbitRadius, orbitDays);
        } else {
            List<BaseThemeGenerator.OrbitGap> gaps = BaseThemeGenerator.findGaps(
                    destination.getCenter(), minDist, maxDist, ark.getRadius() + 50f);
            if (!gaps.isEmpty()) {
                BaseThemeGenerator.OrbitGap gap = gaps.get(0);
                float orbitRadius = (gap.start + gap.end) * 0.5f;
                Vector2f loc = Misc.getPointAtRadius(destination.getCenter().getLocation(), orbitRadius);
                parkingOrbit = destination.createToken(loc);

                if (!destination.isNebula()) {
                    float orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f);
                    parkingOrbit.setCircularOrbit(destination.getCenter(), Misc.random.nextFloat() * 360f, orbitRadius, orbitDays);
                }
            }
        }

        if (parkingOrbit == null) {
            float orbitRadius = minDist + (maxDist - minDist) * Misc.random.nextFloat();
            Vector2f loc = Misc.getPointAtRadius(destination.getCenter().getLocation(), orbitRadius);
            parkingOrbit = destination.createToken(loc);

            if (!destination.isNebula()) {
                float orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f);
                parkingOrbit.setCircularOrbit(destination.getCenter(), Misc.random.nextFloat() * 360f, orbitRadius, orbitDays);
            }
        }

        destination.addEntity(parkingOrbit);
    }

    protected void setAction(ArkAction action) {
        this.action = action;
        elapsedDaysInAction = 0f;
    }

    public ArkEntityPlugin getPlugin() {
        return (ArkEntityPlugin) ark.getCustomPlugin();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (action != null) {
            float days = Misc.getDays(amount);
            elapsedDaysInAction += days;

            ark.fadeOutIndicator();
            if (action == ArkIntelPlugin.ArkAction.DEPLOYING && stableLocation != null) {
                stableLocation.fadeOutIndicator();
            }
        }

        //System.out.println("Gate Hauler speed: " + gateHauler.getVelocity().length() + ", loc: " + gateHauler.getLocation());
        //System.out.println("Loc: " + gateHauler.getLocation());
        //System.out.println("Player speed: " + Global.getSector().getPlayerFleet().getVelocity().length());
        if (action == ArkIntelPlugin.ArkAction.OUTBOUND) {
            float speed = ark.getVelocity().length();
            float dist = ark.getLocation().length();
            CampaignFleetAPI pf = Global.getSector().getPlayerFleet();
            boolean nearPlayer = pf != null && ark.isInCurrentLocation() &&
                    Misc.getDistance(pf, ark) < 10000f;
            if (!nearPlayer && elapsedDaysInAction > 20f &&
                    speed >= ArkEntityPlugin.MAX_SPEED * 0.95f && dist > 40000f) {
                Misc.fadeAndExpire(ark);
                setAction(ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT);
                sendUpdateIfPlayerHasIntel(ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT, false);
            }
        }

        if (action == ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT) {
            if (elapsedDaysInAction >= transitDays) {
                initiateArrival();
                sendUpdateIfPlayerHasIntel(ArkIntelPlugin.ArkAction.INBOUND, false);
            }
        }

        if (action == ArkIntelPlugin.ArkAction.INBOUND) {
            getPlugin().getMovement().moveToLocation(parkingOrbit.getLocation());
            float speed = ark.getVelocity().length();
            float dist = Misc.getDistance(parkingOrbit, ark);

            boolean overshot = Misc.isInArc(ark.getFacing(), 270f,
                    ark.getLocation(), parkingOrbit.getLocation());
            if (overshot || dist < 700f) {
                getPlugin().getMovement().setTurnThenAccelerate(false);
                getPlugin().getMovement().setFaceInOppositeDirection(false);
            }
            boolean closeEnough = speed < 20f && dist < 100f + parkingOrbit.getRadius() + ark.getRadius();
            if (dist < 200f + parkingOrbit.getRadius() + ark.getRadius() && elapsedDaysInAction > 30f) {
                closeEnough = true;
            }
            if (closeEnough) {
                setAction(null);
                destination = null;
                ark.fadeInIndicator();
                getPlugin().getMovement().setFaceInOppositeDirection(false);
                getPlugin().setLongBurn(false);
                float orbitAngle = Misc.getAngleInDegrees(parkingOrbit.getLocation(), ark.getLocation());
                float orbitDays = 1000000f;
                ark.setCircularOrbit(parkingOrbit, orbitAngle, dist, orbitDays);

                if (!ark.isInCurrentLocation()) {
                    for (int i = 0; i < 10; i++) {
                        getPlugin().getEngineGlow().showIdling();
                        getPlugin().getEngineGlow().advance(1f);
                    }
                }
                ark.getMemoryWithoutUpdate().unset("$inTransit");

                if (ark.isInCurrentLocation()) {
                    String key = "$witnessedArkArrival";
                    MemoryAPI mem = Global.getSector().getPlayerMemoryWithoutUpdate();
                    if (!mem.getBoolean(key)) {
                        float distToPlayer = Misc.getDistance(Global.getSector().getPlayerFleet(), ark);
                        if (distToPlayer < 2000f) {
                            sendUpdateIfPlayerHasIntel(UPDATE_WITNESSED_ARRIVAL, false);
                            Global.getSector().getPlayerStats().addStoryPoints(1, null, false);
                            mem.set(key, true);
                        }
                    }
                }
            }
        }


        if (action == ArkIntelPlugin.ArkAction.DEPLOYING) {
            if (ark.getOrbit() == null) {
                getPlugin().getMovement().moveToLocation(stableLocation.getLocation());
            }

            if (elapsedDaysInAction > 1f) {
                if (ark.getOrbit() == null) {
                    float speed = ark.getVelocity().length();
                    float dist = Misc.getDistance(stableLocation, ark);

                    if (dist < 1000f) {
                        getPlugin().getMovement().setTurnThenAccelerate(false);
                    }
                    float test = 100f;
                    if (!ark.isInCurrentLocation()) test = 400f;
                    boolean closeEnough = speed < 20f && dist < test + stableLocation.getRadius() + ark.getRadius();
                    if (dist < 500f + stableLocation.getRadius() + ark.getRadius() +
                            (elapsedDaysInAction - 50f) * 50f && elapsedDaysInAction > 50f) {
                        closeEnough = true;
                    }
                    if (closeEnough) {
                        float orbitAngle = Misc.getAngleInDegrees(stableLocation.getLocation(), ark.getLocation());
                        float orbitDays = 1000000f;
                        ark.setCircularOrbit(stableLocation, orbitAngle, dist, orbitDays);
                        elapsedDaysInAction = 0f;
                    }
                } else {
                    // set the orbit and waited a day; deploy
                    setAction(null);

                    addDebrisField();

                    BaseThemeGenerator.EntityLocation loc = new BaseThemeGenerator.EntityLocation();
                    if (stableLocation.getOrbit() != null) {
                        loc.orbit = stableLocation.getOrbit().makeCopy();
                    } else {
                        loc.location = new Vector2f(stableLocation.getLocation());
                    }
                    BaseThemeGenerator.AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(
                            stableLocation.getStarSystem(), loc, Entities.INACTIVE_GATE, Factions.NEUTRAL);

                    ark.getMemoryWithoutUpdate().unset("$deploying");
                    ark.addTag(Tags.NO_ENTITY_TOOLTIP);
                    Misc.fadeAndExpire(ark, 10f);
                    Misc.fadeAndExpire(stableLocation, 10f);
                    endImmediately();

                    if (added.entity != null) {
                        Misc.fadeIn(added.entity, 3f);
                    }
                }
            }
        }
    }


    protected void addDebrisField() {
        if (stableLocation == null) return;

        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                400f, // field radius - should not go above 1000 for performance reasons
                -1f, // density, visual - affects number of debris pieces
                3f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.density = 1f;
        params.baseSalvageXP = (long) 500; // base XP for scavenging in field

        SectorEntityToken debris = (CampaignTerrainAPI) Misc.addDebrisField(
                stableLocation.getContainingLocation(), params, null);

        debris.setDiscoverable(null);
        debris.setDiscoveryXP(null);

        debris.addDropValue(Drops.EXTENDED, 100000);

        debris.getLocation().set(stableLocation.getLocation());
        if (stableLocation.getOrbit() != null) {
            debris.setOrbit(stableLocation.getOrbit().makeCopy());
        }
    }



    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        FactionAPI faction = getFactionForUIColors();
        Color base = faction.getBaseUIColor();
        Color dark = faction.getDarkUIColor();

        float initPad = pad;
        if (mode == ListInfoMode.IN_DESC) initPad = opad;

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        if (isUpdate) {
            if (getListInfoParam() == ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT) {
                info.addPara("Entered open space", tc, initPad);
                String dStr = "days";
                if (transitDays == 1) dStr = "day";
                info.addPara("Estimated %s " + dStr + " to complete transit", initPad, tc,
                        h, "" + transitDays);
                return;
            }
            if (getListInfoParam() == ArkIntelPlugin.ArkAction.INBOUND) {
                info.addPara("Arrived at " + destination.getNameWithLowercaseType(), tc, initPad);
                return;
            }
            if (getListInfoParam() == UPDATE_WITNESSED_ARRIVAL) {
                info.addPara("Witnessed the arrival of the Ark to a star system", tc, initPad);
                return;
            }
        }

        if (mode == ListInfoMode.INTEL) {
            String locStr = ark.getContainingLocation().getNameWithLowercaseTypeShort();
            if (ark.getContainingLocation() != null && ark.getContainingLocation().isDeepSpace()) {
                locStr = "deep space";
            } else if (ark.getContainingLocation() != null) {
                locStr = ark.getContainingLocation().getNameWithLowercaseTypeShort();
            }
            if (getPlugin().isInTransit() && action == ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT) {
                locStr = "transiting deep space";
            }

            info.addPara("Location: " + locStr, tc, initPad);
            initPad = 0f;

            ArkEntityPlugin plugin = getPlugin();
            if (!plugin.isActivated()) {
                info.addPara("Status: dormant", tc, initPad);
            } else if (plugin.isActivating()) {
                info.addPara("Status: activating", tc, initPad);
            } else if (action == null) {
                info.addPara("Status: operational", tc, initPad);
            } else if (action == ArkIntelPlugin.ArkAction.OUTBOUND) {
                info.addPara("Departing current location", tc, initPad);

                String dStr = "days";
                if (transitDays == 1) dStr = "day";
                info.addPara("Estimated %s " + dStr + " for transit", initPad, tc,
                        h, "" + transitDays);
            } else if (action == ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT) {
                String dStr = "days";
                int daysRemaining = (int) Math.round(transitDays - elapsedDaysInAction);
                if (daysRemaining < 1) daysRemaining = 1;
                if (daysRemaining == 1) dStr = "day";
                info.addPara("Estimated %s " + dStr + " to complete transit", initPad, tc,
                        h, "" + daysRemaining);
            } else if (action == ArkIntelPlugin.ArkAction.INBOUND) {
                info.addPara("Arriving at " + destination.getNameWithLowercaseType(), tc, initPad);
            }
        }

//		if (GateEntityPlugin.isScanned(gateHauler)) {
//			info.addPara("Scanned", tc, initPad);
//			initPad = 0f;
//		}

        unindent(info);
    }


    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        String pre = "";
        String post = "";
//		if (mode == ListInfoMode.MESSAGES && !getPlugin().isActivated()) {
//			pre = "Discovered: ";
//		}

        Color c = getTitleColor(mode);
        info.addPara(pre + getName() + post, c, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        if (ark.getCustomInteractionDialogImageVisual() != null) {
            info.addImage(ark.getCustomInteractionDialogImageVisual().getSpriteName(), width, opad);
        }

        Description desc = Global.getSettings().getDescription(ark.getCustomDescriptionId(), Description.Type.CUSTOM);
        info.addPara(desc.getText1(), opad);

        FactionAPI faction = getFactionForUIColors();
        Color base = faction.getBaseUIColor();
        Color dark = faction.getDarkUIColor();

        info.addSectionHeading("Status", base, dark, Alignment.MID, opad);

        ArkEntityPlugin plugin = getPlugin();
        if (!plugin.isActivated()) {;
            info.addPara("The ark is dormant, its systems shut down to conserve power.", opad);
            info.showCost("Resources required to activate:", false, base, dark, opad, getResources(),getQuantities());
        } else if (plugin.isActivating()) {
            info.addPara("The ark is in the process of reactivating its systems and should be operational "
                    + "within a day.", opad);
        } else if (action == null) {
            info.addPara("The ark is operational and ready to travel to another star system or "
                    + "land on another planet to start re-awakening process", opad);
        } else if (action == ArkIntelPlugin.ArkAction.OUTBOUND) {
            info.addPara("The ark is outbound from its current location, "
                    + "heading for open space and accelerating.", opad);

            String dStr = "days";
            if (transitDays == 1) dStr = "day";
            info.addPara("Once it's in open space, it's estimated that it will take %s " + dStr + " until it arrives "
                            + "to its destination, the " + destination.getNameWithLowercaseTypeShort() + ". On arrival, "
                            + "it will take some time to decelerate and attain a parking orbit.", opad,
                    h, "" + transitDays);
        } else if (action == ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT) {
            String dStr = "days";
            int daysRemaining = (int) Math.round(transitDays - elapsedDaysInAction);
            if (daysRemaining < 1) daysRemaining = 1;
            if (daysRemaining == 1) dStr = "day";
            info.addPara("The ark is in transit, in deep space. It's estimated that it will take %s " + dStr + " until it arrives "
                            + "to its destination, the " + destination.getNameWithLowercaseTypeShort() + ". On arrival, "
                            + "it will take some time to decelerate and attain a parking orbit.", opad,
                    h, "" + daysRemaining);
        } else if (action == ArkIntelPlugin.ArkAction.INBOUND) {
            info.addPara("The ark has arrived to the " + destination.getNameWithLowercaseTypeShort() + " "
                    + "and is decelerating in order to attain a parking orbit.", opad);
        } else if (action == ArkIntelPlugin.ArkAction.DEPLOYING) {
            info.addPara("The ark has been given an order to land on planet to start re-awakening process.", opad);
        }


        //addBulletPoints(info, ListInfoMode.IN_DESC);

    }
    public String [] getResources() {
        return new String[] {Commodities.RARE_METALS,Commodities.METALS};
    }

    public int [] getQuantities() {
        return new int[] {500,1000};
    }

    public SectorEntityToken getEntity(){
        return this.getArk();
    }
    public static ArkIntelPlugin getArkIntel(SectorEntityToken entity) {
        for (IntelInfoPlugin intel : Global.getSector().getIntelManager().getIntel(ArkIntelPlugin.class)) {
            if (((ArkIntelPlugin)intel).getEntity() == entity) return (ArkIntelPlugin)intel;
        }
        return null;
    }
    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "gate_hauler");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_GATES);
        tags.add(Tags.INTEL_EXPLORATION);
        return tags;
    }

    public String getSortString() {
        return super.getSortString();
    }


    public String getName() {
        return "Ark";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return ark.getFaction();
        //return super.getFactionForUIColors();
    }

    public String getSmallDescriptionTitle() {
        //return getName() + " - " + gateHauler.getContainingLocation().getNameWithTypeShort();
        return getName();
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (!ark.isAlive() && destination != null) {
            return destination.getCenter();
        }
        return ark;
    }

    @Override
    public String getCommMessageSound() {
        return "ui_discovered_entity";
    }

    public SectorEntityToken getArk() {
        return ark;
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        if (destination == null || action == null) {
            return null;
        }

        boolean showArrow = action ==ArkIntelPlugin.ArkAction.OUTBOUND || action == ArkIntelPlugin.ArkAction.DEEP_SPACE_TRANSIT;
        if (!showArrow) return null;

        if (ark.getContainingLocation() == destination) {
            return null;
        }

        List<ArrowData> result = new ArrayList<ArrowData>();

        ArrowData arrow = new ArrowData(ark, destination.getCenter());
        arrow.color = getFactionForUIColors().getBaseUIColor();
        arrow.width = 20f;
        result.add(arrow);

        return result;
    }

}
