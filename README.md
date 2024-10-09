# Automated Podcast Transcription
Programmatically transcribe audio streams and surface the well-known entities contained therein.

![MVP GUI screencap](https://github.com/jeffreyolchovy/jamie/raw/master/screenshots/mvp-10fps.gif)

## Installation requirements
- [sbt 1.x](https://www.scala-sbt.org)
- A local installation of [Node.js](https://nodejs.org) and [npm](https://www.npmjs.com)
- A local installation of [SoX](http://sox.sourceforge.net/)
- A Google Could Platform (GCP) project with the [Cloud Speech-to-Text](https://cloud.google.com/speech-to-text/) and [Natural Language](https://cloud.google.com/natural-language/) APIs enabled

## Usage
Ensure that your local environment is targeting the desired GCP project:
```sh
$ export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your/project/credentials.json
```

From the project root, begin an interactive sbt session:
```sh
$ sbt
```

From the interactive sbt session, issue:
```sbt
> project gui
> run --port 8080
```

Front-end/GUI (node/npm) dependencies will be downloaded via sbt.

Visit http://localhost:8080 in a browser to begin processing an audio stream.

## Project structure

### common
Shared resources (domain objects, [de]serializers, etc.) used by both the api and gui projects.

### api
A REST API for processing audio streams and performing the subsequent entity detection.

### gui
A web application and server, with browser-based GUI.
