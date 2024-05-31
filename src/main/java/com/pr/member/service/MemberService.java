package com.pr.member.service;

import com.pr.member.domain.MemberInfo;
import com.pr.member.dto.OAuthAttributes;
import com.pr.member.dto.SessionMember;
import com.pr.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemberService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final MemberRepository memberRepository;
    private final HttpSession httpSession;

    public MemberService(MemberRepository memberRepository, HttpSession httpSession) {
        this.memberRepository = memberRepository;
        this.httpSession = httpSession;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Oauth2 서비스 id (구글, 카카오, 네이버)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // Oauth2 로그인 진행 시 키가 되는 필드 값(PK)
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // OauthUserService
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        MemberInfo memberInfo = save(attributes);

        httpSession.setAttribute("memberInfo", new SessionMember(memberInfo));

        System.out.println("getClientRegistration: " + userRequest.getClientRegistration());
        System.out.println("getAccessToken: " + userRequest.getAccessToken().getTokenValue());
        System.out.println("getAttributes: " + oAuth2User.getAttributes());

        return buildOAuth2User(memberInfo);
    }

    // OAuth2User 객체 생성
    private OAuth2User buildOAuth2User(MemberInfo memberInfo) {
        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(memberInfo.getRoleKey()));
        Map<String, Object> attributes = Map.of(
                "email", memberInfo.getEmail(),
                "name", memberInfo.getName(),
                "picture", memberInfo.getPicture()
        );

        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    // 유저 생성
    private MemberInfo save(OAuthAttributes attributes) {
        MemberInfo memberInfo = memberRepository.findByEmail(attributes.getEmail()).orElse(null); // email로 회원 조회

        if (memberInfo == null) { // 회원이 존재하지 않는 경우에만 저장
            memberInfo = attributes.toEntity(); // 새로운 회원 생성
            memberInfo = memberRepository.save(memberInfo); // 회원 저장
        }

        return memberInfo; // 최종적으로 회원 반환
    }

    // 사용자 정보 업데이트 메서드
    public void updateMember(OAuthAttributes attributes) {
        memberRepository.findByEmail(attributes.getEmail())
                .ifPresent(entity -> entity.update(attributes.getNickName(), attributes.getUpdateDate()));
    }

    public List<MemberInfo> findAllMembers() {
        return memberRepository.findAll();
    }

}