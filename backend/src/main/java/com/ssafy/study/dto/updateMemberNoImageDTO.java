package com.ssafy.study.dto;

import java.util.Set;

import com.ssafy.study.model.DateForUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class updateMemberNoImageDTO {
	private Long id;
	private String userEamil;
	private String userName;
	private String userContent;
	private String password;
	private Long majorSeq;
	private String major;
	private String education;
	private String field1;
	private String desiredField1;
	private String desiredField2;
	private String desiredField3;
	private boolean isSecret;
}
