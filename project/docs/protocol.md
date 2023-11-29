# Communications Protocol

This document describes the communications protocol build on top of TCP used by servers and clients.

## Message Format

All messages are sent as a JSON object with the following fields:
 - **method** the action being done
 - additional parameters depending on the method

## Client-to-Server Messages

| Method | Parameters | Response | Description                |
|--------|------------|----------|----------------------------|
| write  | list       | NULL     | Write a list on the server |
| get    | id         | list     | Get a list from the server |

### Server-to-Client Messages

The server will never initiate a message to the client.

### Server-to-Server Messages

| Method   | Parameters | Response                 | Description                                          |
|----------|------------|--------------------------|------------------------------------------------------|
| write    | list       | status {0 OK, 1 [Error]} | Write a list to the db                               |
| read     | id         | list                     | Write a list to the db                               |
| redirect | request    | response                 | Redirect a client request to proper node in the ring |
