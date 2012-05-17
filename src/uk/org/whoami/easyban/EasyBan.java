/*
 * Copyright 2011 Sebastian Köhler <sebkoehler@whoami.org.uk>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.org.whoami.easyban;

import java.io.IOException;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.maxmind.geoip.LookupService;

import uk.org.whoami.easyban.commands.AlternativeCommand;
import uk.org.whoami.easyban.commands.BanCommand;
import uk.org.whoami.easyban.commands.BanCountryCommand;
import uk.org.whoami.easyban.commands.BanInfoCommand;
import uk.org.whoami.easyban.commands.BanSubnetCommand;
import uk.org.whoami.easyban.commands.HistoryCommand;
import uk.org.whoami.easyban.commands.KickCommand;
import uk.org.whoami.easyban.commands.ListBansCommand;
import uk.org.whoami.easyban.commands.ListCountryBansCommand;
import uk.org.whoami.easyban.commands.ListSubnetBansCommand;
import uk.org.whoami.easyban.commands.ListTemporaryBansCommand;
import uk.org.whoami.easyban.commands.ListWhitelistCommand;
import uk.org.whoami.easyban.commands.ReloadCommand;
import uk.org.whoami.easyban.commands.UnbanCommand;
import uk.org.whoami.easyban.commands.UnbanCountryCommand;
import uk.org.whoami.easyban.commands.UnbanSubnetCommand;
import uk.org.whoami.easyban.commands.UnwhitelistCommand;
import uk.org.whoami.easyban.commands.WhitelistCommand;
import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.datasource.HSQLDataSource;
import uk.org.whoami.easyban.datasource.MySQLDataSource;
import uk.org.whoami.easyban.datasource.YamlDataSource;
import uk.org.whoami.easyban.listener.EasyBanCountryListener;
import uk.org.whoami.easyban.listener.EasyBanPlayerListener;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.tasks.UnbanTask;
import uk.org.whoami.easyban.util.ConsoleLogger;
import uk.org.whoami.easyban.util.DNSBL;
import uk.org.whoami.easyban.util.Metrics;
import uk.org.whoami.easyban.util.Metrics.Graph;
import uk.org.whoami.geoip.GeoIPLookup;
import uk.org.whoami.geoip.GeoIPTools;

//Updating & cleanup by Fishrock123 <Fishrock123@rocketmail.com>
public class EasyBan extends JavaPlugin {

    private DataSource database;
    private Settings settings;
    private DNSBL dnsbl;
    GeoIPLookup geo;

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        if (database != null) {
            database.close();
        }
        ConsoleLogger.info("EasyBan disabled; Version: " + this.getDescription().getVersion());
    }

    @Override
    public void onEnable() {
        settings = Settings.getInstance();
        switch (settings.getDatabase()) {
            case YAML:
                database = new YamlDataSource();
                break;
            case MYSQL:
                try {
                    database = new MySQLDataSource(settings);
                    break;
                } catch (Exception ex) {
                    ConsoleLogger.info(ex.getMessage());
                    ConsoleLogger.info("Can't load database");
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
            case HSQL:
                try {
                    database = new HSQLDataSource(this);
                    break;
                } catch (Exception ex) {
                    ConsoleLogger.info(ex.getMessage());
                    ConsoleLogger.info("Can't load database");
                    this.getServer().getPluginManager().disablePlugin(this);
                    return;
                }
        }
        try {
            dnsbl = new DNSBL();

            for (String bl : settings.getBlockLists()) {
                dnsbl.addLookupService(bl);
            }

            EasyBanPlayerListener l = new EasyBanPlayerListener(database, dnsbl);
            this.getServer().getPluginManager().registerEvents(l, this);
            this.getServer().getPluginManager().registerEvents(l, this);
        } catch (Exception ex) {
            ConsoleLogger.info(ex.getMessage());
            ConsoleLogger.info("DNSBL error");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        geo = getGeoIPLookup();
        if (geo != null) {
            this.getServer().getPluginManager().registerEvents(new EasyBanCountryListener(database, geo), this);
        }

        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new UnbanTask(database), 60L, 1200L);

        this.getCommand("ekick").setExecutor(new KickCommand());
        this.getCommand("eban").setExecutor(new BanCommand(database));
        this.getCommand("eunban").setExecutor(new UnbanCommand(database));
        this.getCommand("ehistory").setExecutor(new HistoryCommand(database));
        this.getCommand("ealternative").setExecutor(new AlternativeCommand(database));
        this.getCommand("ebaninfo").setExecutor(new BanInfoCommand(database));
        this.getCommand("elistbans").setExecutor(new ListBansCommand(database));
        this.getCommand("elisttmpbans").setExecutor(new ListTemporaryBansCommand(database));
        this.getCommand("ebansubnet").setExecutor(new BanSubnetCommand(database));
        this.getCommand("eunbansubnet").setExecutor(new UnbanSubnetCommand(database));
        this.getCommand("elistsubnets").setExecutor(new ListSubnetBansCommand(database));
        this.getCommand("ebancountry").setExecutor(new BanCountryCommand(database));
        this.getCommand("eunbancountry").setExecutor(new UnbanCountryCommand(database));
        this.getCommand("elistcountries").setExecutor(new ListCountryBansCommand(database));
        this.getCommand("ewhitelist").setExecutor(new WhitelistCommand(database));
        this.getCommand("eunwhitelist").setExecutor(new UnwhitelistCommand(database));
        this.getCommand("elistwhite").setExecutor(new ListWhitelistCommand(database));
        this.getCommand("ereload").setExecutor(new ReloadCommand(database, dnsbl));

        try {
        	
		    Metrics metrics = new Metrics(this);
		    
		    Graph banGraph = metrics.createGraph("Ban data");
		    
		    banGraph.addPlotter(new Metrics.Plotter("Nickname Bans") {	
				@Override
				public int getValue() {
					return database.getBannedNicks().length;
				}
			});
		    banGraph.addPlotter(new Metrics.Plotter("Subnet Bans") {	
				@Override
				public int getValue() {
					return database.getBannedSubnets().length;
				}
			});
		    banGraph.addPlotter(new Metrics.Plotter("Temporary bans") {	
				@Override
				public int getValue() {
					return database.getTempBans().size();
				}
			});
		    
		    if (geo != null) {
		    	
		    	banGraph.addPlotter(new Metrics.Plotter("Country bans") {	
		    		@Override
		    		public int getValue() {
						return database.getBannedCountries().length;
					}
				});
		    	 
		    	Graph countryGraph = metrics.createGraph("Commonly Banned Countries");
				    
				for (String ccode : database.getBannedCountries()) {
				    countryGraph.addPlotter(new Metrics.Plotter(LookupService.getCountryName(ccode)) {	
						@Override
						public int getValue() {
							return 1;
						}
					});
				}
		    }
		    
		    metrics.start();
		    
		} catch (IOException e) {
			ConsoleLogger.info(e.getMessage());
		}
        
        ConsoleLogger.info("EasyBan enabled; Version: " + this.getDescription().
                getVersion());
    }

    private GeoIPLookup getGeoIPLookup() {
        Plugin pl = this.getServer().getPluginManager().getPlugin("GeoIPTools");
        if (pl != null) {
            return ((GeoIPTools) pl).getGeoIPLookup(GeoIPLookup.COUNTRYDATABASE
                    | GeoIPLookup.IPV6DATABASE);
        } else {
            return null;
        }
    }
}
