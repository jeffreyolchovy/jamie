# "Pull that up, Jamie"
*Short description*

<!-- ![GUI step #1 screenshot](https://github.com/jeffreyolchovy/jamie/raw/master/screenshots/1.png) -->
<!-- #![GUI step #2 screenshot](https://github.com/jeffreyolchovy/jamie/raw/master/screenshots/2.png) -->

## Usage
Front-end/GUI (node/npm) dependencies are downloaded via sbt.

From an interactive sbt session, issue:
```
> project gui
> run --port 8080
```

Visit http://localhost:8080 in a browser to begin processing an audio stream.

## Project structure

### common
Shared resources (domain objects, [de]serializers, etc.) used by both the api and gui projects.

### api
A REST API for processing audio streams and performing the subsequent entity detection.

### gui
A web application and server, with browser-based GUI.
