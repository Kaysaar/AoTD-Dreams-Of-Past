package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.Cryorevival;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.intel.misc.CryosleeperIntel;
import com.fs.starfarer.api.loading.IndustrySpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class AoTDCryosleeperIntel extends CryosleeperIntel {
    public AoTDCryosleeperIntel(SectorEntityToken entity, TextPanelAPI textPanel) {
        super(entity, textPanel);
    }
    @Override
    protected void addPostDescriptionSection(TooltipMakerAPI info, float width, float height, float opad) {
        if (!entity.getMemoryWithoutUpdate().getBoolean("$hasDefenders")) {
            IndustrySpecAPI spec = Global.getSettings().getIndustrySpec(Industries.CRYOREVIVAL);
            info.addPara("Allows one of colonies to grow to size %s",5f, Color.ORANGE,"7");
        }
    }
}
