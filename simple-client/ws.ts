import {WsConfig} from "./config.ts";
import {AuthorizedUser} from "./auth.ts";

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
