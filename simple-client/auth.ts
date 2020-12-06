import {loginUrl, registerUrl, UserData} from "./config.ts";

export async function login(username: string, password: string): Promise<string | undefined> {
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

export async function register(user: UserData): Promise<string | undefined> {
    const response = await fetch(registerUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(user)
    })
    console.log(`Register: ${response.status} ${response.statusText}`)
    return response.headers.get("Authorization")?.slice(7)
}

export type AuthorizedUser = {
    username: string,
    token: string,
}

async function getAuthToken(user: UserData): Promise<string | undefined> {
    const {username, password} = user
    return await login(username, password) ?? await register(user)
}

export async function authorize(user: UserData): Promise<AuthorizedUser | null> {
    const token = await getAuthToken(user)
    if (token == null) {
        return null
    } else {
        return {
            ...user,
            token,
        }
    }
}