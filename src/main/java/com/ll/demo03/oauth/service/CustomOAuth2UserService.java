package com.ll.demo03.oauth.service;

import com.ll.demo03.member.domain.Member;
import com.ll.demo03.member.service.port.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.ll.demo03.oauth.domain.OAuth2UserInfo;
import com.ll.demo03.oauth.domain.PrincipalDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, attributes);
        Member member = getOrSave(oAuth2UserInfo);

        PrincipalDetails principalDetails = new PrincipalDetails(member, attributes, userNameAttributeName);
        log.info("유저 정보 로딩됨 ={}", principalDetails );
        log.info("유저 정보 로딩됨 ={}", principalDetails.getName()  );
        log.info("유저 정보 로딩됨 ={}", principalDetails.getUsername()  );

//        SecurityContextHolder.getContext().setAuthentication(
//                new OAuth2AuthenticationToken(
//                        principalDetails,
//                        principalDetails.getAuthorities(),
//                        registrationId
//                )
//        );

        return principalDetails;
    }

    private Member getOrSave(OAuth2UserInfo oAuth2UserInfo) {
        log.info("oAuth2UserInfo.email() = {}", oAuth2UserInfo.email());
        return memberRepository.findByEmail(oAuth2UserInfo.email())
                .orElseGet(() -> {
                    Member newMember = oAuth2UserInfo.toEntity();
                    log.info("New member created: {}", newMember);
                    return memberRepository.save(newMember);
                });
    }


}
