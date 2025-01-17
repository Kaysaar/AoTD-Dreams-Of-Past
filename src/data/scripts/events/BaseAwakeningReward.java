package data.scripts.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class BaseAwakeningReward {
    public AwakeningEventPlugin.EventType type;
    Object reward;

    public BaseAwakeningReward(AwakeningEventPlugin.EventType type) {
        this.type = type;
    }

    public void createReward() {
        if (type == AwakeningEventPlugin.EventType.OFFICER) {
            PersonAPI officer = Global.getFactory().createPerson();
            officer.setFaction(Factions.SLEEPER);

        }
        if (type == AwakeningEventPlugin.EventType.ADMIN) {

        }
        if(type == AwakeningEventPlugin.EventType.WEAPON_BP){

        }
        if(type == AwakeningEventPlugin.EventType.SHIP_BP){

        }
        if(type == AwakeningEventPlugin.EventType.FIGHTER_BP){

        }
        if(type == AwakeningEventPlugin.EventType.DATABANKS){

        }
        if(type == AwakeningEventPlugin.EventType.MEGASTRUCTURE_LOCATION){

        }
        if(type == AwakeningEventPlugin.EventType.INSTANT_RESEARCH){

        }
        return;
    }

    public void grantReward() {

    }

}
