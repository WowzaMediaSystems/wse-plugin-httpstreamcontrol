/*
 * This code and all components (c) Copyright 2006 - 2018, Wowza Media Systems, LLC. All rights reserved.
 * This code is licensed pursuant to the Wowza Public License version 1.0, available at www.wowza.com/legal.
 */
package com.wowza.wms.plugin;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.http.HTTProvider2Base;
import com.wowza.wms.http.IHTTPRequest;
import com.wowza.wms.http.IHTTPResponse;
import com.wowza.wms.logging.WMSLogger;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.stream.publish.Playlist;
import com.wowza.wms.stream.publish.Publisher;
import com.wowza.wms.stream.publish.Stream;
import com.wowza.wms.vhost.IVHost;

public class HTTPStreamControl extends HTTProvider2Base
{

	public static final String CLASSNAME = "HTTPStreamControl";
	private static final Class<HTTPStreamControl> CLASS = HTTPStreamControl.class;

	private WMSLogger logger = WMSLoggerFactory.getLogger(CLASS);

	@Override
	public void init()
	{
		super.init();
		logger.info(CLASSNAME + ".init: Build# 1");
	}

	@Override
	public void onHTTPRequest(IVHost vhost, IHTTPRequest req, IHTTPResponse resp)
	{
		if (!doHTTPAuthentication(vhost, req, resp))
			return;

		StringBuffer ret = new StringBuffer();
		Set<String> paramNames = req.getParameterNames();
		IApplicationInstance appInstance = null;
		String actionType = "getStreamNames";
		String streamName = null;
		String playlistName = null;
		String playlistItemName = null;
		int start = 0;
		int length = -1;

		if (paramNames.contains("appName"))
		{
			String appName = req.getParameter("appName");
			String appInstanceName = req.getParameter("appInstanceName");
			appInstanceName = appInstanceName == null ? "_definst_" : appInstanceName;
			try
			{
				appInstance = vhost.getApplication(appName).getAppInstance(appInstanceName);
			}
			catch (Exception e)
			{
				logger.error("Invalid appInstance: " + e);
			}

			if (appInstance != null)
			{
				if (paramNames.contains("action"))
					actionType = req.getParameter("action");
				if (paramNames.contains("streamName"))
					streamName = req.getParameter("streamName");
				if (paramNames.contains("playlistName"))
					playlistName = req.getParameter("playlistName");
				if (paramNames.contains("playlistItemName"))
					playlistItemName = req.getParameter("playlistItemName");
				if (paramNames.contains("start"))
					start = Integer.parseInt(req.getParameter("start"));
				if (paramNames.contains("length"))
					length = Integer.parseInt(req.getParameter("length"));

				if (actionType.equalsIgnoreCase("getStreamNames"))
					getStreamNames(appInstance, ret);
				else if (actionType.equalsIgnoreCase("openPlaylistOnStream") && playlistName != null && streamName != null)
					openPlaylistOnStream(appInstance, playlistName, streamName, ret);
				else if (actionType.equalsIgnoreCase("addItemToPlaylist") && playlistName != null && playlistItemName != null)
					addItemToPlaylist(appInstance, playlistName, playlistItemName, start, length, ret);
				else if (actionType.equalsIgnoreCase("removeItemFromPlaylist") && playlistItemName != null && streamName != null)
					removeItemFromPlaylist(appInstance, playlistItemName, streamName, ret);
				else if (actionType.equalsIgnoreCase("playNextPlaylistItem") && streamName != null)
					playNextPlaylistItem(appInstance, streamName, ret);
				else if (actionType.equalsIgnoreCase("addNewStream") && streamName != null)
					addNewStream(appInstance, streamName, ret);
				else if (actionType.equalsIgnoreCase("addNewPlaylist") && playlistName != null)
					addNewPlaylist(appInstance, playlistName, ret);
				else if (actionType.equalsIgnoreCase("stopStream") && streamName != null)
					stopStream(appInstance, streamName, ret);
				else
					ret.append("Invalid or missing argument(s).");
			}
			else
				ret.append("Requires a valid application name.");
		}
		else
			ret.append("Requires an application name.");

		try
		{
			OutputStream out = resp.getOutputStream();
			byte[] outBytes = ret.toString().getBytes();
			out.write(outBytes);
		}
		catch (Exception e)
		{
			logger.error(e);
		}

	}

