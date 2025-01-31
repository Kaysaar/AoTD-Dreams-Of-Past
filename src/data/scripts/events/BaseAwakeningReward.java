package data.scripts.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.CryopodOfficerGen;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import static com.fs.starfarer.api.util.Misc.random;

public class BaseAwakeningReward {
    public AwakeningEventPlugin.EventType type;
    public String response;
    public String timeLapse = null;
    public static ArrayList<String> ITEMS = new ArrayList();
    static {
        ITEMS.add("corrupted_nanoforge");
        ITEMS.add("synchrotron");
        ITEMS.add("orbital_fusion_lamp");
        ITEMS.add("coronal_portal");
        ITEMS.add("mantle_bore");
        ITEMS.add("catalytic_core");
        ITEMS.add("soil_nanites");
        ITEMS.add("biofactory_embryo");
        ITEMS.add("fullerene_spool");
        ITEMS.add("plasma_dynamo");
        ITEMS.add("cryoarithmetic_engine");
        ITEMS.add("drone_replicator");
        ITEMS.add("dealmaker_holosuite");
    }
    public static WeightedRandomPicker<CryopodOfficerGen.CryopodOfficerTemplate> TEMPLATES_EXCEPTIONAL = new WeightedRandomPicker<CryopodOfficerGen.CryopodOfficerTemplate>();
    static {
        CryopodOfficerGen.CryopodOfficerTemplate t;

        // BEGIN LEVEL 7 OFFICERS

        // fast high-tech ship
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.elite.add(Skills.ENERGY_WEAPON_MASTERY);
        t.elite.add(Skills.FIELD_MODULATION);
        t.elite.add(Skills.GUNNERY_IMPLANTS);
        t.elite.add(Skills.SYSTEMS_EXPERTISE);
        t.base.add(Skills.COMBAT_ENDURANCE);
        t.base.add(Skills.HELMSMANSHIP);
        TEMPLATES_EXCEPTIONAL.add(t, 10f);

        // slow high-tech ship
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.HELMSMANSHIP);
        t.elite.add(Skills.ENERGY_WEAPON_MASTERY);
        t.elite.add(Skills.FIELD_MODULATION);
        t.elite.add(Skills.GUNNERY_IMPLANTS);
        t.elite.add(Skills.ORDNANCE_EXPERTISE);
        t.base.add(Skills.TARGET_ANALYSIS);
        t.base.add(Skills.COMBAT_ENDURANCE);
        TEMPLATES_EXCEPTIONAL.add(t, 10f);

