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

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class Messages {

    private Configuration msgFile;

    public Messages(JavaPlugin plugin) {
        msgFile = new Configuration(new File(plugin.getDataFolder(),
                "messages.yml"));
        msgFile.load();
        if(msgFile.getString("EasyBan enabled") == null) {
            loadDefaults();
            msgFile.save();
        }
    }

    public String getMessage(String string) {
        return msgFile.getString(string);
    }

    private void loadDefaults() {
        msgFile.setProperty("EasyBan enabled", "EasyBan enabled");
        msgFile.setProperty("has been kicked", " has been kicked");
        msgFile.setProperty("You have been kicked", "You have been kicked");
        msgFile.setProperty("has been banned", " has been banned");
        msgFile.setProperty("You have been banned", "You have been banned");
        msgFile.setProperty("has been unbanned", " has been unbanned");
        msgFile.setProperty("Invalid Subnet", "Invalid Subnet");
    }
}
