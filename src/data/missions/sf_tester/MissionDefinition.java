package data.missions.sf_tester;

import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.BattleObjectives;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {

    public void defineMission(MissionDefinitionAPI api) {

        // Set up the fleets so we can add ships and fighter wings to them.
        // In this scenario, the fleets are attacking each other, but
        // in other scenarios, a fleet may be defending or trying to escape
        api.initFleet(FleetSide.PLAYER, "ISS", FleetGoal.ATTACK, false);
        api.initFleet(FleetSide.ENEMY, "ISS", FleetGoal.ATTACK, true);

//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.COORDINATED_MANEUVERS, 3);
//		api.getDefaultCommander(FleetSide.PLAYER).getStats().setSkillLevel(Skills.ELECTRONIC_WARFARE, 3);

        // Set a small blurb for each fleet that shows up on the mission detail and
        // mission results screens to identify each side.
        api.setFleetTagline(FleetSide.PLAYER, "\"这只是一名诚实商人的谋生手段, 警官.\"");
        api.setFleetTagline(FleetSide.ENEMY, "坏家伙 \"High Rad\" Salazar 以及 Moon 的生锈骡子");

        // These show up as items in the bulleted list under
        // "Tactical Objectives" on the mission detail screen
        //api.addBriefingItem("告诉 'High Rad' Moon 撕毁一桩交易会导致什么后果.");
        //api.addBriefingItem("别让 'Stranger II' 挂掉 - 这可是我最宝贵的财富.");
        //api.addBriefingItem("Moon 骡子-级的引擎状态看起来很糟糕, 用火蛇让它飘起来吧.");

        boolean testMode = false;
        // Set up the player's fleet.  Variant names come from the
        // files in data/variants and data/variants/fighters
        //api.addToFleet(FleetSide.PLAYER, "station_small_Standard", FleetMemberType.SHIP, "Test Station", false);
        if (!testMode) {
            api.addToFleet(FleetSide.PLAYER, "fs_AnYong_variant", FleetMemberType.SHIP, "暗涌", true);
            api.addToFleet(FleetSide.PLAYER, "fs_BaWangA7_variant", FleetMemberType.SHIP, "霸王A7", true);
            api.addToFleet(FleetSide.PLAYER, "fs_ShaYiXi_variant", FleetMemberType.SHIP, "沙翼蜥", true);
            api.addToFleet(FleetSide.PLAYER, "fs_WeiZhiXingHao_variant", FleetMemberType.SHIP, "未知型号", true);
            api.addToFleet(FleetSide.PLAYER, "fs_XiuChong_variant", FleetMemberType.SHIP, "锈铳", true);
            api.addToFleet(FleetSide.PLAYER, "fs_ChuiXinZhe_variant", FleetMemberType.SHIP, "锤心者", true);

            api.addToFleet(FleetSide.PLAYER, "fs_AnYongZhongPaoShou_variant", FleetMemberType.SHIP, "暗涌重炮手", true);
            api.addToFleet(FleetSide.PLAYER, "fs_BoBo_variant", FleetMemberType.SHIP, "波波", false);

            api.addToFleet(FleetSide.PLAYER, "fs_DeLaiManNi_gt_variant", FleetMemberType.SHIP, "德莱曼妮", false);
            api.addToFleet(FleetSide.PLAYER, "fs_DeLaiManNi_impact_variant", FleetMemberType.SHIP, "德莱曼妮", false);
            api.addToFleet(FleetSide.PLAYER, "fs_DeLaiManNi_trebie_variant", FleetMemberType.SHIP, "德莱曼妮", false);

            api.addToFleet(FleetSide.PLAYER, "fs_GeMing_variant", FleetMemberType.SHIP, "革命", false);
            api.addToFleet(FleetSide.PLAYER, "fs_HaiDeLa_variant", FleetMemberType.SHIP, "海德拉", false);
            api.addToFleet(FleetSide.PLAYER, "fs_HaiShi_variant", FleetMemberType.SHIP, "海使", false);
            api.addToFleet(FleetSide.PLAYER, "fs_HeiLang_variant", FleetMemberType.SHIP, "黑狼", false);
            api.addToFleet(FleetSide.PLAYER, "fs_HeiYing_variant", FleetMemberType.SHIP, "黑鹰", false);
            api.addToFleet(FleetSide.PLAYER, "fs_HeiZhu_variant", FleetMemberType.SHIP, "黑蛛", false);
            api.addToFleet(FleetSide.PLAYER, "fs_MeiYing_variant", FleetMemberType.SHIP, "魅影", false);
            api.addToFleet(FleetSide.PLAYER, "fs_RouLing_variant", FleetMemberType.SHIP, "蹂躏", false);

            api.addToFleet(FleetSide.PLAYER, "fs_TianDun_TuJi_variant", FleetMemberType.SHIP, "天盾", false);
            api.addToFleet(FleetSide.PLAYER, "fs_TianDunXianQuan_variant", FleetMemberType.SHIP, "天盾", false);

            api.addToFleet(FleetSide.PLAYER, "fs_XiuGu_variant", FleetMemberType.SHIP, "锈骨", false);
            api.addToFleet(FleetSide.PLAYER, "fs_XiuShi_variant", FleetMemberType.SHIP, "锈侍", false);
            api.addToFleet(FleetSide.PLAYER, "fs_XiuZhe_variant", FleetMemberType.SHIP, "锈者", false);
            api.addToFleet(FleetSide.PLAYER, "fs_XiuZhong_variant", FleetMemberType.SHIP, "锈种", false);
            api.addToFleet(FleetSide.PLAYER, "fs_ZhenZhu_variant", FleetMemberType.SHIP, "珍珠", false);
            api.addToFleet(FleetSide.PLAYER, "fs_ZhuBei_variant", FleetMemberType.SHIP, "珠贝1", false);
            api.addToFleet(FleetSide.PLAYER, "fs_ZhuBei_variant", FleetMemberType.SHIP, "珠贝2", false);
            api.addToFleet(FleetSide.PLAYER, "fs_ZhuBei_variant", FleetMemberType.SHIP, "珠贝3", false);
            api.addToFleet(FleetSide.PLAYER, "fs_ZhuBei_variant", FleetMemberType.SHIP, "珠贝4", false);

            // Set up the enemy fleet.
            api.addToFleet(FleetSide.ENEMY, "mule_d_pirates_Smuggler", FleetMemberType.SHIP, "Cherenkov Bloom", false);

            api.defeatOnShipLoss("Stranger II");
        }

        if (testMode) {
//			FleetMemberAPI member = api.addToFleet(FleetSide.PLAYER, "omen_PD", FleetMemberType.SHIP, "Milk Run", true);
//			member.getCaptain().getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 2);
//			member.getCaptain().getStats().setSkillLevel(Skills.SHIELD_MODULATION, 2);
//			member.getCaptain().getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);

            api.addToFleet(FleetSide.PLAYER, "falcon_Attack", FleetMemberType.SHIP, "Stranger II", true);
            //		PersonAPI person = new AICoreOfficerPluginImpl().createPerson(Commodities.ALPHA_CORE, null, null);
            //		member.setCaptain(person);

            api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, "Cherenkov Bloom", false);
            api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);
            api.addToFleet(FleetSide.ENEMY, "lasher_CS", FleetMemberType.SHIP, null, false);

            api.addObjective(0, 4000, BattleObjectives.SENSOR_JAMMER);
            api.addObjective(4000, 0, BattleObjectives.COMM_RELAY);
            api.addObjective(-3000, -2000, BattleObjectives.NAV_BUOY);
        }

        // Set up the map.
        float width = 12000f;
        float height = 12000f;

        if (testMode) {
            width += 4000;
            height += 8000;
        }

        api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        float minX = -width/2;
        float minY = -height/2;

        // Add an asteroid field
        api.addAsteroidField(minX, minY + height / 2, 0, 8000f,
                20f, 70f, 100);

        api.addPlanet(0, 0, 50f, StarTypes.RED_GIANT, 250f, true);

    }

}