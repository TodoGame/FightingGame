// @ts-ignore
import { WebSocket } from 'https://deno.land/x/websocket@v0.0.5/mod.ts';
import {UserData, WsConfig} from "./config.ts";
import {authorize, AuthorizedUser} from "./auth.ts";

async function getTicket(config: WsConfig, user: AuthorizedUser): Promise<object> {
    const response = await fetch(config.getTicketEndpoint, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${user.token}`
        }
    })
    console.log(`Ticket Request: ${response.status} ${response.statusText}`)
    return await response.json()
}

export async function getWsUrlWithTicket(config: WsConfig, user: AuthorizedUser): Promise<string> {
    const ticket = await getTicket(config, user)
    return `${config.wsEndpoint}?ticket=${JSON.stringify(ticket)}`
}

export async function makeWebSocketConnection(config: WsConfig, user: UserData): Promise<WebSocket> {
    const authorizedUser = await authorize(user)
    if (authorizedUser == null) {
        throw new Error(`Auth failed for ${user.username}`)
    }
    const wsUrl = await getWsUrlWithTicket(config, authorizedUser)
    return new WebSocket(wsUrl)
}
