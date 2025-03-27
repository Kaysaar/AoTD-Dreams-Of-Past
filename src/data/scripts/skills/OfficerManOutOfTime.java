package data.scripts.skills;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.*;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.regex.Pattern;

public class OfficerManOutOfTime {
    public static class Level1 extends BaseSkillEffectDescription implements ShipSkillEffect {

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.PILOTED_SHIP;
        }

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            float opad = 10f;
            info.addPara("When piloting ship belonging to any %s", 0f, hc, "Domain Battlegroup");
            info.addPara("+25% damage against cruisers", hc, 3f);
            info.addPara("+30% damage against capitals", hc, 3f);


        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            if (stats.getFleetMember() != null) {
                FleetMemberAPI member = stats.getFleetMember();
                if (isBattlegroup(member.getHullSpec().getManufacturer())) {
                    stats.getDamageToCruisers().modifyPercent(id, 25f);
                    stats.getDamageToCapital().modifyPercent(id, 30f);
                } else {
                    stats.getDamageToCruisers().modifyPercent(id, 5f);
                    stats.getDamageToCapital().modifyPercent(id, 5f);
                }
            }
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getDamageToCruisers().unmodifyPercent(id);
            stats.getDamageToCapital().unmodifyPercent(id);
        }
    }

    public static class Level2 extends BaseSkillEffectDescription implements ShipSkillEffect {

        @Override
        public void createCustomDescription(MutableCharacterStatsAPI stats, SkillSpecAPI skill, TooltipMakerAPI info, float width) {
            init(stats, skill);
            float opad = 10f;
            info.addPara("+15% damage against frigates", hc, 3f);
            info.addPara("+20% damage against destroyers", hc, 3f);
            info.addPara("+100% damage against weapons and engines", hc, 3f);
            info.addPara("Piloting any ship, that has not been manufactured by standards of Domain Battlegroups reduces bonuses to %s", 5f, Misc.getNegativeHighlightColor(), "5%");
            CampaignFleetAPI fleet = stats.getFleet();
            if(fleet!=null) {
                for (FleetMemberAPI memberAPI : fleet.getFleetData().getMembersListCopy()) {
                    if(memberAPI.getCaptain()!=null){
                        if(memberAPI.getCaptain().getStats().equals(stats)){
                            if(isBattlegroup(memberAPI.getHullSpec().getManufacturer())){
                                info.addPara("This vessel meets criteria of Domain Battlegroups !",Misc.getPositiveHighlightColor(),5f);
                            }
                        }
                    }
                }
            }

        }

        @Override
        public void apply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id, float level) {
            if (stats.getFleetMember() != null) {
                FleetMemberAPI member = stats.getFleetMember();
                if (isBattlegroup(member.getHullSpec().getManufacturer())) {
                    stats.getDamageToFrigates().modifyPercent(id, 15f);
                    stats.getDamageToDestroyers().modifyPercent(id, 20);
                    stats.getDamageToTargetEnginesMult().modifyPercent(id, 100);
                    stats.getDamageToTargetWeaponsMult().modifyPercent(id, 100);
                } else {
                    stats.getDamageToFrigates().modifyPercent(id, 5);
                    stats.getDamageToDestroyers().modifyPercent(id, 5);
                    stats.getDamageToTargetEnginesMult().modifyPercent(id, 5);
                    stats.getDamageToTargetWeaponsMult().modifyPercent(id, 5);
                }
            }
        }

        @Override
        public void unapply(MutableShipStatsAPI stats, ShipAPI.HullSize hullSize, String id) {
            stats.getDamageToFrigates().unmodifyPercent(id);
            stats.getDamageToDestroyers().unmodifyPercent(id);
            stats.getDamageToTargetEnginesMult().unmodify(id);
            stats.getDamageToTargetWeaponsMult().unmodify(id);
        }
    }

    public static boolean isBattlegroup(String input) {
        // Define the pattern for a Roman numeral followed by "Battlegroup"
        String regex = "^(?i)(M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})) Battlegroup$";
        Pattern pattern = Pattern.compile(regex);

        // Match the input against the pattern
        return pattern.matcher(input).matches();
    }
}
