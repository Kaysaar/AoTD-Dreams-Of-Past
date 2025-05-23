package data.scripts.campaign.econ.conditions;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

public class DomainMajority extends BaseMarketConditionPlugin {

    @Override
    public boolean isTransient() {
        return false;
    }

    public void setShouldReduceStability(boolean shouldReduceStability) {
        market.getMemory().set("$aotd_fix_stab",shouldReduceStability);
    }
    public boolean shouldReduce(){
        if(!market.getMemory().contains("$aotd_fix_stab")){
            setShouldReduceStability(false);
        }
        return market.getMemory().getBoolean("$aotd_fix_stab");
    }

    @Override
    public void apply(String id) {
        super.apply(id);
        for (Industry industry : market.getIndustries()) {
            industry.getDemand(Commodities.DRUGS).getQuantity().modifyFlat("aotd_majority_domain",-1,"Domain Majority");
            for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllSupply()) {
                mutableCommodityQuantity.getQuantity().modifyFlat("aotd_majority_domain",1,"Domain Majority");
            }
        }
        if(shouldReduce()){
            market.getStability().modifyFlat("aotd_majority_domain",-market.getSize(),"Domain Majority");
        }
        if(market.hasCondition(Conditions.LUDDIC_MAJORITY)){
            market.removeCondition(Conditions.LUDDIC_MAJORITY);
        }


    }

    @Override
    public void unapply(String id) {
        super.unapply(id);
        for (Industry industry : market.getIndustries()) {
            industry.getDemand(Commodities.DRUGS).getQuantity().unmodifyFlat("aotd_majority_domain");
            for (MutableCommodityQuantity mutableCommodityQuantity : industry.getAllSupply()) {
                mutableCommodityQuantity.getQuantity().unmodifyFlat("aotd_majority_domain");
            }
        }
        market.getStability().unmodifyFlat("aotd_majority_domain");
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        Color[] col = new Color[2];
        col[0] = Misc.getPositiveHighlightColor();
        col[1] = Color.ORANGE;
        tooltip.addPara("%s towards usage of %s", 5f, col,"-1",Global.getSettings().getCommoditySpec(Commodities.DRUGS).getName());
        tooltip.addPara("%s towards production of %s", 5f, col,"+1","all commodities");
        if(shouldReduce()){
            tooltip.addPara("Reduction of stability by %s for the duration of awakening procedure",5f,Misc.getNegativeHighlightColor(),""+market.getSize());
        }
    }
}
