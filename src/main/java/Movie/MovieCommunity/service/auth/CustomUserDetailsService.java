package Movie.MovieCommunity.service.auth;

import Movie.MovieCommunity.JPADomain.Member;
import Movie.MovieCommunity.JPARepository.MemberRepository;
import Movie.MovieCommunity.advice.assertThat.DefaultAssert;
import Movie.MovieCommunity.config.security.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("유저 정보를 찾을 수 없습니다.")
                );

        return UserPrincipal.create(member);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Optional<Member> member = memberRepository.findById(id);

        DefaultAssert.isOptionalPresent(member);

        return UserPrincipal.create(member.get());
    }
}