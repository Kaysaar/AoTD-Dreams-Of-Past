package data.scripts.skills;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.CharacterStatsSkillEffect;
import com.fs.starfarer.api.characters.MarketSkillEffect;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class AdminManOutOfTime {

        public static class Level1 implements MarketSkillEffect {
            public String getEffectDescription(float level) {
                return "-10% market upkeep";
            }

            public String getEffectPerLevelDescription() {
                return null;
            }

            public ScopeDescription getScopeDescription() {
                return ScopeDescription.GOVERNED_OUTPOST;
            }

            @Override
            public void apply(MarketAPI market, String id, float level) {
                market.getUpkeepMult().modifyMult("aotd_man_out_of_time",0.9f,"Man out of Time");
            }

            @Override
            public void unapply(MarketAPI market, String id) {
                market.getUpkeepMult().unmodifyMult("aotd_man_out_of_time");

            }
        }

    public static class Level2 implements MarketSkillEffect {
        public String getEffectDescription(float level) {
            return "+25% Fleet size";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.GOVERNED_OUTPOST;
        }

        @Override
        public void apply(MarketAPI market, String id, float level) {
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("aotd_man_out_of_time",0.25f,"Man out of Time");
        }

        @Override
        public void unapply(MarketAPI market, String id) {
            market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).unmodifyFlat("aotd_man_out_of_time");

        }
    }
    public static class Level3 implements MarketSkillEffect {
        public String getEffectDescription(float level) {
            return "Removes all pather cells present on this planet as long as market have Ground Defences or Heavy Batteries";
        }

        public String getEffectPerLevelDescription() {
            return null;
        }

        public ScopeDescription getScopeDescription() {
            return ScopeDescription.GOVERNED_OUTPOST;
        }

        @Override
        public void apply(MarketAPI market, String id, float level) {
            if(market.hasIndustry(Industries.GROUNDDEFENSES)||market.hasIndustry(Industries.HEAVYBATTERIES)){
                market.removeCondition(Conditions.PATHER_CELLS);

            }
        }

        @Override
        public void unapply(MarketAPI market, String id) {
        }
    }
}
