export async function ask(question: string = '', stdin = Deno.stdin, stdout = Deno.stdout): Promise<string> {
    const buf = new Uint8Array(1024);

    // Write question to console
    await stdout.write(new TextEncoder().encode(question));

    // Read console's input into answer
    const n = <number>await stdin.read(buf);
    const answer = new TextDecoder().decode(buf.subarray(0, n));

    return answer.trim();
}