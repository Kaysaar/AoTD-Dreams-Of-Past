package data.scripts.campaign.econ.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomDialogDelegate;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.CustomDialogDelegate;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.BaseIndustryOptionProvider;
import com.fs.starfarer.api.campaign.listeners.DialogCreatorUI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.plugins.AodCryosleeperPLugin;
import data.scripts.industry.ReawakeningFacility;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CryoIndustryOptionsProvider extends BaseIndustryOptionProvider {
    public static Object CRYOSLEEPER = new Object();
    public static Object ARK = new Object();

    @Override
    public List<IndustryOptionData> getIndustryOptions(Industry ind) {
        ArrayList<IndustryOptionData> data = new ArrayList<>();
        if (ind.getId().equals("reawakening_facility")) {
            boolean foundArk = false;
            boolean foundCryosleeper = false;
            for (SectorEntityToken entity : ind.getMarket().getStarSystem().getEntitiesWithTag("aotd_cryosleeper")) {
                if (entity.getMemory().is("$reawakening", true)) continue;
                if(entity.getMemory().is("$inTransit",true))continue;
                if (entity.getMemory().is("$defenderFleetDefeated", true)) {
                    if (entity.getCustomEntityType().equals("ark") && !foundArk) {
                        IndustryOptionData opt;
                        opt = new IndustryOptionData("Start re-awakening process of Ark", ARK, ind, this);
                        opt.color = new Color(0, 144, 246, 255);
                        data.add(opt);
                        foundArk = true;
                    }
                    if (entity.getCustomEntityType().equals("derelict_cryosleeper") && !foundCryosleeper) {
                        IndustryOptionData opt;
                        opt = new IndustryOptionData("Start re-awakening process of Cryosleeper", CRYOSLEEPER, ind, this);
                        opt.color = new Color(0, 98, 246, 255);
                        data.add(opt);
                        foundCryosleeper = true;
                    }
                }

            }
        }
        return data;
    }

    @Override
    public void createTooltip(IndustryOptionData opt, TooltipMakerAPI tooltip, float width) {
        if (opt.id == CRYOSLEEPER) {
            tooltip.addPara("With Cryosleeper being in our solar system, we can start awakening process. Due to capacity of people being in cryosleep, this will make colony to grow to size %s",10f,Misc.getHighlightColor(),""+(int)AodCryosleeperPLugin.sizeCryosleeper);
        }
        if (opt.id == ARK) {
            tooltip.addPara("With Ark being in our solar system, we can start awakening process. Due to capacity of people being in cryosleep, this will make colony to grow to size %s",10f,Misc.getHighlightColor(),""+(int) AodCryosleeperPLugin.sizeArk);
        }
    }

    @Override
    public void optionSelected(final IndustryOptionData opt, DialogCreatorUI ui) {
        if (opt.id == CRYOSLEEPER || opt.id == ARK) {
            final BaseIndustry industry = (BaseIndustry) opt.ind;
            CustomDialogDelegate delegate = new BaseCustomDialogDelegate() {
                @Override
                public void createCustomDialog(CustomPanelAPI panel, CustomDialogCallback callback) {
                    float opad = 10f;
                    Color highlight = Misc.getHighlightColor();
                    TooltipMakerAPI info = panel.createUIElement(600, 100, false);
                    info.setParaInsigniaLarge();
                    info.addSpacer(2f);
                    int days = (int) AodCryosleeperPLugin.AwakeningArk;
                    if (opt.id == CRYOSLEEPER) {
                        days = (int) AodCryosleeperPLugin.AwakeningCryosleeper;
                    }
                    info.addPara("Reawakening process will take %s days to complete", opad, highlight, "" + days);


                    panel.addUIElement(info).inTL(0, 0);
                }

                @Override
                public boolean hasCancelButton() {
                    return true;
                }

                @Override
                public void customDialogConfirm() {
                    for (SectorEntityToken entity : opt.ind.getMarket().getStarSystem().getEntitiesWithTag("aotd_cryosleeper")) {
                        if (entity.getMemory().is("$reawakening", true)) continue;
                        if (entity.getMemory().is("$defenderFleetDefeated", true)) {
                            if (entity.getCustomEntityType().equals("ark") && opt.id == ARK) {
                                initalizeAwakening(entity, (ReawakeningFacility) industry, AodCryosleeperPLugin.AwakeningArk);
                                break;
                            }
                            if (entity.getCustomEntityType().equals("derelict_cryosleeper") && opt.id == CRYOSLEEPER) {
                                initalizeAwakening(entity, (ReawakeningFacility) industry, AodCryosleeperPLugin.AwakeningCryosleeper);
                                break;
                            }
                        }

                    }
                }

                @Override
                public void customDialogCancel() {

                }
            };
            ui.showDialog(600, 80, delegate);
        }
    }

    private void initalizeAwakening(final SectorEntityToken entity, final ReawakeningFacility industry, final float awakeningTime) {
        entity.getMemoryWithoutUpdate().set("$reawakening", true);
        industry.tiedEntity = entity;
        industry.setReawakening(true);
        industry.getSpec().setUpgrade(industry.getId());
        float prev = industry.getSpec().getBuildTime();
        industry.getSpec().setBuildTime(awakeningTime);
        industry.startUpgrading();
        industry.getSpec().setUpgrade(null);
        industry.getSpec().setBuildTime(prev);


    }
}
