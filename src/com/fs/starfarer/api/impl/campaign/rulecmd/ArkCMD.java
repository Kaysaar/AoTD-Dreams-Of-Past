package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.ArkIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.CryosleeperIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.misc.GateHaulerIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.missions.GateHaulerCMD;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CampaignTerrain;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArkCMD extends BaseCommandPlugin{
    public static int ACTIVATION_COST = 1000;

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected SectorEntityToken stableLocation;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        stableLocation = findNearestStableLocation();

        if (command.equals("addIntel")) {
            if(!entity.getCustomEntityType().equals("ark")){
                Global.getSector().getIntelManager().addIntel(new CryosleeperIntelPlugin(entity), false, text);

            }
            else{
                Global.getSector().getIntelManager().addIntel(new ArkIntelPlugin(entity), false, text);

            }
        } else if (command.equals("handleCost")) {
            printCost();
        } else if (command.equals("removeActivationCosts")) {
            removeActivationCosts();
        } else if (command.equals("canActivate")) {
            return canActivate();
        } else if (command.equals("dissableTransitIfNeeded")) {
            return dissableTransitIfNeeded();
        } else if (command.equals("selectDestination")) {
            selectDestination();
        } else if (command.equals("showInterlalData")) {
            showInternalData();
        } else if (command.equals("activate")) {
            activate();
        } else if (command.equals("canDeploy")) {
            return canDeploy();
        } else if (command.equals("deploy")) {
            deploy();
        }
        return true;
    }

    private boolean dissableTransitIfNeeded() {
        if(entity.getMemory().is("$reawakening",true)){
            dialog.getOptionPanel().setEnabled("cmdTest",false);
        }

        return true;
    }

    public void showInternalData() {
        if(entity.getCustomEntityType().equals("ark")){
            text.addPara("Vessel Designation : %s", Misc.getTooltipTitleAndLightHighlightColor(),Color.ORANGE,"Ark");
            text.addPara("Planned Destination : %s",Misc.getTooltipTitleAndLightHighlightColor(),Color.ORANGE,"Orion Sector");
            text.addPara("Current Location - %s",Misc.getTooltipTitleAndLightHighlightColor(),Misc.getNegativeHighlightColor(),"Perseus Arm");
            text.addPara("Override: Current Location - %s",Misc.getTooltipTitleAndLightHighlightColor(),Misc.getPositiveHighlightColor(),"Persean Sector");

            if(!entity.getMemory().is("$depleted",true)){
                text.addPara("Current Capacity: %s",Color.ORANGE,Misc.getPositiveHighlightColor(),"100 Million People are put into cryosleep");
                text.addPara("Build Cryorevival Facility at one of your colonies to increase its size!",Misc.getPositiveHighlightColor());

            }
            else{
                text.addPara("All cryopods are empty!");

            }

        }
        else{
            text.addPara("Vessel Designation : %s", Misc.getTooltipTitleAndLightHighlightColor(),Color.ORANGE,"Cryosleeper");
            text.addPara("Planned Destination : %s",Misc.getTooltipTitleAndLightHighlightColor(),Color.ORANGE,"Persean Sector");
            text.addPara("Current Location - %s",Misc.getTooltipTitleAndLightHighlightColor(),Misc.getPositiveHighlightColor(),"Persean Sector");
            if(!entity.getMemory().is("$depleted",true)){
                text.addPara("Current Capacity: %s",Color.ORANGE,Misc.getPositiveHighlightColor(),"10 Million People are put into cryosleep");

            }
            else {
                text.addPara("All cryopods are empty!");
            }

        }
        if(entity.getMemory().is("$reawakening",true)){
            text.addPara("Initialized Re-awakening procedure!",Misc.getPositiveHighlightColor());
        }
    }

    public void deploy() {
        if (stableLocation == null) return;

        ArkIntelPlugin intel = ArkIntelPlugin.get(entity);
        if (intel != null) {
            intel.initiateDeployment(stableLocation);
        }
    }

    public boolean canDeploy() {
        ArkIntelPlugin intel = ArkIntelPlugin.get(entity);
        if (intel == null) return false;
        return stableLocation != null;
    }

    public SectorEntityToken findNearestStableLocation() {
        if (entity.getContainingLocation() == null) return null;
        float minDist = Float.MAX_VALUE;
        SectorEntityToken nearest = null;
        for (SectorEntityToken curr : entity.getContainingLocation().getEntitiesWithTag(Tags.STABLE_LOCATION)) {
            float dist = Misc.getDistance(curr, entity);
            if (dist < minDist) {
                minDist = dist;
                nearest = curr;
            }
        }
        return nearest;
    }

    public void activate() {
        if(entity.getCustomEntityType().equals("derelict_cryosleeper")){
            CryosleeperIntelPlugin intel = CryosleeperIntelPlugin.get(this.entity);
            Global.getSector().getPlayerFleet().getCargo().removeCommodity(Commodities.RARE_METALS,100);
            if (intel != null) {
                intel.activate();
            }
        }
        else{
            ArkIntelPlugin intel = ArkIntelPlugin.get(ArkCMD.this.entity);
            Global.getSector().getPlayerFleet().getCargo().removeCommodity(Commodities.RARE_METALS,1000);
            if (intel != null) {
                intel.activate();
            }
        }

    }

    public void printCost() {
        if(entity.getCustomEntityType().equals("ark")){
            text.addPara("Our technicians indicate, to reactive engines of Ark we will need substantial amount of transplutonics.");
            Misc.showCost(text, null, null, getResources(), getQuantities());
            dialog.getOptionPanel().clearOptions();

            dialog.getOptionPanel().addOption("Power up the engines","ark_repaired");
            dialog.getOptionPanel().addOption("Leave the vessel for now.","defaultLeave");
            if (canActivate()) {
                text.addPara("Proceed with reactivation?");

            } else {
                text.addPara("You do not have the necessary resources to reactivate the Ark's engines.");
                dialog.getOptionPanel().setEnabled("ark_repaired",false);
            }
        }
        else{
            text.addPara("Our technicians indicate, to reactive engines of Cryosleeper  we will need substantial amount of transplutonics and metals to restore internal systems.");
            Misc.showCost(text, null, null, getResources(), getQuantities());
            dialog.getOptionPanel().clearOptions();

            dialog.getOptionPanel().addOption("Power up the engines and repair drones","cryosleeper_repaired");
            dialog.getOptionPanel().addOption("Leave the vessel for now.","defaultLeave");
            if (canActivate()) {
                text.addPara("Proceed with operation?");

            } else {
                text.addPara("You do not have the necessary resources to reactivate the Cryosleeper's internal systems.");
                dialog.getOptionPanel().setEnabled("cryosleeper_repaired",false);
            }
        }

    }

    public void removeActivationCosts() {
        CargoAPI cargo = playerCargo;
        String [] res = getResources();
        int [] quantities = getQuantities();
        for (int i = 0; i < res.length; i++) {
            String commodityId = res[i];
            int quantity = quantities[i];
            cargo.removeCommodity(commodityId, quantity);
        }
    }

    public boolean canActivate() {
        if (DebugFlags.OBJECTIVES_DEBUG) {
            return true;
        }

        CargoAPI cargo = playerCargo;
        String [] res = getResources();
        int [] quantities = getQuantities();

        for (int i = 0; i < res.length; i++) {
            String commodityId = res[i];
            int quantity = quantities[i];
            if (quantity > cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, commodityId)) {
                return false;
            }
        }
        return true;
    }

    public String [] getResources() {
        if(entity.getCustomEntityType().equals("ark")){
            return new String[] {Commodities.RARE_METALS};
        }
        return  new String[]{Commodities.RARE_METALS,Commodities.METALS};
    }

    public int [] getQuantities() {
        if(entity.getCustomEntityType().equals("ark")){
            return new int[] {ACTIVATION_COST};
        }
        return new int[] {ACTIVATION_COST/2,ACTIVATION_COST};
    }

    public int getTravelDays(SectorEntityToken entity) {
        if(entity.getId().equals("ark")){
            ArkIntelPlugin intel = ArkIntelPlugin.get(ArkCMD.this.entity);
            if (intel != null) {
                StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                return intel.computeTransitDays(system);
            }
        }
        else{
            CryosleeperIntelPlugin intel = CryosleeperIntelPlugin.get(ArkCMD.this.entity);
            if (intel != null) {
                StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                return intel.computeTransitDays(system);
            }
        }

        return 365;
    }

    public void selectDestination() {
        final ArrayList<SectorEntityToken> systems = new ArrayList<SectorEntityToken>();
        for (StarSystemAPI curr : Global.getSector().getStarSystems()) {
            if (curr == entity.getContainingLocation()) continue;
            if (curr.hasTag(Tags.THEME_HIDDEN) && !"Limbo".equals(curr.getBaseName())) continue;
            if (curr.isDeepSpace()) continue;
            if (curr.getHyperspaceAnchor() == null) continue;
            if (Misc.getStarSystemForAnchor(curr.getHyperspaceAnchor()) == null) continue;
            systems.add(curr.getHyperspaceAnchor());
        }
        String nameOfEntity = "Ark";
        if(!ArkCMD.this.entity.getCustomEntityType().equals("ark")){
            nameOfEntity = "Cryosleeper";
        }
        final String finalNameOfEntity = nameOfEntity;
        dialog.showCampaignEntityPicker("Select destination for "+nameOfEntity, "Destination:", "Execute",
                Global.getSector().getPlayerFaction(), systems,
                new BaseCampaignEntityPickerListener() {
                    public void pickedEntity(SectorEntityToken entity) {
                        dialog.dismiss();
                        Global.getSector().setPaused(false);
                        if(!ArkCMD.this.entity.getCustomEntityType().equals("ark")){
                            CryosleeperIntelPlugin intel = CryosleeperIntelPlugin.get(ArkCMD.this.entity);
                            if (intel != null) {
                                StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                                intel.initiateDeparture(system);
                            }
                        }
                        else{
                            ArkIntelPlugin intel = ArkIntelPlugin.get(ArkCMD.this.entity);
                            if (intel != null) {
                                StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                                intel.initiateDeparture(system);
                            }
                        }

                    }
                    public void cancelledEntityPicking() {

                    }
                    public String getMenuItemNameOverrideFor(SectorEntityToken entity) {
                        StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                        if (system != null) {
                            return system.getNameWithLowercaseTypeShort();
                        }
                        return null;
                    }
                    public String getSelectedTextOverrideFor(SectorEntityToken entity) {
                        StarSystemAPI system = Misc.getStarSystemForAnchor(entity);
                        if (system != null) {
                            return system.getNameWithLowercaseType();
                        }
                        return null;
                    }
                    public void createInfoText(TooltipMakerAPI info, SectorEntityToken entity) {
                        int days = getTravelDays(entity);
                        info.setParaSmallInsignia();
                        String daysStr = "days";
                        if (days == 1) daysStr = "day";
                        info.addPara("    Estimated "+ finalNameOfEntity +" travel time: %s " + daysStr, 0f, Misc.getHighlightColor(), "" + days);
                    }

                    public boolean canConfirmSelection(SectorEntityToken entity) {
                        return true;
                    }
                    public float getFuelColorAlphaMult() {
                        return 0f;
                    }
                    public float getFuelRangeMult() { // just for showing it on the map when picking destination
                        return 0f;
                    }
                });
    }
}