        // hull/armor tank, low tech
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.DAMAGE_CONTROL);
        t.elite.add(Skills.IMPACT_MITIGATION);
        t.elite.add(Skills.POLARIZED_ARMOR);
        t.elite.add(Skills.BALLISTIC_MASTERY);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.base.add(Skills.MISSILE_SPECIALIZATION);
        t.base.add(Skills.GUNNERY_IMPLANTS);
        TEMPLATES_EXCEPTIONAL.add(t, 5f);

        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.ORDNANCE_EXPERTISE);
        t.elite.add(Skills.IMPACT_MITIGATION);
        t.elite.add(Skills.POLARIZED_ARMOR);
        t.elite.add(Skills.BALLISTIC_MASTERY);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.base.add(Skills.MISSILE_SPECIALIZATION);
        t.base.add(Skills.GUNNERY_IMPLANTS);
        TEMPLATES_EXCEPTIONAL.add(t, 5f);

        // phase ship
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.IMPACT_MITIGATION);
        t.elite.add(Skills.FIELD_MODULATION);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.elite.add(Skills.SYSTEMS_EXPERTISE);
        t.elite.add(Skills.COMBAT_ENDURANCE);
        t.base.add(Skills.POLARIZED_ARMOR);
        t.base.add(Skills.ENERGY_WEAPON_MASTERY);
        TEMPLATES_EXCEPTIONAL.add(t, 10f);


        // generally-ok-for-most-ships, take 1
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.FIELD_MODULATION);
        t.elite.add(Skills.ORDNANCE_EXPERTISE);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.elite.add(Skills.POINT_DEFENSE);
        t.elite.add(Skills.GUNNERY_IMPLANTS);
        t.base.add(Skills.HELMSMANSHIP);
        t.base.add(Skills.COMBAT_ENDURANCE);
        TEMPLATES_EXCEPTIONAL.add(t, 5f);

        // generally-ok-for-most-ships, take 2
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.FIELD_MODULATION);
        t.elite.add(Skills.ORDNANCE_EXPERTISE);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.elite.add(Skills.IMPACT_MITIGATION);
        t.elite.add(Skills.GUNNERY_IMPLANTS);
        t.base.add(Skills.HELMSMANSHIP);
        t.base.add(Skills.COMBAT_ENDURANCE);
        TEMPLATES_EXCEPTIONAL.add(t, 5f);


        // SO, ballistic weapons
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.COMBAT_ENDURANCE);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.elite.add(Skills.SYSTEMS_EXPERTISE);
        t.elite.add(Skills.DAMAGE_CONTROL);
        t.elite.add(Skills.IMPACT_MITIGATION);
        t.base.add(Skills.FIELD_MODULATION);
        t.base.add(Skills.BALLISTIC_MASTERY);
        TEMPLATES_EXCEPTIONAL.add(t, 5f);

        // SO, energy weapons
        t = new CryopodOfficerGen.CryopodOfficerTemplate();
        t.elite.add(Skills.COMBAT_ENDURANCE);
        t.elite.add(Skills.TARGET_ANALYSIS);
        t.elite.add(Skills.SYSTEMS_EXPERTISE);
        t.elite.add(Skills.DAMAGE_CONTROL);
        t.elite.add(Skills.IMPACT_MITIGATION);
        t.base.add(Skills.FIELD_MODULATION);
        t.base.add(Skills.ENERGY_WEAPON_MASTERY);
        TEMPLATES_EXCEPTIONAL.add(t, 5f);
    }
    public BaseAwakeningReward(AwakeningEventPlugin.EventType type) {
        this.type = type;
    }


    public void grantReward(final float width, MarketAPI market) {
        if (type == AwakeningEventPlugin.EventType.OFFICER) {
            CryopodOfficerGen.CryopodOfficerTemplate template = TEMPLATES_EXCEPTIONAL.pick(random);
            PersonAPI officer =template.create(Global.getSector().getFaction(Factions.MERCENARY),random);
            officer.setFaction(Factions.PLAYER);
            officer.getStats().setSkillLevel("officer_man_out_of_time",4);
            officer.getStats().setSkillLevel(Skills.TARGET_ANALYSIS,0);
            Global.getSector().getPlayerFleet().getFleetData().addOfficer(officer);

        }
        if (type == AwakeningEventPlugin.EventType.ADMIN) {
            PersonAPI person = Global.getSector().getFaction(Factions.PLAYER).createRandomPerson();

            person.setFaction(Factions.PLAYER);
            person.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING,3);
            person.getStats().setSkillLevel("admin_man_out_of_time",3);

            Global.getSector().getCharacterData().addAdmin(person);
            response = " New Administrator joined our ranks";

        }
        if(type == AwakeningEventPlugin.EventType.SHIP_BP){
            response = " We have received blueprint package of XIV Battlegroup";

            CargoAPI cargoAPI = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
            cargoAPI.addSpecial(new SpecialItemData("XIV_package","XIV_bp"),1);

        }
        if(type== AwakeningEventPlugin.EventType.ITEM){
            ArrayList<String> list = new ArrayList<>(ITEMS);
            Collections.shuffle(list);
            CargoAPI cargoAPI = market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo();
            String id = list.get(MathUtils.getRandomNumberInRange(0,list.size()-1));
                cargoAPI.addSpecial(new SpecialItemData(id,null),1);
            response = " "+Global.getSettings().getSpecialItemSpec(id).getName()+" has been retried and re-located to local storage";
        }
        timeLapse = Global.getSector().getClock().getShortDate();
    }


}
