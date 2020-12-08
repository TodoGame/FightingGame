// @ts-ignore
import { WebSocket } from 'https://deno.land/x/websocket@v0.0.5/mod.ts';
import {getWsUrlWithTicket} from "./ws.ts";
import {subWsConfig, user1} from "./config.ts";
import {authorize} from "./auth.ts";


async function main() {
    const user = await authorize(user1)
    if (user == null) {
        console.log('Authorization failed')
        return
    }
    const urlWithTicket = await getWsUrlWithTicket(subWsConfig, user)

    const ws = new WebSocket(urlWithTicket)
    ws.on('open', () => {
        console.log('Connected to WS')
        const subscription = {
            type: 'subscription.UserMoneyUpdateSubscription',
            username: 'username',
            subscribe: true,
        }
        ws.send(JSON.stringify(subscription))
        console.log('Subscribed to money updates of User(username=username)')
    })
    ws.on('message', (msg: string) => {
        console.log('Received message', msg)
    })
    ws.on('close', () => {
        console.log('WS Closed')
    })
}

main()
