// @ts-ignore
import { WebSocket } from 'https://deno.land/x/websocket@v0.0.5/mod.ts';

const heroku = true

const username = 'usernameTest1'
const password = 'password'
const name = 'name'

const host = heroku ? 'https://fighting-game-server.herokuapp.com' : 'http://localhost:8080'
const wsHost = heroku ? 'ws://fighting-game-server.herokuapp.com' : 'ws://localhost:8080'

const registerUrl = `${host}/register`
const loginUrl = `${host}/login`
const ticketUrl = `${host}/getWebSocketTicket/match`
const webSocketUrl = `${wsHost}/ws/match`

async function login(): Promise<string | undefined> {
    const response = await fetch(loginUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            username,
            password,
        })
    })
    console.log(`Login: ${response.status} ${response.statusText}`)
    return response.headers.get("Authorization")?.slice(7)
}

async function register(): Promise<string | undefined> {
    const response = await fetch(registerUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            username,
            password,
            name
        })
    })
    console.log(`Register: ${response.status} ${response.statusText}`)
    return response.headers.get("Authorization")?.slice(7)
}

async function getAuthToken(): Promise<string | undefined> {
    return await login() ?? await register()
}

async function getMatchTicket(token: string): Promise<object | undefined> {
    const response = await fetch(ticketUrl, {
        method: 'GET',
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
    console.log(`Ticket Request: ${response.status} ${response.statusText}`)
    return await response.json()
}

function connect(ticket: object) {
    const ticketString = JSON.stringify(ticket)
    console.log('Ticket is: ', ticketString)
    const ws = new WebSocket(`${webSocketUrl}?ticket=${ticketString}`)
    ws.on('open', () => {
        console.log('WebSocket connected')
    })
    ws.on('message', (message: string) => {
        console.log('WebSocket received messsage', message)
    })
    ws.on('close', () => {
        console.log('WebSocket closed')
    })
}


async function main() {
    const token = await getAuthToken()
    console.log(`Token: ${token}`)
    if (token == null) {
        throw Error('Token was null')
    }
    const ticket = await getMatchTicket(token)
    console.log('Ticket:', ticket)
    if (ticket == null) {
        throw Error('Ticket was null')
    }
    connect(ticket)
}

main()