package data.scripts.events;

import com.fs.starfarer.api.impl.campaign.intel.events.BaseEventIntel;
import com.fs.starfarer.api.impl.campaign.intel.events.BaseOneTimeFactor;

public class AwakeningEventFactor extends BaseOneTimeFactor {
    public AwakeningEventFactor(int points) {
        super(points);
    }

    @Override
    public String getDesc(BaseEventIntel intel) {
        return "An event has occurred during awakening process";
    }
}
