import { Command, SafetyJim } from '../../safetyjim/safetyjim';
import * as Discord from 'discord.js';

class Info implements Command {
    public usage = 'info - displays some information about the bot';

    private MAGNITUDES = [
        [1000 * 60 * 60 * 24 * 30, 'months'],
        [1000 * 60 * 60 * 24, 'days'],
        [1000 * 60 * 60, 'hours'],
        [1000 * 60, 'minutes'],
        [1000, 'seconds'],
    ];

    // tslint:disable-next-line:no-empty
    constructor(bot: SafetyJim) {}

    public run(bot: SafetyJim, msg: Discord.Message, args: string): boolean {
        this.asyncRun(bot, msg, args);
        return;
    }

    private async asyncRun(bot: SafetyJim, msg: Discord.Message, args: string): Promise<void> {
        let config = await bot.database.getGuildConfiguration(msg.guild);
        let uptimeString = this.timeElapsed((new Date()).getTime(), bot.bootTime.getTime());
        let embed = new Discord.RichEmbed({
            author: { name: 'Safety Jim - v0.0.1', icon_url: bot.client.user.avatarURL },
            description: `Lifting the :hammer: since ${uptimeString} ago.`,
            fields: [
                { name: 'Server Count', value:  bot.client.guilds.size.toString(), inline: true },
                { name: 'User Count', value: bot.client.users.size.toString(), inline: true },
                { name: 'Github', value: '[Samoxive/SafetyJim](https://github.com/samoxive/safetyjim)', inline: true },
                { name: 'Websocket Ping', value: `${bot.client.ping.toFixed(0)}ms`, inline: true},
                // tslint:disable-next-line:max-line-length
                { name: 'RAM usage', value: `${(process.memoryUsage().rss / (1024 * 1024)).toFixed(0)}MB`, inline: true },
                { name: 'Support', value: '[discord.io/SafetyJim](https://discord.io/safetyjim)', inline: true },
            ],
            footer: { text: `Made by Safety Jim team.`},
            color: parseInt(config.EmbedColor, 16),
        });

        msg.channel.send('', {embed});
    }

    private timeElapsed(before: number, after: number) {
        let diff = Math.abs(after - before);
        return this.MAGNITUDES.reduce((out, m: [number, string]) => {
          const current = Math.floor(diff / m[0]);
          diff %= m[0];
          if (out.length || current) {
            out.push(`${current} ${m[1]}`);
          }
          return out;
        }, []).join(' and ');
    }
}

export = Info;
