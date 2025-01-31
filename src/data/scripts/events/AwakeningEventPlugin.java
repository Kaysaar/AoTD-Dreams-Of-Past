package data.scripts.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.comm.CommMessageAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.events.BaseEventPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventFactor;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.EventFactor;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.CampaignClock;
import data.scripts.campaign.econ.conditions.DomainMajority;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class AwakeningEventPlugin extends BaseEventIntel {
    public MarketAPI tiedMarket;
    public IntervalUtil util = new IntervalUtil(CampaignClock.SECONDS_PER_GAME_DAY, CampaignClock.SECONDS_PER_GAME_DAY);

    public enum AwakeningType {
        ARK,
        CRYOSLEEPER,
    }

    boolean showDomainMajority = false;
    public AwakeningType type;
    int awakenedSoFar = 0;
    boolean finishedAwakening = false;
    public enum EventType {
        SHIP_BP,
        ITEM,
        OFFICER,
        ADMIN,
    }
    public LinkedHashMap<String, BaseAwakeningReward> rewards = new LinkedHashMap<>();

    public void setUp() {
        int intervalBetweenEvents = 200;

        int min = 2;
        int max = 2;
        if (type == AwakeningType.ARK) {
            min = 6;
            max = 6;
        }
        int randomEvents = MathUtils.getRandomNumberInRange(min, max);
        int maxEventPoint = (randomEvents + 1) * intervalBetweenEvents;
        this.maxProgress = 1000;
        addStage("aotd_cryosleeper_start", 0);
        addStage("aotd_cryosleeper_end", maxProgress);
        if(type == AwakeningType.CRYOSLEEPER) {
            if(randomEvents >= 2){
                addStage("test_1",MathUtils.getRandomNumberInRange(10,30),StageIconSize.SMALL);
                rewards.put("test_1",new BaseAwakeningReward(generateRandomEventType()));
                addStage("test_2",MathUtils.getRandomNumberInRange(760,850),StageIconSize.SMALL);
                rewards.put("test_2",new BaseAwakeningReward(generateRandomEventType()));
            }
            if(randomEvents>=3){
                addStage("test_3",MathUtils.getRandomNumberInRange(140,170),StageIconSize.SMALL);
                rewards.put("test_3",new BaseAwakeningReward(generateRandomEventType()));
            }
            if(randomEvents>=4){
                addStage("test_4",MathUtils.getRandomNumberInRange(340,370),StageIconSize.SMALL);
                rewards.put("test_4",new BaseAwakeningReward(generateRandomEventType()));
            }

        }
        if(type == AwakeningType.ARK) {
            if(randomEvents>=6){

                addStage("test_2",MathUtils.getRandomNumberInRange(100,120),StageIconSize.SMALL);
                addStage("test_3",MathUtils.getRandomNumberInRange(200,240),StageIconSize.SMALL);
                addStage("test_4",MathUtils.getRandomNumberInRange(350,450),StageIconSize.SMALL);
                addStage("test_5",MathUtils.getRandomNumberInRange(520,550),StageIconSize.SMALL);
                addStage("test_6",MathUtils.getRandomNumberInRange(620,650),StageIconSize.SMALL);
                addStage("test_1",MathUtils.getRandomNumberInRange(740,870),StageIconSize.SMALL);

                rewards.put("test_2",new BaseAwakeningReward(generateRandomEventType()));
                rewards.put("test_3",new BaseAwakeningReward(generateRandomEventType()));
                rewards.put("test_4",new BaseAwakeningReward(generateRandomEventType()));
                rewards.put("test_5",new BaseAwakeningReward(generateRandomEventType()));
                rewards.put("test_6",new BaseAwakeningReward(generateRandomEventType()));
                rewards.put("test_1",new BaseAwakeningReward(generateRandomEventType()));
            }
            if(randomEvents>=7){
                addStage("test_7",MathUtils.getRandomNumberInRange(720,780),StageIconSize.SMALL);
                rewards.put("test_7",new BaseAwakeningReward(generateRandomEventType()));


            }
            if(randomEvents>=8){
                addStage("test_8",MathUtils.getRandomNumberInRange(840,870),StageIconSize.SMALL);
                rewards.put("test_7",new BaseAwakeningReward(generateRandomEventType()));

            }
        }

    }

    public int getProgressFromAwakened(int awakenedSoFar){
        return (int)((float)((float)awakenedSoFar/(float)getPopulationToAwaken())*getMaxProgress());
    }
    public AwakeningEventPlugin(MarketAPI market, AwakeningType type) {
        super();
        this.tiedMarket = market;
        this.type = type;

        //Global.getSector().getEconomy().addUpdateListener(this);
        setUp();

        // now that the event is fully constructed, add it and send notification
        Global.getSector().getIntelManager().addIntel(this);
    }

    @Override
    protected String getName() {
        return "Awakening Procedure - " + tiedMarket.getName();
    }

    @Override
    protected String getStageIcon(Object stageId) {
        return Global.getSettings().getSpriteName("intel", "reawakening_event");
    }

    @Override
    public int getMonthlyProgress() {
        return 0;
    }

    @Override
    public TooltipMakerAPI.TooltipCreator getBarTooltip() {
        return new TooltipMakerAPI.TooltipCreator() {
            public boolean isTooltipExpandable(Object tooltipParam) {
                return false;
            }

            public float getTooltipWidth(Object tooltipParam) {
                return 450;
            }

            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                float opad = 10f;
                Color h = Misc.getHighlightColor();

                tooltip.addPara("Daily progress %s ", 0f, h, "" + progress, "" + maxProgress);
                int p = getMonthlyProgress();
                String pStr = "" + p;
                if (p > 0) pStr = "+" + p;

                tooltip.addPara("Event progress is influenced by various factors. Some of these apply over time, "
                        + "and some only apply once. As the event progresses, "
                        + "different stages and outcomes may unfold.", opad);
            }
        };
    }


    @Override
    public void addStageDescriptionWithImage(TooltipMakerAPI main, Object stageId) {
        super.addStageDescriptionWithImage(main, stageId);
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {

        float opad = 10f;
        uiWidth = width;

        TooltipMakerAPI main = panel.createUIElement(width, height, true);

        main.setTitleOrbitronVeryLarge();
        main.addTitle(getName(), Misc.getBasePlayerColor());

        EventProgressBarAPI bar = main.addEventProgressBar(this, 100f);
        TooltipMakerAPI.TooltipCreator barTC = getBarTooltip();
        if (barTC != null) {
            main.addTooltipToPrevious(barTC, TooltipMakerAPI.TooltipLocation.BELOW, false);
        }

        for (EventStageData curr : stages) {
            if (curr.progress <= 0) continue; // no icon for "starting" stage
            //if (curr.rollData == null || curr.rollData.equals(RANDOM_EVENT_NONE)) continue;
            if (RANDOM_EVENT_NONE.equals(curr.rollData)) continue;
            if (curr.wasEverReached && curr.isOneOffEvent && !curr.isRepeatable) continue;

            if (curr.hideIconWhenPastStageUnlessLastActive &&
                    curr.progress <= progress &&
                    getLastActiveStage(true) != curr) {
                continue;
            }

            EventStageDisplayData data = createDisplayData(curr.id);
            UIComponentAPI marker = main.addEventStageMarker(data);
            float xOff = bar.getXCoordinateForProgress(curr.progress) - bar.getPosition().getX();
            marker.getPosition().aboveLeft(bar, data.downLineLength).setXAlignOffset(xOff - data.size / 2f - 1);

            TooltipMakerAPI.TooltipCreator tc = getStageTooltip(curr.id);
            if (tc != null) {
                main.addTooltipTo(tc, marker, TooltipMakerAPI.TooltipLocation.LEFT, false);
            }
        }

        // progress indicator
        {
            UIComponentAPI marker = main.addEventProgressMarker(this);
            float xOff = bar.getXCoordinateForProgress(progress) - bar.getPosition().getX();
            marker.getPosition().belowLeft(bar, -getBarProgressIndicatorHeight() * 0.5f - 2)
                    .setXAlignOffset(xOff - getBarProgressIndicatorWidth() / 2 - 1);
        }

        main.addSpacer(opad);
        main.addSpacer(opad);
        TooltipMakerAPI tooltip = main.beginSubTooltip(getBarWidth());
        addAdditionalInfo(tooltip);
        main.endSubTooltip();
        main.addCustom(tooltip, 5f);
        main.addSpacer(opad);
        for (EventStageData curr : stages) {
            if (curr.wasEverReached && curr.isOneOffEvent && !curr.isRepeatable) continue;
            addStageDescriptionWithImage(main, curr.id);
        }
        tooltip = main.beginSubTooltip(getBarWidth());
        tooltip.addSectionHeading("Current progress", Alignment.MID, 5f);
        if(!finishedAwakening){
            tooltip.addPara("Currently we are able to awake %s people from cryosleep daily", 5f, Color.ORANGE, Misc.getWithDGS(getAwakenedPerDay()));
            tooltip.addPara("Estimated monthly awakening progress :%S", 5f, Color.ORANGE, Misc.getWithDGS(getAwakenedMonthly()));
        }
        else{
            tooltip.addPara("Process finished!",5f);
        }
        tooltip.addSectionHeading("Awakening Log",Alignment.MID,5f);
        for (EventStageData stage : getStages()) {
            if(stage.wasEverReached){
                if(rewards.get(stage.id)!=null&&rewards.get(stage.id).timeLapse!=null){
                    tooltip.addPara("%s :"+rewards.get(stage.id).response,5f,Color.ORANGE,rewards.get(stage.id).timeLapse);
                }
            }
        }
        main.endSubTooltip();
        main.addCustom(tooltip, 5f);

        if (showDomainMajority) {
            tooltip = main.beginSubTooltip(getBarWidth());
            tooltip.addSectionHeading("Domain Majority - Effects", Alignment.MID, 0f);
            tooltip.addPara("Currently our colony is populated by majority of people, whom never witnessed the Collapse and still remembers days of golden age of the Domain.", 5f, Color.ORANGE, Misc.getWithDGS(getAwakenedPerDay()));
            Color[] col = new Color[2];
            col[0] = Misc.getPositiveHighlightColor();
            col[1] = Color.ORANGE;
            tooltip.addPara("%s towards usage of %s", 5f, col,"-1",Global.getSettings().getCommoditySpec(Commodities.DRUGS).getName());
            tooltip.addPara("%s towards production of %s", 5f, col,"+1","all commodities");
            if(!finishedAwakening){
                tooltip.addPara("Reduction of stability by %s for the duration of awakening procedure",5f,Misc.getNegativeHighlightColor(),""+tiedMarket.getSize());

            }
            main.endSubTooltip();
            main.addCustom(tooltip, 2f);
        }
        panel.addUIElement(main).inTL(0, 0);
    }

    public void addAdditionalInfo(TooltipMakerAPI info) {
        info.addPara("Awakening procedure of Cryosleeper has started", Misc.getPositiveHighlightColor(), 5f);
        if(awakenedSoFar>=getPopulationToAwaken()){
            awakenedSoFar = getPopulationToAwaken();
        }
        info.addPara("Awakened people so far: %s", 5f, Color.ORANGE, "" + Misc.getWithDGS(awakenedSoFar));
        info.addPara("Current size of %s : %s", 5f, Color.ORANGE, tiedMarket.getName(), tiedMarket.getSize() + "");
        info.addPara("With awakening procedure progressing, market size will grow, and with that, this market will receive special benefits", 5f);
        info.addPara("We might also receive unique rewards from awakenings, as we will awaken more and more people!",Misc.getPositiveHighlightColor(),5f);
        info.addPara("The rate of awakening will increase as market size grows.", Misc.getTooltipTitleAndLightHighlightColor(), 5f);

    }

    @Override
    public TooltipMakerAPI.TooltipCreator getStageTooltipImpl(Object stageId) {
       return null;
    }

    public BaseAwakeningReward generateRandomReward() {
        BaseAwakeningReward reward = new BaseAwakeningReward(generateRandomEventType());
        return reward;
    }

    public EventType generateRandomEventType() {
        boolean haveOfficer = false;
        boolean haveAdmin = false;
        boolean haveShip = false;
        for (BaseAwakeningReward value : rewards.values()) {
           if( value.type==EventType.OFFICER){
               haveOfficer = true;
           }
           if( value.type==EventType.ADMIN){
               haveAdmin = true;
           }
            if( value.type==EventType.SHIP_BP){
                haveShip = true;
            }
        }
        while (true){
            EventType type =  EventType.values()[MathUtils.getRandomNumberInRange(0, EventType.values().length - 1)];
            if(haveAdmin&&type==EventType.ADMIN)continue;
            if(haveOfficer&&type==EventType.OFFICER)continue;
            if(haveShip&&type==EventType.SHIP_BP)continue;
            return type;
        }
    }

    @Override
    public void reportEconomyTick(int iterIndex) {

    }


    @Override
    public void advance(float amount) {
        super.advance(amount);
        if (util == null) {
            util = new IntervalUtil(CampaignClock.SECONDS_PER_GAME_DAY, CampaignClock.SECONDS_PER_GAME_DAY);
        }
        util.advance(amount);
        if (util.intervalElapsed()) {
            if(awakenedSoFar< getPopulationToAwaken()){
                awakenedSoFar += getAwakenedPerDay();
                int size = tiedMarket.getSize();
                if (size < countDivisionsByTen(awakenedSoFar)) {
                    tiedMarket.removeCondition("population_" + tiedMarket.getSize());
                    tiedMarket.setSize(size + 1);
                    tiedMarket.addCondition("population_" + tiedMarket.getSize());
                    tiedMarket.getPopulation().setWeight(0.01f);
                    if(!tiedMarket.hasCondition("aotd_domain_majority")){
                        tiedMarket.addCondition("aotd_domain_majority");
                    }
                    MessageIntel intel = new MessageIntel(tiedMarket.getName() + " in " + tiedMarket.getStarSystem().getName(), Misc.getBasePlayerColor());
                    intel.addLine(BaseIntelPlugin.BULLET + "Colony grew to size " + tiedMarket.getSize());
                    intel.setIcon(Global.getSector().getPlayerFaction().getCrest());
                    intel.setSound(BaseIntelPlugin.getSoundStandardUpdate());
                    Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, tiedMarket);
                    showDomainMajority = true;
                }
            }


            float progres = (float) awakenedSoFar / getPopulationToAwaken();
            setProgress((int) (progres * (float) getMaxProgress()));
        }
        if(progress>=getMaxProgress()&&!isEnding()&&!isEnded()){
            finishedAwakening = true;
            tiedMarket.removeIndustry("reawakening_facility", MarketAPI.MarketInteractionMode.REMOTE,false);
            DomainMajority majority = (DomainMajority) tiedMarket.getFirstCondition("aotd_domain_majority").getPlugin();
            majority.setShouldReduceStability(false);
            endAfterDelay();
        }
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
        rewards.clear();
    }

    private int getPopulationToAwaken() {
        if(type== AwakeningType.ARK){
            return 100000000;
        }
        return 10000000;
    }

    public void setProgress(int progress) {
        if (this.progress == progress) return;

        if (progress < 0) progress = 0;
        if (progress > maxProgress) progress = maxProgress;
        this.progress = progress;
        EventStageData prev = getLastActiveStage(true);

        //progress += 30;
        //progress = 40;
        //progress = 40;
        //progress = 499;

;


        if (progress < 0) {
            progress = 0;
        }
        if (progress > getMaxProgress()) {
            progress = getMaxProgress();
        }


        for (EventStageData curr : getStages()) {
            //if (curr.progress > progress) continue
            // reached
            if (curr.progress <= progress) {
                if (!curr.wasEverReached) {
                    if (curr.sendIntelUpdateOnReaching && curr.progress > 0) {
                        sendUpdateIfPlayerHasIntel(curr, getTextPanelForStageChange());
                        if(getTextPanelForStageChange()==null){
                            String id = (String) curr.id;
                            if(id.equals("aotd_cryosleeper_end")){
                                Global.getSector().getCampaignUI().addMessage("Awakening procedure finished!",Color.ORANGE);

                            } else if (id.equals("aotd_cryosleeper_begin")) {
                                Global.getSector().getCampaignUI().addMessage("Awakening procedure started",Color.ORANGE);


                            }
                            else{
                                Global.getSector().getCampaignUI().addMessage("An event has occurred during awakening procedure",Color.ORANGE);

                            }
                        }
                    }
                    notifyStageReached(curr);
                    curr.rollData = null;
                    curr.wasEverReached = true;

                    progress = getProgress(); // in case it was changed by notifyStageReached()
                }
            }
        }
    }

    public int getAwakenedPerDay(int size) {
        float mult = (float) Math.pow(10, size - 1);
        int multiplier = 2;
        int toReturn = (int) (multiplier * mult);
        return toReturn;
    }
    public int getLeftDays() {
        int soFar= awakenedSoFar;
        int size = tiedMarket.getSize();
        int days = 0;
        if(countDivisionsByTen(soFar)>=size){
            size = countDivisionsByTen(soFar);
        }
        while (soFar<getPopulationToAwaken()){
            soFar+=getAwakenedPerDay(size);
            if(countDivisionsByTen(soFar)>=size){
                size = countDivisionsByTen(soFar);
            }
            days++;
        }
        return days;
    }

    public int getAwakenedInCertainPeriod(int days) {
        int sofar= awakenedSoFar;
        int awakened = 0;
        int awakenedTotalSoFar = awakenedSoFar;
        int currSize = tiedMarket.getSize();
        int predictedSizeSoFar = countDivisionsByTen(sofar);
        if(predictedSizeSoFar>=currSize){
            currSize = predictedSizeSoFar;
        }
        for (int i = 0; i < days; i++) {
            float perDay = getAwakenedPerDay(currSize);
            awakened+=perDay;
            sofar+=perDay;
            if(countDivisionsByTen(sofar)>=currSize){
                currSize = countDivisionsByTen(sofar);
            }

        }
        return awakened;
    }
    public int getAwakenedMonthly() {
        int sofar= awakenedSoFar;
        int awakened = 0;
        int awakenedTotalSoFar = awakenedSoFar;
        int currSize = tiedMarket.getSize();
        int predictedSizeSoFar = countDivisionsByTen(sofar);
        if(predictedSizeSoFar>=currSize){
            currSize = predictedSizeSoFar;
        }
        for (int i = 0; i < 30; i++) {
            float perDay = getAwakenedPerDay(currSize);
            awakened+=perDay;
            sofar+=perDay;
            if(countDivisionsByTen(sofar)>=currSize){
                currSize = countDivisionsByTen(sofar);
            }

        }
        if(awakenedSoFar+awakened>=getPopulationToAwaken()){
            return getPopulationToAwaken()-awakenedSoFar;
        }
        return awakened;
    }
    public int getAwakenedPerDay() {
        int size = tiedMarket.getSize();
        float mult = (float) Math.pow(10, size - 1);
        int multiplier = 2;
        int toReturn = (int) (multiplier * mult);
        return toReturn;
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel","reawakening");
    }

    @Override
    protected void notifyStageReached(EventStageData stage) {
        String id = (String) stage.id;
        if(id.contains("test")){
            BaseAwakeningReward reward = rewards.get(id);
            if(reward!=null){
                reward.grantReward(uiWidth,tiedMarket);
            }


        }


    }

    public int countDivisionsByTen(int number) {
        if (number <= 0) {
            return 1;
        }

        int count = 0;
        while (number >= 10) {
            number /= 10;
            count++;
        }
        return count;
    }

}
