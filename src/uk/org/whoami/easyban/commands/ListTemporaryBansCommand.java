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

import java.text.DateFormat;
import java.util.Date;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import uk.org.whoami.easyban.datasource.DataSource;

public class ListTemporaryBansCommand extends EasyBanCommand {

    private DataSource database;

    public ListTemporaryBansCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        cs.sendMessage(m._("Temporary bans: "));
        for (String key : database.getTempBans().keySet()) {
            cs.sendMessage(key + " : "
                    + DateFormat.getDateTimeInstance().format(new Date(database.getTempBans().get(key))));
        }
    }
}
