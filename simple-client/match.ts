import {makeWebSocketConnection} from "./ws.ts";
import {matchWsConfig, user1, user2} from "./config.ts";
import {ask} from "./ask.ts";

const user1OpponentFacultyId = 2
const user2OpponentFacultyId = 1

const user1AdditionalParam = user1OpponentFacultyId == null ? '' : `&opponentFacultyId=${user1OpponentFacultyId}`
const user2AdditionalParam = user2OpponentFacultyId == null ? '' : `&opponentFacultyId=${user2OpponentFacultyId}`

function makePlayerAction(attacker: String, target: String, itemId: number | null): string {
    return JSON.stringify({
        type: 'match.PlayerAction',
        attacker,
        target,
        itemId
    })
}

function makeSkipTurn() {
    return JSON.stringify({
        type: 'match.SkipTurn',
        isDefenced: true,
    })
}

async function main() {
    console.log('user1', user1)
    console.log('user2', user2)

    const ws1 = await makeWebSocketConnection(matchWsConfig, user1, user1AdditionalParam)
    ws1.on('open', () => {
        console.log('user1 connected')
    })
    ws1.on('message', (msg: any) => {
        console.log('user1 received', msg)
    })
    ws1.on('close', () => {
        console.log('user1 disconnected')
    })
    const ws2 =  await makeWebSocketConnection(matchWsConfig, user2, user2AdditionalParam)
    ws2.on('open', () => {
        console.log('user2 connected')
    })
    ws2.on('message', (msg: any) => {
        console.log('user2 received', msg)
    })
    ws2.on('close', () => {
        console.log('user2 disconnected')
    })
    const users = [user1, user2]
    const websockets = [ws1, ws2]
    console.log("-->Commands syntax: \n--> <userNumber> hit [itemId] \n--> <userNumber> skip")
    while (true) {
        const command = await ask("")
        const parts = command.split(" ")
        const user = users[parseInt(parts[0]) - 1]
        const otherUser = users[2 - parseInt(parts[0])]
        const websocket = websockets[parseInt(parts[0]) - 1]

        if (parts[1] == "hit") {
            const itemId: number | null = Number.isNaN(parseInt(parts[2])) ? null : parseInt(parts[2])
            websocket.send(makePlayerAction(user.username, otherUser.username, itemId))
        } else if (parts[1] == "skip") {
            websocket.send(makeSkipTurn())
        }
    }
}

main()