package data.scripts.industry;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.plugins.AodCryosleeperPLugin;

public class ReawakeningFacility extends BaseIndustry {
    @Override
    public boolean showShutDown() {
        return !isReawakening;
    }


    public SectorEntityToken tiedEntity = null;

    public float interval = 0f;
    public boolean isReawakening = false;
    public boolean metDemand = true;

    public void setReawakening(boolean reawakening) {
        this.isReawakening = reawakening;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    public boolean canImprove() {
        return false;
    }

    @Override
    public void apply() {
        super.apply(true);
        demand(Commodities.ORGANICS, 8);
        if (getMaxDeficit(Commodities.ORGANICS).two != 0) {
            metDemand = true;
        } else {
            metDemand = false;
        }
    }

    @Override
    public void unapply() {
        super.unapply();
    }

    @Override
    public void finishBuildingOrUpgrading() {
        building = false;
        buildProgress = 0;
        buildTime = 1f;

        buildingFinished();
        reapply();

    }

    @Override
    protected void sendBuildOrUpgradeMessage() {

        if (market.isPlayerOwned()) {
            if (isReawakening) {
                isReawakening = false;
                int size = 7;
                if (tiedEntity.getCustomEntityType().equals("ark")) {
                   size= (int) AodCryosleeperPLugin.sizeArk;
                }
                if (tiedEntity.getCustomEntityType().equals("derelict_cryosleeper")) {
                   size = (int) AodCryosleeperPLugin.sizeCryosleeper;
                }
                grow(size);
                MessageIntel intel = new MessageIntel(getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
                intel.addLine(BaseIntelPlugin.BULLET + "Re-awakening procedure finished!");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
            } else {
                MessageIntel intel = new MessageIntel(getCurrentName() + " at " + market.getName(), Misc.getBasePlayerColor());
                intel.addLine(BaseIntelPlugin.BULLET + "Construction completed");
                intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
            }

        }
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
        super.addPostDemandSection(tooltip, hasDemand, mode);
        if (isReawakening) {
            tooltip.addPara("Re-awakening procedure has started", Misc.getPositiveHighlightColor(), 10f);
            //brb
        } else {
            tooltip.addPara("This facility will be used to safely re-awaken people from cryosleep", Misc.getTooltipTitleAndLightHighlightColor(), 10f);

        }
    }

    @Override
    public void notifyBeingRemoved(MarketAPI.MarketInteractionMode mode, boolean forUpgrade) {
        if(tiedEntity!=null){
            tiedEntity.getMemoryWithoutUpdate().unset("$reawakening");

        }
        super.notifyBeingRemoved(mode, forUpgrade);
    }

    @Override
    public String getBuildOrUpgradeProgressText() {
        if (isUpgrading()) {
            return "Re - awakening process " + this.getBuildOrUpgradeDaysText();
            //return "" + (int) Math.round(Misc.getMarketSizeProgress(market) * 100f) + "%";

        }
        return super.getBuildOrUpgradeProgressText();
    }

    @Override
    public boolean isAvailableToBuild() {
        if (market.getPrimaryEntity().getStarSystem() == null) return false;
        boolean haveCryosleeper = false;
        for (SectorEntityToken aotd_cryosleeper : market.getStarSystem().getEntitiesWithTag("aotd_cryosleeper")) {
            if(aotd_cryosleeper.getMemory().is("$inTransit",true))continue;
            if (aotd_cryosleeper.getMemory().is("$reawakening", false)||!aotd_cryosleeper.getMemory().contains("$reawakening")) {
                if (aotd_cryosleeper.getMemoryWithoutUpdate().is("$defenderFleetDefeated", true)) {
                    haveCryosleeper = true;
                    break;
                }

            }
        }
        return haveCryosleeper;
    }

    @Override
    public boolean showWhenUnavailable() {
        if (market.getPrimaryEntity().getStarSystem() == null) return false;
        boolean haveCryosleeper = false;
        for (SectorEntityToken aotd_cryosleeper : market.getStarSystem().getEntitiesWithTag("aotd_cryosleeper")) {
            if(aotd_cryosleeper.getMemory().is("$inTransit",true))continue;
            if (aotd_cryosleeper.getMemory().is("$reawakening", false)||!aotd_cryosleeper.getMemory().contains("$reawakening")) {
                if (aotd_cryosleeper.getMemoryWithoutUpdate().is("$defenderFleetDefeated", true)) {
                    haveCryosleeper = true;
                    break;
                }

            }
        }
        return haveCryosleeper;
    }

    private void grow(int size) {
        if (market.getSize() < size) {
            market.removeCondition("population_" + market.getSize());
            market.setSize((int) size);
            market.addCondition("population_" + market.getSize());
            market.getPopulation().setWeight(0.1f);
            Misc.fadeAndExpire(tiedEntity, 1f);
            tiedEntity = null;
        }

    }
}
