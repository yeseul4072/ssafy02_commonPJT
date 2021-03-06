package com.ssafy.study.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.ssafy.study.dto.createStudyroomDTO;
import com.ssafy.study.dto.dateDTO;
import com.ssafy.study.dto.detailStudyroomDTO;
import com.ssafy.study.dto.getStudyroomDTO;
import com.ssafy.study.dto.roomFeedDTO;
import com.ssafy.study.dto.roomId_memberIdDTO;
import com.ssafy.study.dto.updateDateDTO;
import com.ssafy.study.dto.updateStudyroomDTO;
import com.ssafy.study.model.*;
import com.ssafy.study.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@ApiResponses(value = { @ApiResponse(code = 401, message = "Unauthorized", response = BasicResponse.class),
        @ApiResponse(code = 403, message = "Forbidden", response = BasicResponse.class),
        @ApiResponse(code = 404, message = "Not Found", response = BasicResponse.class),
        @ApiResponse(code = 500, message = "Failure", response = BasicResponse.class) })

@CrossOrigin(origins = { "http://i3a102.p.ssafy.io" })
@RestController
@RequestMapping("/study")
public class studyroomController {

	@Autowired
	StudyroomRepository studyroomRepo;
	
	@Autowired
	MemberRepository memberRepo;
	
	@Autowired
	StudyroomUserRepository studyroomuserRepo;
	
	@Autowired
	DateForStudyroomRepository dateforstudyroomRepo;

	@Autowired
	DateForUserRepository dateforuserRepo;
	
	@Autowired
	HashtagRepository hashRepo;

	@Autowired
	LicenseRepository licenseRepo;
	
	@Autowired
	FeedRepository feedRepo;

	@Autowired
	LikeRepository likeRepo;

	@Autowired
	CommentRepository commentRepo;
	
	@PostMapping("/createStudyroom")
	public Object createStudyroom(@RequestBody createStudyroomDTO studyroomObject, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
//		System.out.println(studyroomObject);
//		Long id = (Long)session.getAttribute("uid");
		Optional<Member> member = memberRepo.findById(studyroomObject.getCaptinId());
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		}
		Optional<License> license=licenseRepo.findByLicenseCode(studyroomObject.getLicenseCode());
		if(!license.isPresent()){
			result.status = false;
			result.data = "자격증 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
		}
		
		Studyroom studyroom = new Studyroom(license.get(), studyroomObject.getCaptinId(), studyroomObject.getRoomTitle(), studyroomObject.getTestDate(), 
				studyroomObject.isPrivate(), studyroomObject.getRoomPassword(), studyroomObject.getRoomInfo(), studyroomObject.getRoomGoal(), studyroomObject.getMaxMembers());
		StudyroomUser studyroomuser = new StudyroomUser(studyroom, member.get());
		studyroomRepo.save(studyroom);
		studyroomuserRepo.save(studyroomuser);
		
		for (Hashtag hashtag : studyroomObject.getRoomHashtag()) {
			hashtag.setStudyroom(studyroom);
			hashRepo.save(hashtag);
		}
		
