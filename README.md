# HTTPStreamControl
You can use the **HTTPStreamControl** plugin for Wowza Streaming Engine™ [media server software](https://www.wowza.com/products/streaming-engine) to control Stream class streams and playlists through HTTP requests. Use it to dynamically create, set up, and start new live Stream class streams from static and live sources, and to add sources to existing Stream class streams. For extra control, use this HTTP provider with the **StreamPublisher** module.

This repo includes a [compiled version](/lib/wse-plugin-httpstreamcontrol.jar).

## Prerequisites
Wowza Streaming Engine™ 4.0.0 or later is required.

## Usage
Control the playlist through an HTTP request, passing the required variables as query string parameters.

1. Add a new stream:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=addNewStream&streamName=testStream"
```

2. Add a new playlist:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=addNewPlaylist&playlistName=p1"
```

3.  Add a playlist item:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=addItemToPlaylist&playlistName=p1&streamName=testStream&playlistItemName=sample.mp4"
```

4. Open the playlist on a stream. By default, start is **0** and length is **-1**. For live streams, use **-2** for start. Any positive integer for length plays the source for that number of seconds:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=openPlaylistOnStream&playlistName=p1&streamName=testStream"
```

5. Play the next item on the playlist:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=playNextPlaylistItem&streamName=testStream"
```

6. Remove an item from a playlist:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=removeItemFromPlaylist&playlistItemName=sample.mp4&streamName=testStream"
```
7. Stop a stream:
```
curl "http://localhost:8086/streamcontrol?appName=live&action=stopStream&streamName=testStream"
```

## More resources
To use the compiled version of this module, see [Control Stream class streams dynamically with a Wowza Streaming Engine Java module](https://www.wowza.com/docs/how-to-control-stream-class-streams-dynamically-httpstreamcontrol).

[Wowza Streaming Engine Server-Side API Reference](https://www.wowza.com/resources/serverapi/)

[How to extend Wowza Streaming Engine using the Wowza IDE](https://www.wowza.com/docs/how-to-extend-wowza-streaming-engine-using-the-wowza-ide)

[How to use server-side modules and HTTP Providers](https://www.wowza.com/docs/how-to-use-wowza-streaming-engine-server-side-modules-and-http-providers)

Wowza Media Systems™ provides developers with a platform to create streaming applications and solutions. See [Wowza Developer Tools](https://www.wowza.com/developer) to learn more about our APIs and SDKs. 

## Contact
[Wowza Media Systems, LLC](https://www.wowza.com/contact)

## License
This code is distributed under the [Wowza Public License](/LICENSE.txt).
