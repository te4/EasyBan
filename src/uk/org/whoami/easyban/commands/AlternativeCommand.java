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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.util.HashSet;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import uk.org.whoami.easyban.datasource.DataSource;

public class AlternativeCommand extends EasyBanCommand {

    private DataSource database;

    public AlternativeCommand(DataSource database) {
        this.database = database;
    }

    @Override
    protected void execute(CommandSender cs, Command cmnd, String cmd, String[] args) {
        String ip;
        if (args.length == 0) {
            return;
        }
        ip = args[0];
        if (ip.contains(".")) {
            cs.sendMessage(m._("Users who connected from IP") + ip);
            this.sendListToSender(cs, database.getNicks(ip));
        } else {
            cs.sendMessage(m._("Alternative nicks of ") + ip);
            this.sendListToSender(cs, getNicks(ip));
        }
    }

    private String[] getNicks(String ip) {
        HashSet<String> nicks = new HashSet<String>();

        for (String altip : database.getHistory(ip)) {
            nicks.addAll(Arrays.asList(database.getNicks(altip)));
        }
        return nicks.toArray(new String[0]);
    }
}
