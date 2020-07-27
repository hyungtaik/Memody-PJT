package com.web.blog.controller;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.web.blog.config.jwt.JwtTokenProvider;
import com.web.blog.domain.Users;
import com.web.blog.model.Response;
import com.web.blog.model.ResponseMessage;
import com.web.blog.model.RestException;
import com.web.blog.model.StatusCode;
import com.web.blog.service.UserService;
import com.web.blog.service.UserService2;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

/*
 * LoginController
 * <pre>
 * <b> History:</b>
 *			김형택, ver.0.1 , 2020-07-26, (First Commit)
 * </pre>
 * 
 * @author 김형택
 * @version 0.1, 2020-07-26, 유저 관리 Controller
 * @see None
 * 
 */
@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RequiredArgsConstructor
@RestController
public class LoginController {

	private final 	PasswordEncoder 	passwordEncoder;
	private final 	JwtTokenProvider 	jwtTokenProvider;
	private final 	RedisTemplate 		redisTemplate;
	private final 	UserService 		userService;
	private final 	UserService2 		mailService;

	/**
	 * 로그인 - 가입한 Email과 Password를 입력하여 로그인한다. Header에 토큰 값을 전달한다.
	 * 
	 * @param Users user - String email, String password
	 * @return ResponseEntity<Response> - StatusCode, ResponseMessage(LOGIN_SUCCESS,LOGIN_FAIL), HttpStatus
	 * @exception RestException - NOT_FOUND_USER
	 */
	@ApiOperation(value = "로그인", response = ResponseEntity.class)
	@PostMapping("/login")
	public ResponseEntity login(@RequestBody Users user, HttpServletResponse res) {
		Users member = userService.findByEmail(user.getEmail())
				.orElseThrow(() -> new RestException(ResponseMessage.NOT_FOUND_USER, HttpStatus.NOT_FOUND));
		
		if (!passwordEncoder.matches(user.getPassword(), member.getPassword())) {
			return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.LOGIN_FAIL),
					HttpStatus.FORBIDDEN);
		}
		
		String token = jwtTokenProvider.createToken(member.getUsername(), member.getRoles());
		res.setHeader("auth", token);
		
		return new ResponseEntity<Response>(new Response(StatusCode.OK, ResponseMessage.LOGIN_SUCCESS, member),
				HttpStatus.OK);
	}

	/**
	 * 회원가입 - Email, Password, NickName을 입력하여 회원가입을 한다.
	 * 
	 * @param Users user - String email, String uid(NickName), password 
	 * @return ResponseEntity<Response> - StatusCode, ResponseMessage(LOGIN_SUCCESS,LOGIN_FAIL), HttpStatus
	 * @exception RestException - ALREADY_USER
	 */
	@ApiOperation(value = "회원 가입", response = ResponseEntity.class)
	@PostMapping("/users")
	public ResponseEntity signUp(@RequestBody Users user) {
		if (userService.findByEmail(user.getEmail()).isPresent()) {
			throw new RestException(ResponseMessage.ALREADY_USER, HttpStatus.FORBIDDEN);
		}
		
		userService.join(user,passwordEncoder.encode(user.getPassword()));
		
		return new ResponseEntity<Response>(new Response(StatusCode.CREATED, ResponseMessage.CREATED_USER),HttpStatus.CREATED);
	}

	/**
	 * 로그아웃 - 토큰을 만료시키고 redis에 저장하여 블랙리스트 생성(토큰만료시간까지 저장시켜두고 추후 자동 삭제)
	 * 
	 * @param
	 * @return ResponseEntity<Response> - StatusCode, ResponseMessage(LOGOUT_SUCCESS,LOGOUT_FAIL), HttpStatus
	 * @exception FORBIDDEN
	 */
	@ApiOperation(value = "로그아웃", response = ResponseEntity.class)
	@GetMapping(path = "/logout")
	public ResponseEntity logout(HttpServletRequest req) {
		System.out.println("test");
		String token = req.getHeader("auth");
		if (jwtTokenProvider.validateToken(token)) {
			Date expirationDate = jwtTokenProvider.getExpirationDate(token);
			redisTemplate.opsForValue().set(token, "logout",
					expirationDate.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
			return new ResponseEntity<Response>(new Response(StatusCode.NO_CONTENT, ResponseMessage.LOGOUT_SUCCESS),HttpStatus.OK);
		}else {
			return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.LOGOUT_FAIL),HttpStatus.FORBIDDEN);
		}
		
	}

	/**
	 * 회원 정보 조회 - 입력받은 이메일의 회원정보를 조회
	 * 
	 * @param String Email
	 * @return ResponseEntity<Response> - StatusCode, ResponseMessage(READ_USER,NOT_FOUND_USER), HttpStatus, data(사용자 정보)
	 * @exception RestException - NOT_FOUND
	 */
	@ApiOperation(value = "회원 정보 조회", response = ResponseEntity.class)
	@GetMapping(value = "/users/{email}")
	public ResponseEntity userInfo(@PathVariable String email, HttpServletRequest req) {
		String token = req.getHeader("auth");
		
		if (jwtTokenProvider.validateToken(token)) {
			Users member = userService.findByEmail(email)
					.orElseThrow(() -> new RestException(ResponseMessage.NOT_FOUND_USER, HttpStatus.NOT_FOUND));
			
			return new ResponseEntity<Response>(new Response(StatusCode.OK, ResponseMessage.READ_USER, member),
					HttpStatus.OK);
		}else {
			return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.FORBIDDEN),HttpStatus.FORBIDDEN);
		}
	}
	
	
	
	/**
	 * 회원 탈퇴 - 현재 로그인되어있는 사용자의 회원 정보 삭제
	 * 
	 * @param String Email
	 * @return ResponseEntity<Response> - StatusCode, ResponseMessage(READ_USER,NOT_FOUND_USER), HttpStatus
	 * @exception FORBIDDEN
	 */
	@ApiOperation(value = "회원 탈퇴", response = ResponseEntity.class)
	@DeleteMapping(value = "/users/{email}")
	public ResponseEntity findAllUser(@PathVariable String email, HttpServletRequest req) {
		String token = req.getHeader("auth");
		
		if (jwtTokenProvider.validateToken(token) && jwtTokenProvider.getUserPk(token).equals(email)) {
			userService.deleteUser(email);
			
			return new ResponseEntity<Response>(new Response(StatusCode.NO_CONTENT, ResponseMessage.DELETE_USER),
					HttpStatus.OK);
		}else {
			return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.FORBIDDEN),HttpStatus.FORBIDDEN);
		}
	}
	
	
	
	@ApiOperation(value = "비밀번호 재설정", response = ResponseEntity.class)
	@GetMapping(value = "/users/{emails}/pw")
	public ResponseEntity tempPassword(@PathVariable String email) {
		
		if(userService.findByEmail(email).isPresent()) {
			// 임시 비밀번호 생성
			String tmpPassword = UUID.randomUUID().toString().replaceAll("-", "");
			tmpPassword = tmpPassword.substring(0, 10);
			
			final String SEND_EMAIL_ID = "kimhyungtaik@gmail.com"; // 관리자 email
			String subject = "임시비밀번호 발급 안내 입니다.";
			StringBuilder sb = new StringBuilder();
			sb.append("귀하의 임시 비밀번호 입니다. 로그인 후 비밀번호를 변경하세요.\n");
			sb.append("임시 비밀번호 : " + tmpPassword);
			
			String ecdPwd = passwordEncoder.encode(tmpPassword);
			
			if (mailService.send(subject, sb.toString(), SEND_EMAIL_ID, email, null)) {
				// DB에 임시 비밀번호로 재설정 해줘야함. 암호화 해서 
				userService.tempPwdUpdate(email,ecdPwd);
				
				return new ResponseEntity<Response>(new Response(StatusCode.OK, ResponseMessage.TEMP_PWD),HttpStatus.OK);
			} else {
				return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.FORBIDDEN),HttpStatus.OK);
			}
		}else {
			return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.NOT_FOUND_USER),HttpStatus.FORBIDDEN);
		}

	}