		for (DateForStudyroom date : studyroomObject.getDateForStudyroom()) {
			date.setStudyroom(studyroom);
			dateforstudyroomRepo.save(date);
			dateforuserRepo.save(new DateForUser(member.get(),date,false));
		}
		
		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
	}
	
	@Transactional
	@PostMapping("updateStudyroom")
	public Object updateStudyroom(@RequestBody updateStudyroomDTO studyroomObject, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Studyroom> studyroom = studyroomRepo.findById(studyroomObject.getId());
		if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
		}

		studyroom.get().setRoomTitle(studyroomObject.getRoomTitle());
		studyroom.get().setTestDate(studyroomObject.getTestDate());
		studyroom.get().setPrivate(studyroomObject.isPrivate());
		studyroom.get().setRoomPassword(studyroomObject.getRoomPassword());
		studyroom.get().setMaxMembers(studyroomObject.getMaxMembers());
		studyroom.get().setRoomGoal(studyroomObject.getRoomGoal());
		studyroom.get().setRoomInfo(studyroomObject.getRoomInfo());
		
		for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom.get())) {
			if(!studyroomObject.getRoomHashtag().contains(tag)) {
				hashRepo.delete(tag);
			} else {
				studyroomObject.getRoomHashtag().remove(tag);
			}
		}
		
		for (Hashtag newtag : studyroomObject.getRoomHashtag()) {
			newtag.setStudyroom(studyroom.get());
			hashRepo.save(newtag);
		}
 		studyroomRepo.save(studyroom.get());

		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
	}
	
	@Transactional
	@PostMapping("/deleteStudyroom")
	public Object deleteStudyroom(@RequestBody roomId_memberIdDTO ID, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		System.out.println(ID.getMemberId() + ", " + ID.getRoomId());
		
		Optional<Member> captain = memberRepo.findById(ID.getMemberId());
		Optional<Studyroom> studyroom = studyroomRepo.findByIdAndCaptainId(ID.getRoomId(), ID.getMemberId());
		if(!captain.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		} else if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 멤버가 방장인 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		for (StudyroomUser roomuser : studyroomuserRepo.findAllByStudyroom(studyroom.get())) {
			for (DateForUser date : dateforuserRepo.findAllByMember(roomuser.getMember())) {
				if(date.getDateForStudyroom().getStudyroom().equals(studyroom.get()))
					dateforuserRepo.deleteById(date.getId());
			}
		}
		dateforstudyroomRepo.deleteAllByStudyroom(studyroom.get());
		hashRepo.deleteAllByStudyroom(studyroom.get());
		for (Feed feed : feedRepo.findAllByStudyroom(studyroom.get())) {
			likeRepo.deleteAllByFeed(feed);
			commentRepo.deleteAllByFeed(feed);
		}
		feedRepo.deleteAllByStudyroom(studyroom.get());
		studyroomuserRepo.deleteAllByStudyroom(studyroom.get());
		studyroomRepo.deleteById(ID.getRoomId());
		
		
		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
	}
	
	@Transactional
	@PostMapping("/updateDate")
	public Object updateDate(@RequestBody updateDateDTO newdates, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Studyroom> studyroom = studyroomRepo.findById(newdates.getRoomId());
		if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		for (DateForStudyroom date : dateforstudyroomRepo.findAllByStudyroom(studyroom.get())) {
			boolean isSame = false;
			DateForStudyroom tempdate = null;
			for (DateForStudyroom newdate : newdates.getDateForStudyrooms()) {
				if(date.equals(newdate)) {
					tempdate = newdate;
					isSame = true;
					break;
				}
			}
			if(isSame) {
//				System.out.println("yes");
				newdates.getDateForStudyrooms().remove(tempdate);
			} else {
//				System.out.println("no");
				dateforuserRepo.deleteAllByDateForStudyroom(date);
				dateforstudyroomRepo.delete(date);
			}
		}
		
		for (DateForStudyroom newdate : newdates.getDateForStudyrooms()) {
			newdate.setStudyroom(studyroom.get());
			dateforstudyroomRepo.save(newdate); // 새로 추가
			for (StudyroomUser roomuser : studyroomuserRepo.findAllByStudyroom(studyroom.get())) {
				dateforuserRepo.save(new DateForUser(roomuser.getMember(), newdate, false));
			}
		}
		
		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
	}
	
	@PostMapping("/addMember")
	public Object addMember(@RequestBody roomId_memberIdDTO ID, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
//		Long id = (Long)session.getAttribute("uid");
		Optional<Member> member = memberRepo.findById(ID.getMemberId());
		Optional<Studyroom> studyroom = studyroomRepo.findById(ID.getRoomId());
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		} else if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		StudyroomUser studyroomuser = new StudyroomUser(studyroom.get(), member.get());
		studyroomuserRepo.save(studyroomuser);
		
		for (DateForStudyroom date : dateforstudyroomRepo.findAllByStudyroom(studyroom.get())) {
			dateforuserRepo.save(new DateForUser(member.get(),date, false));
		}
		
		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
		
	}
	
	@Transactional
	@PostMapping("/removeMember")
	public Object removeMember(@RequestBody roomId_memberIdDTO ID, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
//		Long id = (Long)session.getAttribute("uid");
		Optional<Member> member = memberRepo.findById(ID.getMemberId());
		Optional<Studyroom> studyroom = studyroomRepo.findById(ID.getRoomId());
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		} else if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		Optional<StudyroomUser> studyroomuser = studyroomuserRepo.findByMemberAndStudyroom(member.get(), studyroom.get()); 
		if(!studyroomuser.isPresent()) {
			result.status = false;
			result.data = "스터디룸에서 해당 멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		studyroomuserRepo.delete(studyroomuser.get());
		
		for (DateForUser date : dateforuserRepo.findAllByMember(member.get())) {
			if(date.getDateForStudyroom().getStudyroom().equals(studyroom.get()))
				dateforuserRepo.deleteById(date.getId());
		}
		
		for (Feed feed : feedRepo.findAllByStudyroomAndMember(studyroom.get(), member.get())) {
			likeRepo.deleteAllByFeed(feed);
			commentRepo.deleteAllByFeed(feed);
			feedRepo.deleteById(feed.getId());
		}
		
		for (Feed feed : feedRepo.findAllByStudyroom(studyroom.get())) {
			likeRepo.deleteAllByMemberAndFeed(member.get(), feed);
		}
		
		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
		
	}
	
	@GetMapping("/getAllMyTodo")
	public Object getMyTodo(@RequestParam Long UID) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Member> member = memberRepo.findById(UID);
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		}
		
		result.status = true;
		result.data = "success";
		result.object = dateforuserRepo.findAllByMember(member.get());
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
	
		return response;
	}
	
	@GetMapping("/getTodayStudyroomTodo")
	public Object getTodayStudyroomTodo(@RequestParam Long roomId, @RequestParam Long UID) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Member> member = memberRepo.findById(UID);
		Optional<Studyroom> studyroom = studyroomRepo.findById(roomId);
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		} else if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		List<DateForUser> dates = new ArrayList<>();
		for (DateForStudyroom roomdate : dateforstudyroomRepo.findTodayTodo(studyroom.get(), new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24), new Date())) {
			Optional<DateForUser> date = dateforuserRepo.findByMemberAndDateForStudyroom(member.get(), roomdate);
			if(date.isPresent()) {
				dates.add(date.get());
			}
		}		
		
		result.status = true;
		result.data = "success";
		result.object = dates.stream().sorted(Comparator.comparing(DateForUser::getId)).collect(Collectors.toList());
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
	
		return response;
	}
	
	@PostMapping("/checkTodo")
	public Object checkTodo(@RequestBody DateForUser date) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<DateForUser> checkdate = dateforuserRepo.findById(date.getId());
		if(!checkdate.isPresent()) {
			result.status=false;
			result.data="해당 일정을 찾을 수 없음";
			return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
		}
		
		if(!checkdate.get().isChecked()) {
			checkdate.get().setChecked(true);
			dateforuserRepo.save(checkdate.get());
		} else {
			checkdate.get().setChecked(false);
			dateforuserRepo.save(checkdate.get());
		}
		
		result.status = true;
		result.data = "success";
		
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
	}
	
	@GetMapping("/getAll") // 등록 날짜로 정렬
	public Object getAll(HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		List<getStudyroomDTO> rooms = new ArrayList<>();
		for (Studyroom studyroom : studyroomRepo.findAll()) {
			int curMembers = studyroomuserRepo.countByStudyroom(studyroom);
			Set<String> hashtags = new HashSet<String>();
			for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom)) {
				hashtags.add(tag.getHashtag());
			}
			rooms.add(new getStudyroomDTO(studyroom.getId(), studyroom.getLicense().getLicenseName(), memberRepo.findById(studyroom.getCaptainId()).get(), 
					studyroom.getRoomTitle(), studyroom.getTestDate(), studyroom.getRoomDate(), studyroom.isPrivate(), studyroom.getRoomPassword(), 
					studyroom.getRoomInfo(), curMembers, studyroom.getMaxMembers(), hashtags));
		}
		
		Collections.sort(rooms, new Comparator<getStudyroomDTO>() {

			@Override
			public int compare(getStudyroomDTO o1, getStudyroomDTO o2) {
				if(o1.getRoomDate().before(o2.getRoomDate()))
					return 1;
				else
					return -1;
			}
		});
		
		result.status=true;
		result.data="success";
		result.object=rooms;
		response= new ResponseEntity<>(result,HttpStatus.OK);

		return response;
	}

	@GetMapping("/getStudyroomDetail")
	public Object getStudyroomDetail(@RequestParam Long roomId, @RequestParam Long UID, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Studyroom> studyroom = studyroomRepo.findById(roomId);
		if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		String licenseName = studyroom.get().getLicense().getLicenseName();
		Optional<Member> captain = memberRepo.findById(studyroom.get().getCaptainId());
		Optional<Member> member = memberRepo.findById(UID);
		if(!captain.isPresent()) {
			result.status = false;
			result.data = "스터디룸의 방장을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		} else if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		}
		boolean isIn = studyroomuserRepo.findByMemberAndStudyroom(member.get(), studyroom.get()).isPresent() ? true : false;
		int curMembers = studyroomuserRepo.countByStudyroom(studyroom.get());
		List<dateDTO> dates = new ArrayList<dateDTO>();
		List<roomFeedDTO> feeds = new ArrayList<roomFeedDTO>();
		List<String> tags = new ArrayList<String>();
		for (DateForStudyroom date : dateforstudyroomRepo.findAllByStudyroom(studyroom.get())) {
			dates.add(new dateDTO(date.getId(), date.getTodoDate(), date.getTodoContent()));
		}

		for (Feed feed : feedRepo.findAllByStudyroom(studyroom.get())) {
			feeds.add(new roomFeedDTO(feed.getId(),feed.getImageType(), feed.getStudyImage(), feed.getRegistTime()));
		}
		for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom.get())) {
			tags.add(tag.getHashtag());
		}
		
		
		Collections.sort(dates, new Comparator<dateDTO>() {

			@Override
			public int compare(dateDTO o1, dateDTO o2) {
				if(o1.getTodoDate().before(o2.getTodoDate()))
					return -1;
				else
					return 1;
			}
		});
		
		Collections.sort(feeds, new Comparator<roomFeedDTO>() {

			@Override
			public int compare(roomFeedDTO o1, roomFeedDTO o2) {
				if(o1.getRegistTime().before(o2.getRegistTime()))
					return 1;
				else
					return -1;
			}
		});
		
		detailStudyroomDTO detail = new detailStudyroomDTO(licenseName, captain.get(), studyroom.get().getRoomTitle(), studyroom.get().getTestDate(), 
				studyroom.get().isPrivate(), isIn, studyroom.get().getRoomPassword(), studyroom.get().getRoomInfo(), studyroom.get().getRoomGoal(), 
				curMembers, studyroom.get().getMaxMembers(), dates, feeds, tags);
		
		result.status=true;
		result.data="success";
		result.object=detail;
		response= new ResponseEntity<>(result,HttpStatus.OK);

		return response;
	}
	
	@GetMapping("/getByUser")
	public Object getByUser(@RequestParam Long userId, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		List<getStudyroomDTO> rooms = new ArrayList<>();
		Optional<Member> member = memberRepo.findById(userId);
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		}
		Iterator<StudyroomUser> iter = studyroomuserRepo.findAllByMember(member.get()).stream().collect(Collectors.toSet()).iterator();
		while(iter.hasNext()) {
			Studyroom studyroom = iter.next().getStudyroom();
			int curMembers = studyroomuserRepo.countByStudyroom(studyroom);
			Set<String> hashtags = new HashSet<String>();
			for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom)) {
				hashtags.add(tag.getHashtag());
			}
			rooms.add(new getStudyroomDTO(studyroom.getId(), studyroom.getLicense().getLicenseName(), memberRepo.findById(studyroom.getCaptainId()).get(), 
					studyroom.getRoomTitle(), studyroom.getTestDate(), studyroom.getRoomDate(), studyroom.isPrivate(), studyroom.getRoomPassword(), 
					studyroom.getRoomInfo(), curMembers, studyroom.getMaxMembers(), hashtags));
		}
		
		Collections.sort(rooms, new Comparator<getStudyroomDTO>() {
			public int compare(getStudyroomDTO o1, getStudyroomDTO o2) {
				if(o1.getRoomDate().before(o2.getRoomDate()))
					return 1;
				else
					return -1;
			}
		});
		
		result.status=true;
		result.data="success";
		result.object=rooms;
		response= new ResponseEntity<>(result,HttpStatus.OK);

		return response;
	}
	
	@GetMapping("/getStudyroomMembers")
	public Object getStudyroomMembers(@RequestParam Long roomId) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Studyroom> studyroom = studyroomRepo.findById(roomId);
		if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		Set<Member> members = new HashSet<Member>();
		for (StudyroomUser roomuser : studyroomuserRepo.findAllByStudyroom(studyroom.get())) {
			members.add(roomuser.getMember());
		}
		
		result.status=true;
		result.data="success";
		result.object=members;
		response = new ResponseEntity<>(result, HttpStatus.OK);
		
		return response;
	}
	
	
	@GetMapping("/findStudyroomByHashtag")
	public Object findByHashtag(@RequestParam String roomHashtag, HttpSession session){
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		roomHashtag=roomHashtag.trim();
		
		List<getStudyroomDTO> studyrooms = new ArrayList<>();
		
		Iterator<Hashtag> iter = hashRepo.findByHashtagContaining(roomHashtag).stream().collect(Collectors.toSet()).iterator();
		Set<Long> roomIds = new HashSet<Long>();
		while(iter.hasNext()) {
			roomIds.add(iter.next().getStudyroom().getId());
		}
		
		for (Long roomId : roomIds) {
			Optional<Studyroom> studyroom = studyroomRepo.findById(roomId);
			int curMembers = studyroomuserRepo.countByStudyroom(studyroom.get());
			Set<String> hashtags = new HashSet<String>();
			for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom.get())) {
				hashtags.add(tag.getHashtag());
			}
			studyrooms.add(new getStudyroomDTO(studyroom.get().getId(), studyroom.get().getLicense().getLicenseName(), memberRepo.findById(studyroom.get().getCaptainId()).get(), 
					studyroom.get().getRoomTitle(), studyroom.get().getTestDate(), studyroom.get().getRoomDate(), studyroom.get().isPrivate(), studyroom.get().getRoomPassword(), 
					studyroom.get().getRoomInfo(), curMembers, studyroom.get().getMaxMembers(), hashtags));
		}
		
		Collections.sort(studyrooms, new Comparator<getStudyroomDTO>() {

			@Override
			public int compare(getStudyroomDTO o1, getStudyroomDTO o2) {
				if(o1.getRoomDate().before(o2.getRoomDate()))
					return 1;
				else
					return -1;
			}
		});
		
		result.status=true;
		result.data="success";
		result.object=studyrooms;
		response= new ResponseEntity<>(result,HttpStatus.OK);

		return response;
	}

	
	@GetMapping("/findStudyroomByLicense")
	public Object findByLicense(@RequestParam String licenseName, HttpSession session){
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		licenseName=licenseName.trim();
		
		StringBuilder likeKeyword =new StringBuilder("%");
		for(int i=0;i<licenseName.length();i++) {
			likeKeyword.append(licenseName.charAt(i)+"%");
		}
		
		List<getStudyroomDTO> studyrooms = new ArrayList<>();
		
		Iterator<License> iter = licenseRepo.findByKeyword(likeKeyword.toString()).stream().collect(Collectors.toSet()).iterator();
		while(iter.hasNext()) {
			for (Studyroom studyroom : studyroomRepo.findAllByLicense(iter.next())) {
				int curMembers = studyroomuserRepo.countByStudyroom(studyroom);
				Set<String> hashtags = new HashSet<String>();
				for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom)) {
					hashtags.add(tag.getHashtag());
				}
				studyrooms.add(new getStudyroomDTO(studyroom.getId(), studyroom.getLicense().getLicenseName(), memberRepo.findById(studyroom.getCaptainId()).get(), 
						studyroom.getRoomTitle(), studyroom.getTestDate(), studyroom.getRoomDate(), studyroom.isPrivate(), studyroom.getRoomPassword(), 
						studyroom.getRoomInfo(), curMembers, studyroom.getMaxMembers(), hashtags));
			}
		}
		
		Collections.sort(studyrooms, new Comparator<getStudyroomDTO>() {
			
			@Override
			public int compare(getStudyroomDTO o1, getStudyroomDTO o2) {
				if(o1.getRoomDate().before(o2.getRoomDate()))
					return 1;
				else
					return -1;
			}
		});
		
		result.status=true;
		result.data="success";
		result.object=studyrooms;
		response= new ResponseEntity<>(result,HttpStatus.OK);
		
		return response;
	}
	
	@GetMapping("/findStudyroomByCaptain")
	public Object findByCaptain(@RequestParam String captainName, HttpSession session){
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		captainName=captainName.trim();
		
		List<getStudyroomDTO> studyrooms = new ArrayList<>();

		Iterator<Member> iter = memberRepo.findAllByUserNameContaining(captainName).stream().collect(Collectors.toSet()).iterator();
		while(iter.hasNext()) {
			for (Studyroom studyroom : studyroomRepo.findAllByCaptainId(iter.next().getId())) {
				int curMembers = studyroomuserRepo.countByStudyroom(studyroom);
				Set<String> hashtags = new HashSet<String>();
				for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom)) {
					hashtags.add(tag.getHashtag());
				}
				studyrooms.add(new getStudyroomDTO(studyroom.getId(), studyroom.getLicense().getLicenseName(), memberRepo.findById(studyroom.getCaptainId()).get(), 
						studyroom.getRoomTitle(), studyroom.getTestDate(), studyroom.getRoomDate(), studyroom.isPrivate(), studyroom.getRoomPassword(), 
						studyroom.getRoomInfo(), curMembers, studyroom.getMaxMembers(), hashtags));
			}
		}
		
		Collections.sort(studyrooms, new Comparator<getStudyroomDTO>() {
			
			@Override
			public int compare(getStudyroomDTO o1, getStudyroomDTO o2) {
				if(o1.getRoomDate().before(o2.getRoomDate()))
					return 1;
				else
					return -1;
			}
		});
		
		result.status=true;
		result.data="success";
		result.object=studyrooms;
		response= new ResponseEntity<>(result,HttpStatus.OK);
		
		return response;
	}
	
	@GetMapping("/findStudyroomByRoomTitle")
	public Object findByTitle(@RequestParam String roomTitle, HttpSession session){
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		roomTitle=roomTitle.trim();

		result.status=true;
		result.data="success";
		result.object=studyroomRepo.findByRoomTitleContaining(roomTitle).stream().collect(Collectors.toSet());
		response= new ResponseEntity<>(result,HttpStatus.OK);

		return response;
	}

	@GetMapping("/getMyStudyroom")
	public Object getMyStudyroom(HttpSession session){
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();

		Optional<Member> member = memberRepo.findById((Long)session.getAttribute("uid"));
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		}
		Iterator<StudyroomUser> iter = studyroomuserRepo.findAllByMember(member.get()).stream().collect(Collectors.toSet()).iterator();
		Set<Studyroom> studyroomSet = new HashSet<Studyroom>();
		while(iter.hasNext()) {
			studyroomSet.add(iter.next().getStudyroom());
		}

		result.status=true;
		result.data="success";
		result.object=studyroomSet;
		response= new ResponseEntity<>(result,HttpStatus.OK);

		return response;
	}
	
	@GetMapping("/getDateForStudyroom")
	public Object getDateByTodoDate(@RequestParam Long id, HttpSession session) {
		ResponseEntity response = null;
		BasicResponse result = new BasicResponse();
		
		Optional<Member> member = memberRepo.findById((Long)session.getAttribute("uid"));
		Optional<Studyroom> studyroom = studyroomRepo.findById(id);
		if(!member.isPresent()) {
			result.status = false;
			result.data = "멤버를 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.FORBIDDEN);
		} else if(!studyroom.isPresent()) {
			result.status = false;
			result.data = "해당 스터디룸을 찾을 수 없음.";
			return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
		}
		
		result.status=true;
		result.data = "success";
		result.object = dateforstudyroomRepo.findAllByStudyroom(studyroom.get());;
		
		return response;
	}
	
    @GetMapping("/searchMembers")
    public Object searchMembers(@RequestParam String name) {
    	ResponseEntity response = null;
    	BasicResponse result = new BasicResponse();
    	
    	result.status=true;
        result.data="success";
        result.object=memberRepo.findAllByUserNameContaining(name);
        
        response= new ResponseEntity<>(result,HttpStatus.OK);

        return response;
    }
	
    @GetMapping("/hotRooms")
    public Object hotRooms() {

    	ResponseEntity response = null;
    	BasicResponse result = new BasicResponse();

    	List<getStudyroomDTO> rooms = new ArrayList<getStudyroomDTO>();
		for (Studyroom studyroom : studyroomRepo.findAllByIsPrivateFalse()) {
			int curMembers = studyroomuserRepo.countByStudyroom(studyroom);
			if(curMembers != studyroom.getMaxMembers()) { // 방이 안 찼으면
				Set<String> hashtags = new HashSet<String>();
				for (Hashtag tag : hashRepo.findAllByStudyroom(studyroom)) {
					hashtags.add(tag.getHashtag());
				}
				rooms.add(new getStudyroomDTO(studyroom.getId(), studyroom.getLicense().getLicenseName(), memberRepo.findById(studyroom.getCaptainId()).get(), 
						studyroom.getRoomTitle(), studyroom.getTestDate(), studyroom.getRoomDate(), studyroom.isPrivate(), studyroom.getRoomPassword(), 
						studyroom.getRoomInfo(), curMembers, studyroom.getMaxMembers(), feedRepo.countByStudyroomWeek(studyroom, new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7)), hashtags));
			}
		}
		
    	result.status=true;
        result.data="success";
        result.object=rooms.stream().sorted(new Comparator<getStudyroomDTO>() {
			@Override
			public int compare(getStudyroomDTO o1, getStudyroomDTO o2) {
				return o1.getFeeds() > o2.getFeeds() ? -1 : 1; 
			}
		}).limit(5).collect(Collectors.toList());
        
        response= new ResponseEntity<>(result,HttpStatus.OK);

        return response;
    }
    
}