	public void getStreamNames(IApplicationInstance appInstance, StringBuffer ret)
	{
		logger.info("getFiles");

		List<Publisher> publishers = null;
		try
		{
			publishers = appInstance.getPublishers();
		}
		catch (Exception e)
		{
			logger.error(e);
		}
		if (publishers != null)
		{
			Iterator<Publisher> iterp = publishers.iterator();
			iterp = publishers.iterator();
			while (iterp.hasNext())
			{
				Publisher publisher = iterp.next();
				logger.info("Stream Name: " + publisher.getStream().getName());
			}

			List<String> streams = appInstance.getStreams().getPublishStreamNames();
			Iterator<String> iter = streams.iterator();
			while (iter.hasNext())
			{
				String streamName = iter.next();
				ret.append(streamName);
			}
		}
	}

	public void openPlaylistOnStream(IApplicationInstance appInstance, String playlistName, String streamName, StringBuffer ret)
	{

		Stream stream = (Stream)appInstance.getProperties().getProperty(streamName);
		Playlist playlist = (Playlist)appInstance.getProperties().getProperty(playlistName);
		playlist.open(stream);
		logger.info(CLASSNAME + ".openPlaylistOnStream: playlist=" + playlistName + " stream=" + streamName);
		ret.append("Open Playlist: " + playlistName + " on Stream " + streamName);

	}

	public void addItemToPlaylist(IApplicationInstance appInstance, String playlistName, String playlistItemName, int start, int length, StringBuffer ret)
	{

		Playlist playlist = (Playlist)appInstance.getProperties().getProperty(playlistName);
		playlist.addItem(playlistItemName, start, length);
		logger.info(CLASSNAME + ".addItemToPlaylist: playlist=" + playlistName + " playlist item=" + playlistItemName + " start=" + start + "duration" + length);
		ret.append("Add Item to Playlist: " + playlistName + " with PlaylistItem=" + playlistItemName + " start=" + start + " duration=" + length);

	}

	public void removeItemFromPlaylist(IApplicationInstance appInstance, String playlistItemName, String streamName, StringBuffer ret)
	{

		Stream stream = (Stream)appInstance.getProperties().getProperty(streamName);
		stream.removeFromPlaylist(playlistItemName);
		logger.info(CLASSNAME + ".removeItemFromPlaylist: playlist item=" + playlistItemName + " stream=" + streamName);
		ret.append("Remove Item from Playlist: " + playlistItemName + " on Stream " + streamName);

	}

	public void playNextPlaylistItem(IApplicationInstance appInstance, String streamName, StringBuffer ret)
	{

		Stream stream = (Stream)appInstance.getProperties().getProperty(streamName);
		stream.next();
		logger.info(CLASSNAME + ".playNextPlaylistItem: stream=" + streamName);
		ret.append("Play Next Item on Playlist for Stream: " + streamName);

	}

	@SuppressWarnings("unchecked")
	public void addNewStream(IApplicationInstance appInstance, String streamName, StringBuffer ret)
	{

		Stream stream = Stream.createInstance(appInstance, streamName);
		appInstance.getProperties().put(streamName, stream);
		logger.info(CLASSNAME + ".addNewStream: stream=" + streamName);
		ret.append("Add New Stream for Playlist: " + streamName);

	}

	@SuppressWarnings("unchecked")
	public void addNewPlaylist(IApplicationInstance appInstance, String playlistName, StringBuffer ret)
	{

		Playlist playlist = new Playlist(playlistName);
		playlist.setRepeat(true);
		appInstance.getProperties().put(playlistName, playlist);
		logger.info(CLASSNAME + ".addNewPlaylist: playlist=" + playlistName);
		ret.append("Add New Playlist: " + playlistName);

	}

	public void stopStream(IApplicationInstance appInstance, String streamName, StringBuffer ret)
	{

		Stream stream = (Stream)appInstance.getProperties().remove(streamName);
		if (stream != null)
			stream.close();
		logger.info(CLASSNAME + ".stopStream: stream=" + streamName);
		ret.append("Stop Stream for Playlist: " + streamName);
	}

}
