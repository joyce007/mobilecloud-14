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

package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoLikeController {
	@Autowired
	VideoRepository videos;
	
	@RequestMapping(value = "/go",method = RequestMethod.GET)
	public @ResponseBody String goodLuck(){
		return "Good Luck!";
	}
	
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videos.findAll());
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
		return videos.findOne(id);
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		videos.save(v);
		return v;
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public void likeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse resp) {
		Video v = videos.findOne(id);
		if(v == null) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			Set<String> likedUsernames = v.getLikedUsernames();
			String username = p.getName();
			if(likedUsernames.contains(username)) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				likedUsernames.add(username);
				v.setLikedUsernames(likedUsernames);
				v.setLikes(likedUsernames.size());
				videos.save(v);
			}
		}
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse resp) {
		Video v = videos.findOne(id);
		if(v == null) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			Set<String> likedUsernames = v.getLikedUsernames();
			String username = p.getName();
			if(!likedUsernames.contains(username)) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				likedUsernames.remove(username);
				v.setLikedUsernames(likedUsernames);
				v.setLikes(likedUsernames.size());
				videos.save(v);
			}
		}
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
		return videos.findByName(title);
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
		return videos.findByDurationLessThan(duration);
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id) {
		Video v = videos.findOne(id);
		if(v != null) {
			return v.getLikedUsernames();
		}
		// TODO throw 404 error
		return null;
	}
}
