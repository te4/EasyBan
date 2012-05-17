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
package uk.org.whoami.easyban.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.maxmind.geoip.LookupService;

import uk.org.whoami.easyban.datasource.DataSource;
import uk.org.whoami.easyban.settings.Settings;
import uk.org.whoami.easyban.util.ConsoleLogger;

//Updating & cleanup by Fishrock123 <Fishrock123@rocketmail.com>
public class BanCountryCommand extends EasyBanCommand {

    private DataSource database;

    public BanCountryCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        if (args.length == 0) {
    		return;
        }
    	
    	if (!LookupService.hasCountryCode(args[0])) {
    		if (LookupService.hasCountryName(args[0])) {
    			args[0] = LookupService.getCountryCode(args[0]);
    		} else {
    			return;
    		}
    	}
        
        database.banCountry(args[0]);

        Settings settings = Settings.getInstance();
        if (settings.isCountryBanPublic()) {
            cs.getServer().broadcastMessage(m._("A country has been banned: ") + LookupService.getCountryName(args[0]) + " (" + args[0] + ')');
        }
        ConsoleLogger.info(admin + " banned the country " + LookupService.getCountryName(args[0]) + " (" + args[0] + ')');
    }
}
