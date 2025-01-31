package data.plugins;


import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.campaign.*;;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.econ.listeners.CryoIndustryOptionsProvider;
import lunalib.lunaSettings.LunaSettings;

import java.util.Collections;
import java.util.List;

//import data.ui.ExampleIDP;


public class AodCryosleeperPLugin extends BaseModPlugin {
    public static int configSize = Misc.MAX_COLONY_SIZE;
    public static float AwakeningCryosleeper = 180;
    public static float AwakeningArk = 400;
    public static float sizeCryosleeper = 7;
    public static float sizeArk = 8;

    @Override
    public void onNewGameAfterProcGen() {
        super.onNewGameAfterProcGen();
        List<StarSystemAPI> starSystemAPIS = Global.getSector().getStarSystems();
        Collections.shuffle(starSystemAPIS);
        for (StarSystemAPI starSystem : starSystemAPIS) {
            if (starSystem.hasTag(Tags.THEME_DERELICT)) {
                if (starSystem.getEntitiesWithTag("aotd_cryosleeper").isEmpty()) {
                    CustomCampaignEntityAPI ark = starSystem.addCustomEntity((String) null, "Ark", "ark", Global.getSector().getPlayerFaction().getId());
                    float orbitRadius = starSystem.getStar().getRadius() + 1300;
                    long seed = StarSystemGenerator.random.nextLong();
                    float orbitDays = orbitRadius / (20f + Misc.random.nextFloat() * 5f);
                    ark.setOrbit(starSystem.getStar().getOrbit());
                    ark.setCircularOrbit(starSystem.getStar(), Misc.random.nextFloat() * 360f, orbitRadius, orbitDays);
                    ark.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
                    ark.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPEC_ID_OVERRIDE, "ark");
                    ark.getMemoryWithoutUpdate().set("$hasDefenders", true);
                    break;
                }
            }
        }
        starSystemAPIS.clear();
    }

    private void setListenersIfNeeded() {
        ListenerManagerAPI l = Global.getSector().getListenerManager();

        if (!l.hasListenerOfClass(CryoIndustryOptionsProvider.class))
            l.addListener(new CryoIndustryOptionsProvider(), false);
    }

    public void onGameLoad(boolean newGame) {
        setListenersIfNeeded();
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            if (LunaSettings.getInt("Cryo_but_better", "aotd_ark_awakening_period") != null) {
                AwakeningArk = LunaSettings.getInt("Cryo_but_better", "aotd_ark_awakening_period");

            }
            if (LunaSettings.getInt("Cryo_but_better", "aotd_cryosleeper_awakening_period") != null) {
                AwakeningCryosleeper = LunaSettings.getInt("Cryo_but_better", "aotd_cryosleeper_awakening_period");
            }
            if (LunaSettings.getInt("Cryo_but_better", "aotd_ark_awakening_reward") != null) {
                sizeArk = LunaSettings.getInt("Cryo_but_better", "aotd_ark_awakening_reward");

            }
            if (LunaSettings.getInt("Cryo_but_better", "aotd_cryosleepe_awakening_reward") != null) {
                sizeCryosleeper = LunaSettings.getInt("Cryo_but_better", "aotd_cryosleepe_awakening_reward");
            }
        }
//        for (PlanetAPI planet : Global.getSector().getPlayerFleet().getStarSystem().getPlanets()) {
//            if(planet.isStar())continue;
//            if(planet.isMoon())continue;
//            MarketAPI setMarket = planet.getMarket();
//            long seed = StarSystemGenerator.random.nextLong();
//            SectorEntityToken stationEntity = planet.getContainingLocation().addCustomEntity((String) null, "Ark", "ark",Global.getSector().getPlayerFaction().getId());
//            SectorEntityToken primary = setMarket.getPrimaryEntity();
//            stationEntity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SEED, seed);
//            stationEntity.getMemoryWithoutUpdate().set(MemFlags.SALVAGE_SPEC_ID_OVERRIDE, "ark");
//            stationEntity.getMemoryWithoutUpdate().set("$hasDefenders", true);
//            float orbitRadius = primary.getRadius() + 150.0F;
//            stationEntity.setCircularOrbitPointingDown(primary, (float)180F, orbitRadius, orbitRadius / 10.0F);
//
//            stationEntity.setDiscoverable(false);
//            break;
//        }
//
//        for (PlanetAPI planet : Global.getSector().getPlayerFleet().getStarSystem().getPlanets()) {
//            if(planet.isStar())continue;
//            if(planet.isMoon())continue;
//            MarketAPI setMarket = planet.getMarket();
//            SectorEntityToken stationEntity = planet.getContainingLocation().addCustomEntity((String) null, null, "derelict_cryosleeper",Global.getSector().getPlayerFaction().getId());
//            SectorEntityToken primary = setMarket.getPrimaryEntity();
//            float orbitRadius = primary.getRadius() + 150.0F;
//            stationEntity.setCircularOrbitPointingDown(primary, (float)180F, orbitRadius, orbitRadius / 10.0F);
//            stationEntity.getMemoryWithoutUpdate().set("$hasDefenders", true);
//            stationEntity.setDiscoverable(false);
//            break;
//        }
//        new AwakeningEventPlugin(Misc.getPlayerMarkets(true).get(0), AwakeningEventPlugin.AwakeningType.CRYOSLEEPER);
    }
}
