# Toss-a-Message
## How To
Toss-a-Message is a simple chatting server that works with Berkeley sockets. When the server 
is launched, any client can connect to it, and the only thing they have to do before they
[Send Messages](#send-message) is to [Log In](#log-in) with a unique username.

## Actions
When sending data to the Toss-Server, the client must also specify an Action they are performing
so that the server is aware of how to validate and use the data provided. The request and response
formats for each Action are explained in detail in the [Pillow Docs](./pillow/doc.md), and here's
just brief overview of how the communication process.

### Log In
→ {<br/>
&nbsp;&nbsp;action: 'log-in',<br/>
&nbsp;&nbsp;data: { username: 'Vincent van Gogh' }<br/>
}

*The user who's logging in:*<br/>
← {<br/>
&nbsp;&nbsp;action: 'log-in',<br/>
&nbsp;&nbsp;data: { time: Date(Jan 15, 11.30 UTC) }, <br/>
&nbsp;&nbsp;status: 100<br/>
}

*Other users:*<br/>
← {<br/>
&nbsp;&nbsp;action: 'log-in',<br/>
&nbsp;&nbsp;data: { <br/>
&nbsp;&nbsp;&nbsp;&nbsp;username: 'Vincent van Gogh',<br/>
&nbsp;&nbsp;&nbsp;&nbsp;time: Date(Jan 15, 11.30 UTC)<br/>
&nbsp;&nbsp;},<br/>
&nbsp;&nbsp;status: 100<br/>
}

### Send Message
→ {<br/>
&nbsp;&nbsp;action: 'send-message',<br/>
&nbsp;&nbsp;data: { <br/>
&nbsp;&nbsp;&nbsp;&nbsp;username: 'Vincent van Gogh', <br/>
&nbsp;&nbsp;&nbsp;&nbsp;message: 'Chek my new painting!',<br/>
&nbsp;&nbsp;&nbsp;&nbsp;attachment: { file: ByteArray(987543 bytes), name: 'sunflowers.jpg' }<br/>
&nbsp;&nbsp;}<br/>
}

← {<br/>
&nbsp;&nbsp;action: 'send-message',<br/>
&nbsp;&nbsp;data: { <br/>
&nbsp;&nbsp;&nbsp;&nbsp;username: 'Vincent van Gogh', <br/>
&nbsp;&nbsp;&nbsp;&nbsp;message: 'Chek my new painting!',<br/>
&nbsp;&nbsp;&nbsp;&nbsp;attachment: { file: ByteArray(987543 bytes), name: 'sunflowers.jpg' },<br/>
&nbsp;&nbsp;&nbsp;&nbsp;time: Date(Jan 15, 11.36 UTC)<br/>
&nbsp;&nbsp;},<br/>
&nbsp;&nbsp;status: 100<br/>
}

### Chunks
Chunks are needed to send large amounts of data, especially files. Sockets often can't transfer 
these at one go. Thus, whenever a server is up to send some data to the client, it prepends it 
with a message of shape

{ action: 'chunks', data: { chunks: 54631 }, status: 100 }

This means that a message of 54631 bytes is incoming, and the client should not parse it until
all of these are collected.

The same thing should be done client-side: before sending data for any other action, acknowledge
the server of the amount of bytes to expect.

→ { action: 'chunks', data: { chunks: 89076 } }

← { action: 'chunks', status: 101 }

Now the server is ready to accept 89076 bytes of data.

### Close Server
When the server is closed, it acknowledges the connected clients of it by sending

← { action: 'close-server', status: 101 }

... and force-closes all the connections after that.