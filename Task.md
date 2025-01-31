# Task

### Video Streaming API

Our client, a major Hollywood production house, has decided to build tooling for their next generation video streaming platform.
They have asked us to implement functionality for their content managers to publish videos, handle their metadata and handle
the view and impression metrics to subsequently monetize user viewership.

### Functional Requirements:
The main purpose of the application is to:
* Store information related to videos.
* Stream video content.
* Keeps track of user engagement actions related to videos.

### To perform the above functionality, you are expected to implement an API which would allow users to:
* Publish a video
* Add and Edit the metadata associated with the video (some examples of metadata are: Title, Synopsis, Director, Cast, Year of Release, Genre, Running time)
* Delist (soft delete) a video and its associated metadata
* Load a video – return the video metadata and the corresponding content.
* Play a video – return the content related to a video. The content can be a simple string that acts as a mock to the actual video content.
* List all available videos – This should return only a subset of the video metadata such as: Title, Director, Main Actor, Genre and Running Time.
* Search for videos based on some search/query criteria (e.g.: Movies directed by a specific director) – 
The returned result-set should still feature the same subset of metadata as outlined in the previous point.
* Retrieve the engagement statistic for a video. Engagement can be split in 2:
  * Impressions – A client loading a video.
  * Views – A client playing a video.

### Technical Requirements:
You are required to provide a working solution that exposes the above functionality.
Furthermore, the following technical requirements should be met:
* Use of JDK 17 or higher (Open source and LTS versions are preferable)
* Utilise Spring Boot 2.7 or higher.
* Utilise Maven or Gradle for dependency management and build lifecycle.
* Feature appropriate unit and integration tests.
* Persist information in a database or datastore of your choice.
* Contain detailed documentation behind any decisions and/or assumptions taken and
instructions on how to compile and run the solution.
* Your solution must be submitted to a public repository of your choice (ex. GitHub, Gitlab, Bitbucket), 
and should not take no longer than 6 hours to complete. 