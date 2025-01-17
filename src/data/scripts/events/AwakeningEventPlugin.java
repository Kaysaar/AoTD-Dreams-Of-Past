package data.scripts.events;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import org.lazywizard.lazylib.MathUtils;

import java.util.LinkedHashMap;

public class AwakeningEventPlugin extends BaseEventIntel {
    public MarketAPI tiedMarket;

    public enum AwakeningType{
        ARK,
        CRYOSLEEPER,
    }
    public AwakeningType type;
    public enum EventType{
        OFFICER,
        ADMIN,
        WEAPON_BP,
        SHIP_BP,
        FIGHTER_BP
        //Rest here will be AoTD direct events
        ,DATABANKS,
        MEGASTRUCTURE_LOCATION,
        INSTANT_RESEARCH
    }

    public LinkedHashMap<String,BaseAwakeningReward>rewards = new LinkedHashMap<>();
    public void setUp(){
        addStage("aotd_cryosleeper_start",0);
        addStage("aotd_cryosleeper_end",1000);
        int min =2;
        int max = 4;
        if(type==AwakeningType.ARK){
            min = 8;
            max = 10;
        }
        int randomEvents = MathUtils.getRandomNumberInRange(min,max);
        int interval =1000/randomEvents;
    }
    public void setSize(){
        tiedMarket.getPopulation().add();
    }
}
