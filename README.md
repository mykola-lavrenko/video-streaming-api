# Video Streaming API

## Overview
The **Video Streaming API** is a Java-based application designed to manage video metadata and handle video file processing. 
Implemented according to [the task](Task.md).

## Functional Requirements:
The main purpose of the application is to:
* Store information related to videos.
* Stream video content.
* Keeps track of user engagement actions related to videos.

## Prerequisites
* Java 21
* Git
* Docker and Docker Compose
* Optional: An IDE or Editor (e.g., IntelliJ IDEA)

## Getting Started
### Clone repository
``` bash
git clone https://github.com/mykola-lavrenko/video-streaming-api.git
cd video-streaming-api
```
### Build
``` bash
./gradlew build
```
### Testing
Unit and integration tests are set up using **JUnit**. Run tests using the following command:
```bash
./gradlew test
```
### Build and run
#### Use Docker Compose
Run the application along with a PostgreSQL database using Docker Compose:
``` bash
docker-compose up
```
#### Build and Run the application separately
Run the backend locally with:
``` bash
./gradlew bootRun
```
And only database via Docker Compose
``` bash
docker-compose up postgres
```
The backend runs on `http://localhost:8080`, and PostgreSQL is hosted at `localhost:5432`.

## Features
- **Video Management**:
    - Upload and update video metadata.
    - Video soft-deletion feature.

- **Video Retrieval**:
    - Load video previews.
    - Play video content
    - Video content is mocked by a string in this implementation, as per [the task](Task.md) suggestion.

- **Engagement Tracking**:
    - Retrieve engagement statistics (views and impressions).
    - Incremental counters for video views and impressions.

- **Pagination with Filters**:
    - Paginated video metadata listing with filters (e.g., search by title, director, release year).

- **Database Versioning**:
    - Database schema managed using **Liquibase**.

- **Docker Support**:
    - Configured with **Docker Compose** for easy local development and testing.

## Notes and Potential Improvements
### Notes
1. **Mocked Video Playback**: Currently, the video playback functionality returns a mock string response instead of playing actual video files. This can be replaced with actual video streaming capabilities using tools like **AWS S3**, CDN providers, or video streaming libraries.
2. **Soft Delete**: Videos are marked as "deleted" instead of being deleted from the database to facilitate future restoration or maintain history for analytics.

### Potential Improvements
1. **Video Streaming**:
    - Replace mocked video content with real file management and streaming capabilities (e.g., using **Spring Resource** or **AWS S3**).
    - Adopt streaming protocols like **HLS** (HTTP Live Streaming) for scalable video playback.

2. **Authentication and Authorization**:
    - Integrate **Spring Security** with OAuth2 or JWT to secure API endpoints.
    - Role-based access control (e.g., admin vs. regular users).

3. **API Documentation**:
    - Add **Swagger UI** for API usage visualization and interaction.

4. **Asynchronous Processing**:
    - Process large video uploads (e.g., generating previews) asynchronously using tools like **RabbitMQ** or **Kafka**.

5. **Cloud Storage Integration**:
    - Store videos in cloud-based solutions like **AWS S3**, **Google Cloud Storage**, or **Azure Blob Storage**.
    - Reduce local storage requirements and leverage CDN for fast video delivery.

6. **Improved Metadata Search**:
    - Implement advanced searching capabilities (e.g., **Elasticsearch**) to allow full-text search and complex filter combinations.

7. **Frontend Integration**:
    - Build a frontend client (e.g., **React** or **Angular**) to interact with this backend for better video management and playback.

8. **Scalable Architecture**:
    - Containerize the application for microservices deployment using tools like **Kubernetes** or **AWS ECS**.
    - Add horizontal scaling support for high-traffic demands.

9. **Logs and monitoring**

## Examples of Rest endpoints
### **Video Management**
Upload a new video along with metadata.
  -  Requires:
      - `metadata`: JSON payload containing video details (e.g., title, director, synopsis).
      - `videoFile`: Multipart file containing the video content.

```bash
  curl -X POST http://127.0.0.1:8080/api/v1/videos \
  -H "Content-Type: multipart/form-data" \
  --form 'videoFile=Kinda video;type=text/plain;filename=video.txt' \
  --form 'metadata={
      "title": "Sample Video",              
      "synopsis": "synopsis",
      "director": "John Doe",
      "castMembers": "John Doe, Tak Sho, Ni Cho",
      "yearOfRelease": 2023,
      "genre": "ACTION",
      "runningTime": "PT1H30M",
      "description": "A sample video description"
  };type=application/json'
```

Update metadata for an existing video.
```bash
  curl -X PUT --location "http://127.0.0.1:8080/api/v1/videos/{id}" \
      -H "Content-Type: application/json" \
      -d '{
            "title": "Sample Video",
            "synopsis": "synopsis",
            "director": "John Doe",
            "castMembers": "John Doe, Tak Sho, Ni Cho",
            "yearOfRelease": 2023,
            "genre": "ACTION",
            "runningTime": "PT1H30M",
            "description": "A sample video description"
          }'  
```

Soft-delete a video by its ID.
```bash
  curl -X DELETE --location "http://127.0.0.1:8080/api/v1/videos/{id}" \
  -H "Accept: application/json"
```

Retrieve metadata and preview content for a video:
```bash
  curl -X GET --location "http://127.0.0.1:8080/api/v1/videos/{id}"
```

### **Video Playback**
Stream the video content (mocked string response for this implementation)
```bash
  curl -X GET --location "http://127.0.0.1:8080/api/v1/videos/{id}/play"
```

### **Engagement Tracking**
Get engagement details (e.g., views and impressions).
```bash
  curl -X GET --location "http://127.0.0.1:8080/api/v1/videos/{id}/engagement-statistics"
```

### **Video Listing**
Retrieve a paginated list of all videos with optional filters:
- `title` - Filter by video title.
- `director` - Filter by director name.
- `yearOfRelease` - Filter by release year.
```bash
  curl -X GET --location "http://127.0.0.1:8080/api/v1/videos"
  curl -X GET --location "http://127.0.0.1:8080/api/v1/videos?yearOfRelease=2023&title=Title&director=Peter"
```
