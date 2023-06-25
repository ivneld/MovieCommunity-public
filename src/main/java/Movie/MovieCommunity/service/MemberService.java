package Movie.MovieCommunity.service;

import Movie.MovieCommunity.JPADomain.Member;
import Movie.MovieCommunity.JPADomain.Movie;
import Movie.MovieCommunity.JPARepository.MemberRepository;
import Movie.MovieCommunity.JPARepository.MovieRepository;

import Movie.MovieCommunity.JPARepository.MovieWithGenreRepository;
import Movie.MovieCommunity.util.CustomPageImpl;
import Movie.MovieCommunity.web.apiDto.movie.entityDto.LikeGenreDto;
import Movie.MovieCommunity.web.apiDto.movie.response.MovieLikeGenreResponse;
import Movie.MovieCommunity.web.apiDto.movie.response.MovieLikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MovieRepository movieRepository;
    private final MovieWithGenreRepository movieWithGenreRepository;
    public CustomPageImpl<MovieLikeResponse> findLikeMovie(Pageable pageable, Long memberId) {
        Page<Movie> likeMovieList = movieRepository.findByLikeMovie(pageable, memberId);
        List<MovieLikeResponse> responseList = likeMovieList.stream().map(lm -> MovieLikeResponse.builder()
                .id(lm.getId())
                .tmId(lm.getTmId())
                .posterPath(lm.getPosterPath())
                .movieNm(lm.getMovieNm())
                .openDt(lm.getOpenDt()).build()
        ).collect(Collectors.toList());
        return new CustomPageImpl<>(responseList, pageable, likeMovieList.getTotalElements());
    }

    public List<LikeGenreDto> findLikeMovieGenre(Long memberId) {
        List<Movie> likeMovieList = movieRepository.findByLikeMovieList(memberId);
        List<Long> movieIdList = likeMovieList.stream().map(lm -> lm.getId()).collect(Collectors.toList());
        List<LikeGenreDto> response = movieWithGenreRepository.findByLikeGenreCnt(movieIdList);


        return response;
    }
}

