# Communications Protocol

This document describes the communications protocol build on top of TCP used by servers and clients.

## Message Format

All messages are sent as a JSON object with the following fields:
 - **method** the action being done
 - additional parameters depending on the method

## Client-to-Server Messages

| Method | Parameters | Response | Description                 |
|--------|------------|----------|-----------------------------|
| write  | list       | NULL     | Write a list on the server  |
| read   | id         | list     | Read a list from the server |

### Server-to-Client Messages

The server will never initiate a message to the client.

### Server-to-Server Messages

| Method             | Parameters         | Response                   | Description                                     |
|--------------------|--------------------|----------------------------|-------------------------------------------------|
| write              | quorumId, list     | method{writeAck}           | Write a list to the db                          |
| writeAck           | quorumId           | NULL                       | Acknowledge list was written                    |
| redirectWrite      | redirectId, list   | method{redirectWriteReply} | Redirect a client write request                 |
| redirectWriteReply | redirectId         | NULL                       | Answer to redirection                           |
| read               | quorumId, listId   | method{readAck}            | Read a list to the db                           |
| readAck            | quorumId, list     | NULL                       | Send requested list                             |
| redirectRead       | redirectId, listId | method{redirectReadReply}  | Redirect a client read request                  |
| redirectReadReply  | redirectId, list   | NULL                       | Answer to redirection                           |
| statusUpdate       | node, status       | NULL                       | Send update changes on a node                   |
| hintedHandoff      | list<list>, node   | NULL                       | Send a list of lists to hold while node is down |
| returnHinted       | list<list          | NULL                       | Send hinted lists back to node                  |
| heartbeat          | NULL               | NULL                       | Send a heartbeat to the server                  |
| heartbeatReply     | NULL               | NULL                       | Reply to heartbeat                              | 
