package edu.moravian;

import edu.moravian.exceptions.SecretsException;
import edu.moravian.exceptions.TokenNotFound;
import edu.moravian.process.ProcessManager;
import edu.moravian.process.ProcessStorage;
import edu.moravian.process.RedisProcessStorage;
import edu.moravian.process.processes.AddMediaProcess;
import edu.moravian.process.processes.SuggestMediaProcess;
import edu.moravian.process.processes.ViewMediaProcess;
import edu.moravian.watchlist.RedisStorage;
import edu.moravian.watchlist.WatchlistApp;
import edu.moravian.watchlist.WatchlistAppStorage;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class WatchlistBot {
    public static void main(String[] args) {
        String token = null;

        try {
            String secretName = "220_Discord_Token";
            String secretKey = "DISCORD_TOKEN";

            Secrets secrets = new Secrets();
            token = secrets.getSecret(secretName, secretKey);
        } catch (SecretsException e) {
            System.out.println(e.getMessage());
        }

        try {
            Dotenv dotenv = Dotenv.load();
            token = dotenv.get("DISCORD_TOKEN");
            if (token == null) {
                throw new TokenNotFound("No DISCORD_TOKEN exists in .env");
            }
        } catch (TokenNotFound e) {
            System.out.println(e.getMessage());
        }

        JDA api = JDABuilder.createDefault(token).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

        WatchlistAppStorage watchlistStorage = new RedisStorage("localhost", 6379);
        WatchlistApp watchlistApp = new WatchlistApp(watchlistStorage);

        ProcessStorage processStorage = new RedisProcessStorage("localhost", 6379);
        ProcessManager processManager = new ProcessManager(processStorage);

        processManager.registerProcess("view", new ViewMediaProcess(watchlistApp, processStorage));
        processManager.registerProcess("add", new AddMediaProcess(watchlistApp, processStorage));
        processManager.registerProcess("suggest", new SuggestMediaProcess(watchlistApp, processStorage));

        BotCommands commands = new BotCommands(processManager);

        api.addEventListener(new ListenerAdapter() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                if (event.getAuthor().isBot()) {
                    return;
                }

                if (!event.getChannel().getName().equals("general")) {
                    return;
                }

                String message = event.getMessage().getContentRaw();
                String username = event.getAuthor().getAsMention();
                String response = commands.respond(username, message);

                if (!response.isEmpty()) {
                    if (response.length() <= 2000) {
                        event.getChannel().sendMessage(response).queue();
                    } else {
                        // Split by newlines so we don't cut a movie description in half
                        int limit = 1900; // slightly under 2000 to be safe
                        int currentLength = 0;
                        StringBuilder chunk = new StringBuilder();

                        for (String line : response.split("\n")) {
                            if (currentLength + line.length() + 1 > limit) {
                                event.getChannel().sendMessage(chunk.toString()).queue();
                                chunk = new StringBuilder();
                                currentLength = 0;
                            }
                            chunk.append(line).append("\n");
                            currentLength += line.length() + 1;
                        }

                        if (!chunk.isEmpty()) {
                            event.getChannel().sendMessage(chunk.toString()).queue();
                        }
                    }
                }
            }
        });
    }
}