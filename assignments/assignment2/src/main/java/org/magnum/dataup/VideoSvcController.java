/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.http.Multipart;

@Controller
public class VideoSvcController {

	private Map<Long, Video> videos = new HashMap<Long, Video>();
	private static final AtomicLong currentId = new AtomicLong(0L);
	private VideoFileManager videoDataMgr;

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> GetVideoList() {

		System.out.println("GetVideoList");

		List<Video> videoList = new ArrayList<Video>(videos.values());
		return videoList;
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video AddVideo(@RequestBody Video video) {

		System.out.println("AddVideo: video.title - " + video.getTitle());

		if (video.getId() == 0) {
			long id = getNextId();
			String dataUrl = getDataUrl(id);

			video.setId(id);
			video.setDataUrl(dataUrl);
		}

		videos.put(video.getId(), video);

		return video;
	}

	@Multipart
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus AddVideoData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long videoId,
			@RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData,
			HttpServletResponse response) throws IOException {

		System.out.println("AddVideoData: videoId - " + videoId);

		Video video = videos.get(videoId);

		if (video != null) {
			videoDataMgr = VideoFileManager.get();
			videoDataMgr.saveVideoData(video, videoData.getInputStream());

			VideoStatus status = new VideoStatus(VideoState.READY);
			return status;
		} else {
			throw new ResourceNotFoundException();
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	public @ResponseBody void GetVideoData(
			@PathVariable(VideoSvcApi.ID_PARAMETER) long videoId,
			HttpServletResponse response) throws IOException {

		System.out.println("GetVideoData: videoId - " + videoId);

		Video video = videos.get(videoId);
		if (video == null) {
			throw new ResourceNotFoundException();
		}

		if (videoDataMgr.hasVideoData(video)) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			videoDataMgr = VideoFileManager.get();
			videoDataMgr.copyVideoData(video, outputStream);

			response.getOutputStream().write(outputStream.toByteArray());
		} else {
			throw new ResourceNotFoundException();
		}
	}

	private String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes()).getRequest();
		String base = "http://"
				+ request.getServerName()
				+ ((request.getServerPort() != 80) ? ":"
						+ request.getServerPort() : "");
		return base;
	}

	private String getDataUrl(long videoId) {
		String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
		return url;
	}

	private Long getNextId() {
		return currentId.incrementAndGet();
	}

}
