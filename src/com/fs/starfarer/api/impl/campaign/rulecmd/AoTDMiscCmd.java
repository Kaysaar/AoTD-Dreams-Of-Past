package com.fs.starfarer.api.impl.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.AoTDCryosleeperIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.CryosleeperIntel;
import com.fs.starfarer.api.impl.campaign.intel.misc.HypershuntIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MiscCMD;
import com.fs.starfarer.api.impl.campaign.world.NamelessRock;
import com.fs.starfarer.api.impl.combat.threat.DisposableThreatFleetManager;
import com.fs.starfarer.api.impl.combat.threat.ThreatFleetBehaviorScript;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class AoTDMiscCmd extends MiscCMD {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        entity = dialog.getInteractionTarget();
        init(entity);

        memory = getEntityMemory(memoryMap);

        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        if (command.equals("addCryosleeperIntel")) {
            if (CryosleeperIntel.getCryosleeperIntel(entity) == null) {
                new AoTDCryosleeperIntel(entity, dialog.getTextPanel());
            }
        }

        return true;
    }
}