//	@ApiOperation(value = "회원정보 수정", response = ResponseEntity.class)
//	@PutMapping(value = "/users/{emails}")
//	public ResponseEntity updateUser(@PathVariable String email, Users userHttpServletRequest req) {
	@ApiOperation(value = "회원정보 수정", response = ResponseEntity.class)
	@PutMapping(value = "/users")
	public ResponseEntity updateUser(Users user) {
//		String token = req.getHeader("auth");
//		if (jwtTokenProvider.validateToken(token) && jwtTokenProvider.getUserPk(token).equals(email)) {
//		Users member = userService.findByEmail(user.getEmail())
//				.orElseThrow(() -> new RestException(ResponseMessage.NOT_FOUND_USER, HttpStatus.NOT_FOUND));
		System.out.println(user.getUid()+" "+user.getPassword()+" "+user.getEmail());
		userService.userUpdate(user, passwordEncoder.encode(user.getPassword()));
		return new ResponseEntity<Response>(new Response(StatusCode.CREATED, ResponseMessage.UPDATE_USER, user),HttpStatus.OK);
//		}else {
//			return new ResponseEntity<Response>(new Response(StatusCode.FORBIDDEN, ResponseMessage.FAIL_UPDATE_USER),HttpStatus.FORBIDDEN);
//		}
	}
	
}
