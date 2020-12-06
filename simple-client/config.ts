const heroku = false

const host = heroku ? 'https://fighting-game-server.herokuapp.com' : 'http://localhost:8080'
const wsHost = heroku ? 'ws://fighting-game-server.herokuapp.com' : 'ws://localhost:8080'

export const registerUrl = `${host}/register`
export const loginUrl = `${host}/login`

// user

export type UserData = {
    username: string,
    password: string,
    name: string,
    facultyId: number
}

export const user1 = {
    username: 'username',
    password: 'password',
    name: 'name',
    facultyId: 1,
}

// ws

export type WsConfig = {
    wsEndpoint: string,
    getTicketEndpoint: string,
}

export const subWsConfig: WsConfig = {
    wsEndpoint: `${wsHost}/ws/sub`,
    getTicketEndpoint: `${host}/getWebSocketTicket/sub`
}